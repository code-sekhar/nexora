package com.nexora.customer.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.customer.entity.Customer;
import com.nexora.customer.entity.CustomerLedger;
import com.nexora.customer.repository.CustomerLedgerRepository;
import com.nexora.customer.repository.CustomerRepository;
import com.nexora.invoice.entity.Invoice;
import com.nexora.invoice.repository.InvoiceRepository;
import com.nexora.payment.entity.Payment;
import com.nexora.payment.repository.PaymentRepository;

@Service
@Transactional
public class CustomerLedgerReconciliationService {

    private final CustomerRepository customerRepository;
    private final CustomerLedgerRepository customerLedgerRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public CustomerLedgerReconciliationService(
            CustomerRepository customerRepository,
            CustomerLedgerRepository customerLedgerRepository,
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository) {

        this.customerRepository = customerRepository;
        this.customerLedgerRepository = customerLedgerRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
    }

    /*
     * RECONCILE ALL CUSTOMERS
     */
    public int reconcileBusiness(Business business) {

        List<Customer> customers =
                customerRepository.findByBusinessId(
                        business.getId()
                );

        int createdEntries = 0;

        for (Customer customer : customers) {

            createdEntries += reconcileCustomer(
                    business,
                    customer
            );
        }

        return createdEntries;
    }

    /*
     * RECONCILE ONE CUSTOMER
     */
    public int reconcileCustomer(
            Business business,
            Customer customer) {

        int createdEntries = 0;

        /*
         * 1. Opening balance
         */
        createdEntries += reconcileOpeningBalance(
                business,
                customer
        );

        /*
         * 2. Customer invoices
         */
        List<Invoice> invoices =
                invoiceRepository
                        .findByCustomerIdAndBusinessIdOrderByCreatedAtAsc(
                                customer.getId(),
                                business.getId()
                        );

        for (Invoice invoice : invoices) {

            /*
             * CANCELLED invoices should not create
             * customer receivable entries.
             */
            if (invoice.getStatus()
                    == Invoice.InvoiceStatus.CANCELLED) {

                continue;
            }

            /*
             * SALE ledger
             */
            boolean saleExists =
                    customerLedgerRepository
                            .existsByInvoiceIdAndType(
                                    invoice.getId(),
                                    CustomerLedger.LedgerType.SALE
                            );

            if (!saleExists) {

                createSaleEntry(
                        business,
                        customer,
                        invoice
                );

                createdEntries++;
            }

            /*
             * PAYMENT ledger
             */
            List<Payment> payments =
                    paymentRepository
                            .findByInvoiceIdAndBusinessIdOrderByPaymentDateDesc(
                                    invoice.getId(),
                                    business.getId()
                            );

            for (Payment payment : payments) {

                boolean paymentLedgerExists =
                        customerLedgerRepository
                                .existsByPayment_Id(
                                        payment.getId()
                                );

                if (!paymentLedgerExists) {

                    createPaymentEntry(
                            business,
                            customer,
                            invoice,
                            payment
                    );

                    createdEntries++;
                }
            }
        }

        /*
         * Rebuild running balances after
         * reconciliation.
         */
        rebuildCustomerBalances(
                business,
                customer
        );

        return createdEntries;
    }

    /*
     * OPENING BALANCE
     */
    private int reconcileOpeningBalance(
            Business business,
            Customer customer) {

        BigDecimal openingDue =
                customer.getOpeningDue();

        if (openingDue == null ||
                openingDue.compareTo(BigDecimal.ZERO) <= 0) {

            return 0;
        }

        List<CustomerLedger> entries =
                customerLedgerRepository
                        .findByCustomerIdAndBusinessIdOrderByCreatedAtAsc(
                                customer.getId(),
                                business.getId()
                        );

        boolean exists =
                entries.stream()
                        .anyMatch(entry ->
                                entry.getType()
                                        == CustomerLedger.LedgerType.OPENING_BALANCE
                        );

        if (exists) {
            return 0;
        }

        CustomerLedger ledger =
                new CustomerLedger();

        ledger.setBusiness(business);
        ledger.setCustomer(customer);

        ledger.setType(
                CustomerLedger.LedgerType.OPENING_BALANCE
        );

        ledger.setDebit(openingDue);
        ledger.setCredit(BigDecimal.ZERO);

        /*
         * Opening balance must appear before
         * historical invoices/payments.
         */
        List<Invoice> invoices =
                invoiceRepository
                        .findByCustomerIdAndBusinessIdOrderByCreatedAtAsc(
                                customer.getId(),
                                business.getId()
                        );

        if (!invoices.isEmpty()
                && invoices.get(0).getCreatedAt() != null) {

            ledger.setCreatedAt(
                    invoices.get(0)
                            .getCreatedAt()
                            .minusDays(1)
            );
        } else {

            ledger.setCreatedAt(
                    LocalDateTime.now()
            );
        }

        ledger.setBalance(openingDue);

        ledger.setDescription(
                "Customer opening balance"
        );

        customerLedgerRepository.save(ledger);

        return 1;
    }

