package com.nexora.payment.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.customer.service.CustomerLedgerService;
import com.nexora.invoice.entity.Invoice;
import com.nexora.invoice.repository.InvoiceRepository;
import com.nexora.payment.dto.PaymentRequest;
import com.nexora.payment.dto.PaymentResponse;
import com.nexora.payment.entity.Payment;
import com.nexora.payment.exception.InvalidPaymentException;
import com.nexora.payment.exception.PaymentNotFoundException;
import com.nexora.payment.repository.PaymentRepository;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final CustomerLedgerService customerLedgerService;

    public PaymentService(
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository,
            UserRepository userRepository,
            CustomerLedgerService customerLedgerService) {

        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.customerLedgerService = customerLedgerService;
    }

    @Transactional
    public PaymentResponse createPayment(
            PaymentRequest request,
            String email) {

        User user = getUser(email);

        Business business = user.getBusiness();

        if (request.getInvoiceId() == null) {

            throw new InvalidPaymentException(
                    "Invoice ID is required"
            );
        }

        Invoice invoice =
                invoiceRepository
                        .findByIdAndBusinessId(
                                request.getInvoiceId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new InvalidPaymentException(
                                        "Invoice not found"
                                )
                        );

        if (invoice.getStatus()
                == Invoice.InvoiceStatus.CANCELLED) {

            throw new InvalidPaymentException(
                    "Cannot receive payment for cancelled invoice"
            );
        }

        if (invoice.getCustomer() == null) {

            throw new InvalidPaymentException(
                    "Invoice does not have a customer"
            );
        }

        BigDecimal amount = request.getAmount();

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidPaymentException(
                    "Payment amount must be greater than zero"
            );
        }

        BigDecimal currentDue =
                invoice.getDueAmount();

        if (amount.compareTo(currentDue) > 0) {

            throw new InvalidPaymentException(
                    "Payment cannot exceed invoice due amount"
            );
        }

        Payment payment = new Payment();

        payment.setBusiness(business);
        payment.setCustomer(invoice.getCustomer());
        payment.setInvoice(invoice);
        payment.setAmount(amount);

        payment.setPaymentMethod(
                request.getPaymentMethod()
        );

        payment.setReferenceNumber(
                request.getReferenceNumber()
        );

        payment.setNotes(
                request.getNotes()
        );

        payment.setReceivedBy(user);

        if (request.getPaymentDate() != null) {

            payment.setPaymentDate(
                    request.getPaymentDate()
            );
        }

        Payment savedPayment =
                paymentRepository.save(payment);

        BigDecimal newPaidAmount =
                invoice.getPaidAmount()
                        .add(amount);

        BigDecimal newDueAmount =
                invoice.getGrandTotal()
                        .subtract(newPaidAmount);

        invoice.setPaidAmount(newPaidAmount);
        invoice.setDueAmount(newDueAmount);

        if (newDueAmount.compareTo(
                BigDecimal.ZERO) == 0) {

            invoice.setPaymentStatus(
                    Invoice.PaymentStatus.PAID
            );

        } else {

            invoice.setPaymentStatus(
                    Invoice.PaymentStatus.PARTIAL
            );
        }

        invoiceRepository.save(invoice);

        /*
         * CUSTOMER LEDGER
         *
         * Payment = CREDIT
         */
        customerLedgerService.createPaymentEntry(
                business,
                invoice.getCustomer(),
                invoice,
                savedPayment,
                amount
        );

        return mapToResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments(
            String email) {

        User user = getUser(email);

        return paymentRepository
                .findByBusinessIdOrderByPaymentDateDesc(
                        user.getBusiness().getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getCustomerPayments(
            Long customerId,
            String email) {

        User user = getUser(email);

        return paymentRepository
                .findByCustomerIdAndBusinessIdOrderByPaymentDateDesc(
                        customerId,
                        user.getBusiness().getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getInvoicePayments(
            Long invoiceId,
            String email) {

        User user = getUser(email);

        return paymentRepository
                .findByInvoiceIdAndBusinessIdOrderByPaymentDateDesc(
                        invoiceId,
                        user.getBusiness().getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(
            Long id,
            String email) {

        User user = getUser(email);

        Payment payment =
                paymentRepository
                        .findByIdAndBusinessId(
                                id,
                                user.getBusiness().getId()
                        )
                        .orElseThrow(() ->
                                new PaymentNotFoundException(
                                        "Payment not found"
                                )
                        );

        return mapToResponse(payment);
    }

    private User getUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }

    private PaymentResponse mapToResponse(
            Payment payment) {

        PaymentResponse response =
                new PaymentResponse();

        response.setId(payment.getId());

        response.setCustomerId(
                payment.getCustomer().getId()
        );

        response.setCustomerName(
                payment.getCustomer().getName()
        );

        if (payment.getInvoice() != null) {

            response.setInvoiceId(
                    payment.getInvoice().getId()
            );

            response.setInvoiceNumber(
                    payment.getInvoice()
                            .getInvoiceNumber()
            );
        }

        response.setAmount(
                payment.getAmount()
        );

        response.setPaymentMethod(
                payment.getPaymentMethod()
        );

        response.setReferenceNumber(
                payment.getReferenceNumber()
        );

        response.setNotes(
                payment.getNotes()
        );

        response.setPaymentDate(
                payment.getPaymentDate()
        );

        response.setCreatedAt(
                payment.getCreatedAt()
        );

        return response;
    }
}