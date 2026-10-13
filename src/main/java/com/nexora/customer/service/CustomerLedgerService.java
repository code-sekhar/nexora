package com.nexora.customer.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.customer.dto.CustomerLedgerResponse;
import com.nexora.customer.dto.CustomerLedgerSummaryResponse;
import com.nexora.customer.entity.Customer;
import com.nexora.customer.entity.CustomerLedger;
import com.nexora.customer.repository.CustomerLedgerRepository;
import com.nexora.invoice.entity.Invoice;
import com.nexora.payment.entity.Payment;
import com.nexora.sales.returnmodule.entity.SalesReturn;

@Service
@Transactional
public class CustomerLedgerService {

    private final CustomerLedgerRepository customerLedgerRepository;

    public CustomerLedgerService(
            CustomerLedgerRepository customerLedgerRepository) {

        this.customerLedgerRepository =
                customerLedgerRepository;
    }

    /*
     * CREATE SALE LEDGER
     */
    public CustomerLedger createSaleEntry(
            Business business,
            Customer customer,
            Invoice invoice,
            BigDecimal amount) {

        BigDecimal previousBalance =
                getCurrentBalance(
                        customer.getId(),
                        business.getId()
                );

        BigDecimal newBalance =
                previousBalance.add(amount);

        CustomerLedger ledger =
                new CustomerLedger();

        ledger.setBusiness(business);
        ledger.setCustomer(customer);

        ledger.setType(
                CustomerLedger.LedgerType.SALE
        );

        ledger.setInvoice(invoice);

        ledger.setDebit(amount);
        ledger.setCredit(BigDecimal.ZERO);
        ledger.setBalance(newBalance);

        ledger.setDescription(
                "Sale through invoice "
                        + invoice.getInvoiceNumber()
        );

        return customerLedgerRepository.save(ledger);
    }

    /*
     * CREATE PAYMENT LEDGER
     */
    public CustomerLedger createPaymentEntry(
            Business business,
            Customer customer,
            Invoice invoice,
            Payment payment,
            BigDecimal amount) {

        BigDecimal previousBalance =
                getCurrentBalance(
                        customer.getId(),
                        business.getId()
                );

        BigDecimal newBalance =
                previousBalance.subtract(amount);

        CustomerLedger ledger =
                new CustomerLedger();

        ledger.setBusiness(business);
        ledger.setCustomer(customer);

        ledger.setType(
                CustomerLedger.LedgerType.PAYMENT
        );

        ledger.setInvoice(invoice);
        ledger.setPayment(payment);

        ledger.setDebit(BigDecimal.ZERO);
        ledger.setCredit(amount);
        ledger.setBalance(newBalance);

        ledger.setDescription(
                "Payment received for invoice "
                        + invoice.getInvoiceNumber()
        );

        return customerLedgerRepository.save(ledger);
    }

