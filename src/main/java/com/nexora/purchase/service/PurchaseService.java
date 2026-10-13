package com.nexora.purchase.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.inventory.service.InventoryService;
import com.nexora.product.entity.Product;
import com.nexora.product.repository.ProductRepository;
import com.nexora.purchase.dto.PurchaseItemRequest;
import com.nexora.purchase.dto.PurchaseItemResponse;
import com.nexora.purchase.dto.PurchaseRequest;
import com.nexora.purchase.dto.PurchaseResponse;
import com.nexora.purchase.entity.Purchase;
import com.nexora.purchase.entity.PurchaseItem;
import com.nexora.purchase.exception.InvalidPurchaseException;
import com.nexora.purchase.exception.PurchaseNotFoundException;
import com.nexora.purchase.repository.PurchaseItemRepository;
import com.nexora.purchase.repository.PurchaseRepository;
import com.nexora.supplier.entity.Supplier;
import com.nexora.supplier.repository.SupplierRepository;
import com.nexora.supplier.service.SupplierLedgerService;
import com.nexora.tax.entity.TaxRate;
import com.nexora.tax.repository.TaxRateRepository;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final TaxRateRepository taxRateRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final SupplierLedgerService supplierLedgerService;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            PurchaseItemRepository purchaseItemRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            TaxRateRepository taxRateRepository,
            UserRepository userRepository,
            InventoryService inventoryService,
            SupplierLedgerService supplierLedgerService) {

        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.taxRateRepository = taxRateRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
        this.supplierLedgerService = supplierLedgerService;
    }

    private User getCurrentUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    private Business getCurrentBusiness(String email) {

        User user = getCurrentUser(email);

        if (user.getBusiness() == null) {
            throw new RuntimeException(
                    "User is not assigned to any business");
        }

        return user.getBusiness();
    }

    @Transactional
    public PurchaseResponse createPurchase(
            PurchaseRequest request,
            String email) {

        Business business =
                getCurrentBusiness(email);

        Supplier supplier =
                supplierRepository
                        .findByIdAndBusinessId(
                                request.getSupplierId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new InvalidPurchaseException(
                                        "Supplier not found"));

        if (!supplier.isActive()) {
            throw new InvalidPurchaseException(
                    "Cannot create purchase for inactive supplier");
        }

        BigDecimal paidAmount =
                request.getPaidAmount() == null
                        ? BigDecimal.ZERO
                        : request.getPaidAmount();

        if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPurchaseException(
                    "Paid amount cannot be negative");
        }

        Purchase purchase = new Purchase();

        purchase.setPurchaseNumber(
                generatePurchaseNumber());

        purchase.setSupplierInvoiceNumber(
                request.getSupplierInvoiceNumber());

        purchase.setSupplier(supplier);
        purchase.setBusiness(business);

        purchase.setPurchaseDate(
                request.getPurchaseDate() == null
                        ? LocalDate.now()
                        : request.getPurchaseDate());

        purchase.setStatus(
                Purchase.PurchaseStatus.RECEIVED);

        Purchase savedPurchase =
                purchaseRepository.save(purchase);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (PurchaseItemRequest itemRequest :
                request.getItems()) {

            Product product =
                    productRepository
                            .findByIdAndBusinessId(
                                    itemRequest.getProductId(),
                                    business.getId()
                            )
                            .orElseThrow(() ->
                                    new InvalidPurchaseException(
                                            "Product not found: "
                                                    + itemRequest.getProductId()));

            if (!product.isActive()) {
                throw new InvalidPurchaseException(
                        "Cannot purchase inactive product: "
                                + product.getName());
            }

            BigDecimal quantity =
                    itemRequest.getQuantity();

            BigDecimal unitCost =
                    itemRequest.getUnitCost();

            BigDecimal discount =
                    itemRequest.getDiscountAmount() == null
                            ? BigDecimal.ZERO
                            : itemRequest.getDiscountAmount();

            if (quantity == null ||
                    quantity.compareTo(BigDecimal.ZERO) <= 0) {

                throw new InvalidPurchaseException(
                        "Quantity must be greater than zero");
            }

            if (unitCost == null ||
                    unitCost.compareTo(BigDecimal.ZERO) < 0) {

                throw new InvalidPurchaseException(
                        "Unit cost cannot be negative");
            }

            BigDecimal grossAmount =
                    quantity.multiply(unitCost);

            if (discount.compareTo(grossAmount) > 0) {
                throw new InvalidPurchaseException(
                        "Discount cannot be greater than item amount");
            }

            BigDecimal taxableAmount =
                    grossAmount.subtract(discount);

            BigDecimal taxPercent =
                    getTaxPercentage(product);

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

            PurchaseItem item =
                    new PurchaseItem();

            item.setPurchase(savedPurchase);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitCost(unitCost);
            item.setGrossAmount(grossAmount);
            item.setDiscountAmount(discount);
            item.setTaxableAmount(taxableAmount);
            item.setTaxAmount(taxAmount);
            item.setTotalAmount(totalAmount);

            purchaseItemRepository.save(item);

            subtotal =
                    subtotal.add(grossAmount);

            totalDiscount =
                    totalDiscount.add(discount);

            totalTax =
                    totalTax.add(taxAmount);

            grandTotal =
                    grandTotal.add(totalAmount);

            inventoryService.addPurchaseStock(
                    product,
                    business,
                    quantity,
                    savedPurchase.getId()
            );
        }

        if (paidAmount.compareTo(grandTotal) > 0) {
            throw new InvalidPurchaseException(
                    "Paid amount cannot be greater than purchase total");
        }

        BigDecimal dueAmount =
                grandTotal.subtract(paidAmount);

        savedPurchase.setSubtotal(subtotal);
        savedPurchase.setDiscountAmount(totalDiscount);
        savedPurchase.setTaxAmount(totalTax);
        savedPurchase.setGrandTotal(grandTotal);
        savedPurchase.setPaidAmount(paidAmount);
        savedPurchase.setDueAmount(dueAmount);

        Purchase finalPurchase =
                purchaseRepository.save(savedPurchase);

        /*
         * Create supplier ledger purchase entry.
         *
         * paidAmount is NOT treated as supplier payment here.
         * Actual supplier payments are handled by SupplierPayment.
         */
        supplierLedgerService.createPurchaseEntry(
                business,
                supplier,
                finalPurchase
        );

        return mapToResponse(finalPurchase);
    }

    private BigDecimal getTaxPercentage(
            Product product) {

        if (product.getTaxProfile() == null) {
            return BigDecimal.ZERO;
        }

        List<TaxRate> rates =
                taxRateRepository
                        .findByTaxProfileId(
                                product.getTaxProfile().getId()
                        );

        return rates.stream()
                .filter(TaxRate::isActive)
                .map(TaxRate::getRate)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getAllPurchases(
            String email) {

        Business business =
                getCurrentBusiness(email);

        return purchaseRepository
                .findByBusinessIdOrderByCreatedAtDesc(
                        business.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getPurchaseById(
            Long id,
            String email) {

        Business business =
                getCurrentBusiness(email);

        Purchase purchase =
                purchaseRepository
                        .findByIdAndBusinessId(
                                id,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new PurchaseNotFoundException(
                                        "Purchase not found"));

        return mapToResponse(purchase);
    }

    private String generatePurchaseNumber() {

        return "PUR-"
                + System.currentTimeMillis()
                + "-"
                + UUID.randomUUID()
                        .toString()
                        .substring(0, 4)
                        .toUpperCase();
    }

    private PurchaseResponse mapToResponse(
            Purchase purchase) {

        PurchaseResponse response =
                new PurchaseResponse();

        response.setId(purchase.getId());

        response.setPurchaseNumber(
                purchase.getPurchaseNumber());

        response.setSupplierInvoiceNumber(
                purchase.getSupplierInvoiceNumber());

        response.setSupplierId(
                purchase.getSupplier().getId());

        response.setSupplierName(
                purchase.getSupplier().getName());

        response.setPurchaseDate(
                purchase.getPurchaseDate());

        response.setSubtotal(
                purchase.getSubtotal());

        response.setDiscountAmount(
                purchase.getDiscountAmount());

        response.setTaxAmount(
                purchase.getTaxAmount());

        response.setGrandTotal(
                purchase.getGrandTotal());

        response.setPaidAmount(
                purchase.getPaidAmount());

        response.setDueAmount(
                purchase.getDueAmount());

        response.setStatus(
                purchase.getStatus());

        response.setCreatedAt(
                purchase.getCreatedAt());

        response.setUpdatedAt(
                purchase.getUpdatedAt());

        List<PurchaseItemResponse> items =
                purchaseItemRepository
                        .findByPurchaseId(
                                purchase.getId()
                        )
                        .stream()
                        .map(this::mapItemToResponse)
                        .toList();

        response.setItems(items);

        return response;
    }

    private PurchaseItemResponse mapItemToResponse(
            PurchaseItem item) {

        PurchaseItemResponse response =
                new PurchaseItemResponse();

        response.setId(item.getId());

        response.setProductId(
                item.getProduct().getId());

        response.setProductName(
                item.getProduct().getName());

        response.setSku(
                item.getProduct().getSku());

        response.setQuantity(
                item.getQuantity());

        response.setUnitCost(
                item.getUnitCost());

        response.setGrossAmount(
                item.getGrossAmount());

        response.setDiscountAmount(
                item.getDiscountAmount());

        response.setTaxableAmount(
                item.getTaxableAmount());

        response.setTaxAmount(
                item.getTaxAmount());

        response.setTotalAmount(
                item.getTotalAmount());

        return response;
    }

    @Transactional
    public PurchaseResponse cancelPurchase(
            Long purchaseId,
            String email) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new InvalidPurchaseException(
                                        "User not found"));

        Business business =
                user.getBusiness();

        if (business == null) {
            throw new InvalidPurchaseException(
                    "User is not assigned to any business");
        }

        Purchase purchase =
                purchaseRepository
                        .findByIdAndBusinessId(
                                purchaseId,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new PurchaseNotFoundException(
                                        "Purchase not found"));

        /*
         * Already cancelled
         */
        if (purchase.getStatus()
                == Purchase.PurchaseStatus.CANCELLED) {

            throw new InvalidPurchaseException(
                    "Purchase is already cancelled");
        }

        /*
         * Get purchase items
         */
        List<PurchaseItem> items =
                purchaseItemRepository
                        .findByPurchaseId(
                                purchase.getId()
                        );

        /*
         * Reverse inventory.
         */
        for (PurchaseItem item : items) {

            Product product =
                    item.getProduct();

            inventoryService.removePurchaseStock(
                    product,
                    business,
                    item.getQuantity(),
                    purchase.getId()
            );
        }

        /*
         * Change purchase status.
         */
        purchase.setStatus(
                Purchase.PurchaseStatus.CANCELLED
        );

        Purchase savedPurchase =
                purchaseRepository.save(purchase);

        /*
         * Reverse supplier payable because
         * the complete purchase has been cancelled.
         */
        supplierLedgerService.createPurchaseCancellationEntry(
                business,
                purchase.getSupplier(),
                savedPurchase
        );

        return mapToResponse(savedPurchase);
    }
}