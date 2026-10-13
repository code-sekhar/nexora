package com.nexora.invoice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.customer.entity.Customer;
import com.nexora.customer.repository.CustomerRepository;
import com.nexora.invoice.dto.InvoiceItemRequest;
import com.nexora.invoice.dto.InvoiceItemResponse;
import com.nexora.invoice.dto.InvoiceRequest;
import com.nexora.invoice.dto.InvoiceResponse;
import com.nexora.invoice.entity.Invoice;
import com.nexora.invoice.entity.InvoiceItem;
import com.nexora.invoice.exception.InvalidInvoiceException;
import com.nexora.invoice.exception.InvoiceNotFoundException;
import com.nexora.invoice.repository.InvoiceRepository;
import com.nexora.inventory.service.InventoryService;
import com.nexora.product.entity.Product;
import com.nexora.product.repository.ProductRepository;
import com.nexora.tax.entity.TaxRate;
import com.nexora.tax.entity.TaxProfile;
import com.nexora.tax.repository.TaxRateRepository;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

import com.nexora.customer.entity.CustomerLedger;
import com.nexora.customer.service.CustomerLedgerService;
import com.nexora.payment.entity.Payment;
import com.nexora.payment.entity.PaymentMethod;
import com.nexora.payment.repository.PaymentRepository;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final TaxRateRepository taxRateRepository;
    private final InventoryService inventoryService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerLedgerService customerLedgerService;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            TaxRateRepository taxRateRepository,
            InventoryService inventoryService,
            UserRepository userRepository,
            PaymentRepository paymentRepository,
            CustomerLedgerService customerLedgerService) {

        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.taxRateRepository = taxRateRepository;
        this.inventoryService = inventoryService;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.customerLedgerService = customerLedgerService;
    }

    @Transactional
    public InvoiceResponse createInvoice(
            InvoiceRequest request,
            String email) {

        User user = getUser(email);

        Business business = user.getBusiness();

        Long businessId = business.getId();

        Customer customer = null;

        if (request.getCustomerId() != null) {

            customer = customerRepository
                    .findByIdAndBusinessId(
                            request.getCustomerId(),
                            businessId
                    )
                    .orElseThrow(() ->
                            new InvalidInvoiceException(
                                    "Customer not found"
                            )
                    );

            if (!customer.isActive()) {
                throw new InvalidInvoiceException(
                        "Customer is inactive"
                );
            }
        }

        BigDecimal invoiceDiscount =
                request.getDiscountAmount() != null
                        ? request.getDiscountAmount()
                        : BigDecimal.ZERO;

        if (invoiceDiscount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInvoiceException(
                    "Discount cannot be negative"
            );
        }

        Invoice invoice = new Invoice();

        invoice.setBusiness(business);
        invoice.setCustomer(customer);

        invoice.setInvoiceDate(
                request.getInvoiceDate() != null
                        ? request.getInvoiceDate()
                        : LocalDate.now()
        );

        invoice.setInvoiceNumber(
                generateInvoiceNumber(businessId)
        );

        invoice.setDiscountAmount(invoiceDiscount);

        invoice.setNotes(request.getNotes());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (InvoiceItemRequest itemRequest :
                request.getItems()) {

            Product product = productRepository
                    .findByIdAndBusinessId(
                            itemRequest.getProductId(),
                            businessId
                    )
                    .orElseThrow(() ->
                            new InvalidInvoiceException(
                                    "Product not found: "
                                    + itemRequest.getProductId()
                            )
                    );

            if (!product.isActive()) {
                throw new InvalidInvoiceException(
                        "Product is inactive: "
                        + product.getName()
                );
            }

            BigDecimal quantity =
                    itemRequest.getQuantity();

            if (quantity == null ||
                    quantity.compareTo(BigDecimal.ZERO) <= 0) {

                throw new InvalidInvoiceException(
                        "Quantity must be greater than zero"
                );
            }

            BigDecimal unitPrice =
                    itemRequest.getUnitPrice() != null
                            ? itemRequest.getUnitPrice()
                            : product.getSellingPrice();

            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidInvoiceException(
                        "Unit price cannot be negative"
                );
            }

            BigDecimal itemDiscount =
                    itemRequest.getDiscountAmount() != null
                            ? itemRequest.getDiscountAmount()
                            : BigDecimal.ZERO;

            BigDecimal grossAmount =
                    quantity.multiply(unitPrice);

            if (itemDiscount.compareTo(grossAmount) > 0) {
                throw new InvalidInvoiceException(
                        "Item discount cannot exceed item amount"
                );
            }

            BigDecimal taxableAmount =
                    grossAmount.subtract(itemDiscount);

            BigDecimal taxPercent =
                    getTaxPercent(
                            product.getTaxProfile()
                    );

            BigDecimal taxAmount =
                    taxableAmount
                            .multiply(taxPercent)
                            .divide(
                                    BigDecimal.valueOf(100),
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal totalAmount =
                    taxableAmount.add(taxAmount);

            InvoiceItem invoiceItem =
                    new InvoiceItem();

            invoiceItem.setInvoice(invoice);
            invoiceItem.setProduct(product);
            invoiceItem.setQuantity(quantity);
            invoiceItem.setUnitPrice(unitPrice);
            invoiceItem.setGrossAmount(grossAmount);
            invoiceItem.setDiscountAmount(itemDiscount);
            invoiceItem.setTaxableAmount(taxableAmount);
            invoiceItem.setTaxAmount(taxAmount);
            invoiceItem.setTotalAmount(totalAmount);

            invoice.getItems().add(invoiceItem);

            subtotal = subtotal.add(grossAmount);
            totalTax = totalTax.add(taxAmount);
        }

        if (invoice.getItems().isEmpty()) {
            throw new InvalidInvoiceException(
                    "Invoice must contain at least one item"
            );
        }

//        BigDecimal totalDiscount =
//                invoiceDiscount;
//        BigDecimal totalItemDiscount = BigDecimal.ZERO;
//
//        for (InvoiceItem item : invoice.getItems()) {
//            totalDiscount =
//                    totalDiscount.add(
//                            item.getDiscountAmount()
//                    );
//        }
//
//        BigDecimal taxableSubtotal =
//                subtotal.subtract(totalDiscount);
        BigDecimal totalItemDiscount = BigDecimal.ZERO;

        for (InvoiceItem item : invoice.getItems()) {
            totalItemDiscount = totalItemDiscount.add(
                    item.getDiscountAmount()
            );
        }

        BigDecimal totalDiscount =
                invoiceDiscount.add(totalItemDiscount);

        BigDecimal taxableSubtotal =
                subtotal.subtract(totalDiscount);
        
        

        System.out.println("========== INVOICE DISCOUNT DEBUG ==========");
        System.out.println("Invoice Discount: " + invoiceDiscount);
        System.out.println("Item Discount: " + totalItemDiscount);
        System.out.println("Total Discount: " + totalDiscount);
        System.out.println("Subtotal: " + subtotal);
        System.out.println("Tax: " + totalTax);
        System.out.println("============================================");

        if (taxableSubtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInvoiceException(
                    "Total discount cannot exceed subtotal"
            );
        }

        BigDecimal grandTotal =
                taxableSubtotal.add(totalTax);

        BigDecimal paidAmount =
                request.getPaidAmount() != null
                        ? request.getPaidAmount()
                        : BigDecimal.ZERO;

        if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInvoiceException(
                    "Paid amount cannot be negative"
            );
        }

        if (paidAmount.compareTo(grandTotal) > 0) {
            throw new InvalidInvoiceException(
                    "Paid amount cannot exceed invoice total"
            );
        }

        BigDecimal dueAmount =
                grandTotal.subtract(paidAmount);

        Invoice.PaymentStatus paymentStatus;

        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {

            paymentStatus =
                    Invoice.PaymentStatus.UNPAID;

        } else if (paidAmount.compareTo(grandTotal) == 0) {

            paymentStatus =
                    Invoice.PaymentStatus.PAID;

        } else {

            paymentStatus =
                    Invoice.PaymentStatus.PARTIAL;
        }

        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(totalDiscount);
        invoice.setTaxAmount(totalTax);
        invoice.setGrandTotal(grandTotal);
        invoice.setPaidAmount(paidAmount);
        invoice.setDueAmount(dueAmount);
        invoice.setPaymentStatus(paymentStatus);
        invoice.setStatus(
                Invoice.InvoiceStatus.COMPLETED
        );

        Invoice savedInvoice =
                invoiceRepository.save(invoice);

        /*
         * Reduce inventory after invoice creation.
         */
        for (InvoiceItem item :
                savedInvoice.getItems()) {

            inventoryService.removeSaleStock(
                    item.getProduct(),
                    business,
                    item.getQuantity(),
                    savedInvoice.getId()
            );
        }

        /*
         * CUSTOMER LEDGER
         *
         * Create SALE entry only when invoice
         * has a customer.
         */
        if (customer != null) {

            customerLedgerService.createSaleEntry(
                    business,
                    customer,
                    savedInvoice,
                    grandTotal
            );

            /*
             * INITIAL PAYMENT
             *
             * If customer paid something while
             * creating the invoice, create an
             * actual Payment record.
             */
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {

                Payment initialPayment =
                        new Payment();

                initialPayment.setBusiness(business);
                initialPayment.setCustomer(customer);
                initialPayment.setInvoice(savedInvoice);

                initialPayment.setAmount(paidAmount);

                /*
                 * Default method for payment
                 * received during invoice creation.
                 *
                 * Later frontend can explicitly
                 * pass payment method.
                 */
                initialPayment.setPaymentMethod(
                        PaymentMethod.CASH
                );

                initialPayment.setReferenceNumber(
                        "INITIAL-" +
                        savedInvoice.getInvoiceNumber()
                );

                initialPayment.setNotes(
                        "Initial payment received with invoice"
                );

                initialPayment.setReceivedBy(user);

                Payment savedInitialPayment =
                        paymentRepository.save(
                                initialPayment
                        );

                /*
                 * CUSTOMER PAYMENT LEDGER
                 */
                customerLedgerService.createPaymentEntry(
                        business,
                        customer,
                        savedInvoice,
                        savedInitialPayment,
                        paidAmount
                );
            }
        }

        return mapToResponse(savedInvoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices(
            String email) {

        User user = getUser(email);

        Long businessId =
                user.getBusiness().getId();

        return invoiceRepository
                .findByBusinessIdOrderByCreatedAtDesc(
                        businessId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(
            Long id,
            String email) {

        User user = getUser(email);

        Long businessId =
                user.getBusiness().getId();

        Invoice invoice =
                invoiceRepository
                        .findByIdAndBusinessId(
                                id,
                                businessId
                        )
                        .orElseThrow(() ->
                                new InvoiceNotFoundException(
                                        "Invoice not found"
                                )
                        );

        return mapToResponse(invoice);
    }

    @Transactional
    public InvoiceResponse cancelInvoice(
            Long id,
            String email) {

        User user = getUser(email);

        Business business =
                user.getBusiness();

        Long businessId =
                business.getId();

        Invoice invoice =
                invoiceRepository
                        .findByIdAndBusinessId(
                                id,
                                businessId
                        )
                        .orElseThrow(() ->
                                new InvoiceNotFoundException(
                                        "Invoice not found"
                                )
                        );

        if (invoice.getStatus()
                == Invoice.InvoiceStatus.CANCELLED) {

            throw new InvalidInvoiceException(
                    "Invoice is already cancelled"
            );
        }

        for (InvoiceItem item :
                invoice.getItems()) {

            inventoryService.addSaleReturnStock(
                    item.getProduct(),
                    business,
                    item.getQuantity(),
                    invoice.getId()
            );
        }

        invoice.setStatus(
                Invoice.InvoiceStatus.CANCELLED
        );

        Invoice updatedInvoice =
                invoiceRepository.save(invoice);

        return mapToResponse(updatedInvoice);
    }

    private BigDecimal getTaxPercent(
            TaxProfile taxProfile) {

        if (taxProfile == null) {
            return BigDecimal.ZERO;
        }

        return taxRateRepository
                .findByTaxProfileId(
                        taxProfile.getId()
                )
                .stream()
                .filter(TaxRate::isActive)
                .map(TaxRate::getRate)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private String generateInvoiceNumber(
            Long businessId) {

        long count =
                invoiceRepository
                        .findByBusinessIdOrderByCreatedAtDesc(
                                businessId
                        )
                        .size();

        return String.format(
                "INV-%06d",
                count + 1
        );
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

    private InvoiceResponse mapToResponse(
            Invoice invoice) {

        InvoiceResponse response =
                new InvoiceResponse();

        response.setId(invoice.getId());
        response.setInvoiceNumber(
                invoice.getInvoiceNumber()
        );

        if (invoice.getCustomer() != null) {

            response.setCustomerId(
                    invoice.getCustomer().getId()
            );

            response.setCustomerName(
                    invoice.getCustomer().getName()
            );

            response.setCustomerPhone(
                    invoice.getCustomer().getPhone()
            );
        }

        response.setInvoiceDate(
                invoice.getInvoiceDate()
        );

        response.setSubtotal(
                invoice.getSubtotal()
        );

        response.setDiscountAmount(
                invoice.getDiscountAmount()
        );

        response.setTaxAmount(
                invoice.getTaxAmount()
        );

        response.setGrandTotal(
                invoice.getGrandTotal()
        );

        response.setPaidAmount(
                invoice.getPaidAmount()
        );

        response.setDueAmount(
                invoice.getDueAmount()
        );

        response.setStatus(
                invoice.getStatus()
        );

        response.setPaymentStatus(
                invoice.getPaymentStatus()
        );

        response.setNotes(
                invoice.getNotes()
        );

        response.setCreatedAt(
                invoice.getCreatedAt()
        );

        response.setUpdatedAt(
                invoice.getUpdatedAt()
        );

        List<InvoiceItemResponse> itemResponses =
                invoice.getItems()
                        .stream()
                        .map(this::mapItemToResponse)
                        .toList();

        response.setItems(itemResponses);

        return response;
    }

    private InvoiceItemResponse mapItemToResponse(
            InvoiceItem item) {

        InvoiceItemResponse response =
                new InvoiceItemResponse();

        response.setId(item.getId());

        response.setProductId(
                item.getProduct().getId()
        );

        response.setProductName(
                item.getProduct().getName()
        );

        response.setSku(
                item.getProduct().getSku()
        );

        response.setQuantity(
                item.getQuantity()
        );

        response.setUnitPrice(
                item.getUnitPrice()
        );

        response.setGrossAmount(
                item.getGrossAmount()
        );

        response.setDiscountAmount(
                item.getDiscountAmount()
        );

        response.setTaxableAmount(
                item.getTaxableAmount()
        );

        response.setTaxAmount(
                item.getTaxAmount()
        );

        response.setTotalAmount(
                item.getTotalAmount()
        );

        return response;
    }

    
}