    /*
     * CREATE SALE LEDGER
     */
    private void createSaleEntry(
            Business business,
            Customer customer,
            Invoice invoice) {

        CustomerLedger ledger =
                new CustomerLedger();

        ledger.setBusiness(business);
        ledger.setCustomer(customer);

        ledger.setType(
                CustomerLedger.LedgerType.SALE
        );

        ledger.setInvoice(invoice);

        ledger.setDebit(
                invoice.getGrandTotal()
        );

        ledger.setCredit(
                BigDecimal.ZERO
        );

        /*
         * IMPORTANT:
         * Use the invoice's original date/time
         * instead of current time.
         */
        if (invoice.getCreatedAt() != null) {

            ledger.setCreatedAt(
                    invoice.getCreatedAt()
            );
        }

        ledger.setBalance(
                BigDecimal.ZERO
        );

        ledger.setDescription(
                "Sale through invoice "
                        + invoice.getInvoiceNumber()
        );

        customerLedgerRepository.save(ledger);
    }

    /*
     * CREATE PAYMENT LEDGER
     */
    private void createPaymentEntry(
            Business business,
            Customer customer,
            Invoice invoice,
            Payment payment) {

        CustomerLedger ledger =
                new CustomerLedger();

        ledger.setBusiness(business);
        ledger.setCustomer(customer);

        ledger.setType(
                CustomerLedger.LedgerType.PAYMENT
        );

        ledger.setInvoice(invoice);
        ledger.setPayment(payment);

        ledger.setDebit(
                BigDecimal.ZERO
        );

        ledger.setCredit(
                payment.getAmount()
        );

        /*
         * Use the original payment date/time
         * instead of reconciliation time.
         */
        if (payment.getPaymentDate() != null) {

            ledger.setCreatedAt(
                    payment.getPaymentDate()
            );
        }

        ledger.setBalance(
                BigDecimal.ZERO
        );

        ledger.setDescription(
                "Payment received for invoice "
                        + invoice.getInvoiceNumber()
        );

        customerLedgerRepository.save(ledger);
    }

    /*
     * REBUILD RUNNING BALANCE
     */
    private void rebuildCustomerBalances(
            Business business,
            Customer customer) {

        List<CustomerLedger> entries =
                customerLedgerRepository
                        .findByCustomerIdAndBusinessIdOrderByCreatedAtAsc(
                                customer.getId(),
                                business.getId()
                        );

        BigDecimal balance =
                BigDecimal.ZERO;

        for (CustomerLedger ledger : entries) {

            BigDecimal debit =
                    ledger.getDebit() != null
                            ? ledger.getDebit()
                            : BigDecimal.ZERO;

            BigDecimal credit =
                    ledger.getCredit() != null
                            ? ledger.getCredit()
                            : BigDecimal.ZERO;

            balance =
                    balance
                            .add(debit)
                            .subtract(credit);

            ledger.setBalance(balance);
        }

        customerLedgerRepository.saveAll(entries);
    }
    /*
     * REPAIR EXISTING RECONCILIATION ENTRIES
     *
     * Fixes ledger entries that were created during
     * reconciliation with the current timestamp.
     */
    public int repairHistoricalLedgerDates(
            Business business,
            Customer customer) {

        int repaired = 0;

        /*
         * Find customer's invoices in chronological order.
         */
        List<Invoice> invoices =
                invoiceRepository
                        .findByCustomerIdAndBusinessIdOrderByCreatedAtAsc(
                                customer.getId(),
                                business.getId()
                        );

        if (invoices.isEmpty()) {
            return 0;
        }

        /*
         * Repair OPENING_BALANCE
         */
        List<CustomerLedger> entries =
                customerLedgerRepository
                        .findByCustomerIdAndBusinessIdOrderByCreatedAtAsc(
                                customer.getId(),
                                business.getId()
                        );

        for (CustomerLedger ledger : entries) {

            /*
             * Opening balance should be before
             * the first historical invoice.
             */
            if (ledger.getType()
                    == CustomerLedger.LedgerType.OPENING_BALANCE) {

                LocalDateTime firstInvoiceCreatedAt =
                        invoices.get(0).getCreatedAt();

                if (firstInvoiceCreatedAt != null) {

                    LocalDateTime correctDate =
                            firstInvoiceCreatedAt.minusSeconds(1);

                    ledger.setCreatedAt(correctDate);

                    repaired++;
                }
            }

            /*
             * Repair SALE ledger using invoice creation time.
             */
            if (ledger.getType()
                    == CustomerLedger.LedgerType.SALE
                    && ledger.getInvoice() != null) {

                Invoice invoice =
                        ledger.getInvoice();

                if (invoice.getCreatedAt() != null) {

                    ledger.setCreatedAt(
                            invoice.getCreatedAt()
                    );

                    repaired++;
                }
            }

            /*
             * Repair PAYMENT ledger using
             * original payment date.
             */
            if (ledger.getType()
                    == CustomerLedger.LedgerType.PAYMENT
                    && ledger.getPayment() != null) {

                Payment payment =
                        ledger.getPayment();

                if (payment.getPaymentDate() != null) {

                    ledger.setCreatedAt(
                            payment.getPaymentDate()
                    );

                    repaired++;
                }
            }
        }

        customerLedgerRepository.saveAll(entries);

        /*
         * Rebuild balances after repairing dates.
         */
        rebuildCustomerBalances(
                business,
                customer
        );

        return repaired;
    }
}