    /*
     * GET ALL LEDGER ENTRIES
     */
    @Transactional(readOnly = true)
    public List<CustomerLedgerResponse> getAllLedger(
            Business business) {

        return customerLedgerRepository
                .findByBusinessIdOrderByCreatedAtAsc(
                        business.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * GET CUSTOMER LEDGER
     */
    @Transactional(readOnly = true)
    public CustomerLedgerSummaryResponse getCustomerLedger(
            Business business,
            Customer customer) {

        List<CustomerLedger> ledgers =
                customerLedgerRepository
                        .findByCustomerIdAndBusinessIdOrderByCreatedAtAsc(
                                customer.getId(),
                                business.getId()
                        );

        BigDecimal openingBalance =
                BigDecimal.ZERO;

        BigDecimal totalSales =
                BigDecimal.ZERO;

        BigDecimal totalPayments =
                BigDecimal.ZERO;
        
        BigDecimal totalSalesReturns =
                BigDecimal.ZERO;

        for (CustomerLedger ledger : ledgers) {

            if (ledger.getType() ==
                    CustomerLedger.LedgerType.OPENING_BALANCE) {

                openingBalance =
                        openingBalance.add(
                                ledger.getDebit()
                        );
            }

            if (ledger.getType() ==
                    CustomerLedger.LedgerType.SALE) {

                totalSales =
                        totalSales.add(
                                ledger.getDebit()
                        );
            }

            if (ledger.getType() ==
                    CustomerLedger.LedgerType.PAYMENT) {

                totalPayments =
                        totalPayments.add(
                                ledger.getCredit()
                        );
            }
            
            if (ledger.getType() ==
                    CustomerLedger.LedgerType.SALES_RETURN) {

                totalSalesReturns =
                        totalSalesReturns.add(
                                ledger.getCredit()
                        );
            }
        }

        BigDecimal currentBalance =
                ledgers.isEmpty()
                        ? BigDecimal.ZERO
                        : ledgers
                                .get(ledgers.size() - 1)
                                .getBalance();

        CustomerLedgerSummaryResponse response =
                new CustomerLedgerSummaryResponse();

        response.setCustomerId(
                customer.getId()
        );

        response.setCustomerName(
                customer.getName()
        );

        response.setOpeningBalance(
                openingBalance
        );

        response.setTotalSales(
                totalSales
        );

        response.setTotalPayments(
                totalPayments
        );
        
        response.setTotalSalesReturns(
                totalSalesReturns
        );

        response.setCurrentBalance(
                currentBalance
        );

        response.setEntries(
                ledgers.stream()
                        .map(this::mapToResponse)
                        .toList()
        );

        return response;
    }

    /*
     * GET CURRENT BALANCE
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(
            Long customerId,
            Long businessId) {

        List<CustomerLedger> entries =
                customerLedgerRepository
                        .findByCustomerIdAndBusinessIdOrderByCreatedAtAsc(
                                customerId,
                                businessId
                        );

        if (entries.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return entries
                .get(entries.size() - 1)
                .getBalance();
    }

    /*
     * ENTITY → RESPONSE
     */
    private CustomerLedgerResponse mapToResponse(
            CustomerLedger ledger) {

        CustomerLedgerResponse response =
                new CustomerLedgerResponse();

        response.setId(
                ledger.getId()
        );

        response.setCustomerId(
                ledger.getCustomer().getId()
        );

        response.setCustomerName(
                ledger.getCustomer().getName()
        );

        if (ledger.getInvoice() != null) {

            response.setInvoiceId(
                    ledger.getInvoice().getId()
            );

            response.setInvoiceNumber(
                    ledger.getInvoice()
                            .getInvoiceNumber()
            );
        }

        if (ledger.getPayment() != null) {

            response.setPaymentId(
                    ledger.getPayment().getId()
            );
        }
        
        if (ledger.getSalesReturn() != null) {

            response.setSalesReturnId(
                    ledger.getSalesReturn().getId()
            );

            response.setSalesReturnNumber(
                    ledger.getSalesReturn()
                            .getReturnNumber()
            );
        }

        response.setType(
                ledger.getType().name()
        );

        response.setDebit(
                ledger.getDebit()
        );

        response.setCredit(
                ledger.getCredit()
        );

        response.setBalance(
                ledger.getBalance()
        );

        response.setDescription(
                ledger.getDescription()
        );

        response.setCreatedAt(
                ledger.getCreatedAt()
        );

        return response;
    }
    public CustomerLedger createOpeningBalanceEntry(
            Business business,
            Customer customer,
            BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            return null;
        }

        BigDecimal existingBalance =
                getCurrentBalance(
                        customer.getId(),
                        business.getId()
                );

        CustomerLedger ledger =
                new CustomerLedger();

        ledger.setBusiness(business);
        ledger.setCustomer(customer);

        ledger.setType(
                CustomerLedger.LedgerType.OPENING_BALANCE
        );

        ledger.setDebit(amount);
        ledger.setCredit(BigDecimal.ZERO);

        ledger.setBalance(
                existingBalance.add(amount)
        );

        ledger.setDescription(
                "Customer opening balance"
        );

        return customerLedgerRepository.save(ledger);
    }
    /*
     * CREATE SALES RETURN LEDGER
     */
    public CustomerLedger createSalesReturnEntry(
            Business business,
            Customer customer,
            SalesReturn salesReturn) {

        if (salesReturn == null) {
            throw new IllegalArgumentException(
                    "Sales return is required"
            );
        }

        if (salesReturn.getId() == null) {
            throw new IllegalArgumentException(
                    "Sales return must be saved before creating ledger entry"
            );
        }

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer is required"
            );
        }

        if (business == null) {
            throw new IllegalArgumentException(
                    "Business is required"
            );
        }

        if (!salesReturn.getBusiness()
                .getId()
                .equals(business.getId())) {

            throw new IllegalArgumentException(
                    "Sales return does not belong to business"
            );
        }

        if (!salesReturn.getCustomer()
                .getId()
                .equals(customer.getId())) {

            throw new IllegalArgumentException(
                    "Sales return does not belong to customer"
            );
        }

        if (customerLedgerRepository
                .existsBySalesReturnId(
                        salesReturn.getId()
                )) {

            throw new IllegalArgumentException(
                    "Sales return ledger entry already exists"
            );
        }

        BigDecimal amount =
                salesReturn.getTotalAmount();

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Sales return amount must be greater than zero"
            );
        }

        BigDecimal previousBalance =
                getCurrentBalance(
                        customer.getId(),
                        business.getId()
                );

        BigDecimal newBalance =
                previousBalance.subtract(amount);

        CustomerLedger ledger =
                new CustomerLedger();

        ledger.setBusiness(business);

        ledger.setCustomer(customer);

        ledger.setType(
                CustomerLedger.LedgerType.SALES_RETURN
        );

        ledger.setInvoice(
                salesReturn.getInvoice()
        );

        ledger.setSalesReturn(
                salesReturn
        );

        ledger.setDebit(
                BigDecimal.ZERO
        );

        ledger.setCredit(
                amount
        );

        ledger.setBalance(
                newBalance
        );

        ledger.setDescription(
                "Sales return "
                        + salesReturn.getReturnNumber()
        );

        return customerLedgerRepository.save(
                ledger
        );
    }
}