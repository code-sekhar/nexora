package com.nexora.purchase.returnmodule.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.inventory.service.InventoryService;
import com.nexora.purchase.entity.Purchase;
import com.nexora.purchase.entity.PurchaseItem;
import com.nexora.purchase.repository.PurchaseItemRepository;
import com.nexora.purchase.repository.PurchaseRepository;
import com.nexora.purchase.returnmodule.dto.PurchaseReturnItemRequest;
import com.nexora.purchase.returnmodule.dto.PurchaseReturnItemResponse;
import com.nexora.purchase.returnmodule.dto.PurchaseReturnRequest;
import com.nexora.purchase.returnmodule.dto.PurchaseReturnResponse;
import com.nexora.purchase.returnmodule.entity.PurchaseReturn;
import com.nexora.purchase.returnmodule.entity.PurchaseReturnItem;
import com.nexora.purchase.returnmodule.repository.PurchaseReturnItemRepository;
import com.nexora.purchase.returnmodule.repository.PurchaseReturnRepository;
import com.nexora.supplier.entity.Supplier;
import com.nexora.supplier.service.SupplierLedgerService;

@Service
@Transactional
public class PurchaseReturnService {

    private final PurchaseReturnRepository purchaseReturnRepository;

    private final PurchaseReturnItemRepository
            purchaseReturnItemRepository;

    private final PurchaseRepository purchaseRepository;

    private final PurchaseItemRepository
            purchaseItemRepository;

    private final InventoryService inventoryService;

    private final SupplierLedgerService
            supplierLedgerService;

    public PurchaseReturnService(
            PurchaseReturnRepository purchaseReturnRepository,
            PurchaseReturnItemRepository purchaseReturnItemRepository,
            PurchaseRepository purchaseRepository,
            PurchaseItemRepository purchaseItemRepository,
            InventoryService inventoryService,
            SupplierLedgerService supplierLedgerService) {

        this.purchaseReturnRepository =
                purchaseReturnRepository;

        this.purchaseReturnItemRepository =
                purchaseReturnItemRepository;

        this.purchaseRepository =
                purchaseRepository;

        this.purchaseItemRepository =
                purchaseItemRepository;

        this.inventoryService =
                inventoryService;

        this.supplierLedgerService =
                supplierLedgerService;
    }

    public PurchaseReturn createReturn(
            Business business,
            PurchaseReturnRequest request) {

        if (request.getPurchaseId() == null) {
            throw new IllegalArgumentException(
                    "Purchase ID is required"
            );
        }

        if (request.getItems() == null
                || request.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one return item is required"
            );
        }

        Purchase purchase =
                purchaseRepository
                        .findByIdAndBusinessId(
                                request.getPurchaseId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Purchase not found"
                                )
                        );

        if (purchase.getStatus()
                == Purchase.PurchaseStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Cannot return a cancelled purchase"
            );
        }

        Supplier supplier =
                purchase.getSupplier();

        PurchaseReturn purchaseReturn =
                new PurchaseReturn();

        purchaseReturn.setReturnNumber(
                generateReturnNumber()
        );

        purchaseReturn.setBusiness(
                business
        );

        purchaseReturn.setPurchase(
                purchase
        );

        purchaseReturn.setSupplier(
                supplier
        );

        purchaseReturn.setReturnDate(
                request.getReturnDate() != null
                        ? request.getReturnDate()
                        : LocalDate.now()
        );

        purchaseReturn.setReason(
                request.getReason()
        );

        purchaseReturn.setNotes(
                request.getNotes()
        );

        purchaseReturn.setStatus(
                PurchaseReturn.ReturnStatus.COMPLETED
        );

        BigDecimal subtotal =
                BigDecimal.ZERO;

        BigDecimal taxAmount =
                BigDecimal.ZERO;

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        for (PurchaseReturnItemRequest itemRequest
                : request.getItems()) {

            if (itemRequest.getPurchaseItemId()
                    == null) {

                throw new IllegalArgumentException(
                        "Purchase item ID is required"
                );
            }

            if (itemRequest.getQuantity() == null
                    || itemRequest.getQuantity()
                        .compareTo(BigDecimal.ZERO) <= 0) {

                throw new IllegalArgumentException(
                        "Return quantity must be greater than zero"
                );
            }

            PurchaseItem purchaseItem =
                    purchaseItemRepository
                            .findById(
                                    itemRequest
                                            .getPurchaseItemId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Purchase item not found"
                                    )
                            );

            if (!purchaseItem.getPurchase()
                    .getId()
                    .equals(purchase.getId())) {

                throw new IllegalArgumentException(
                        "Purchase item does not belong to purchase"
                );
            }

            BigDecimal alreadyReturned =
                    purchaseReturnItemRepository
                            .getReturnedQuantity(
                                    purchaseItem.getId()
                            );

            BigDecimal availableQuantity =
                    purchaseItem.getQuantity()
                            .subtract(
                                    alreadyReturned
                            );

            if (itemRequest.getQuantity()
                    .compareTo(
                            availableQuantity
                    ) > 0) {

                throw new IllegalArgumentException(
                        "Return quantity exceeds available quantity for product "
                        + purchaseItem.getProduct()
                                .getName()
                );
            }

            BigDecimal quantity =
                    itemRequest.getQuantity();

            BigDecimal unitCost =
                    purchaseItem.getUnitCost();

            BigDecimal taxableAmount =
                    unitCost.multiply(quantity);

            BigDecimal originalQuantity =
                    purchaseItem.getQuantity();

            BigDecimal originalTaxable =
                    purchaseItem.getTaxableAmount();

            BigDecimal originalTax =
                    purchaseItem.getTaxAmount();

            BigDecimal itemTax =
                    BigDecimal.ZERO;

            if (originalQuantity
                    .compareTo(BigDecimal.ZERO) > 0) {

                itemTax =
                        originalTax
                                .multiply(quantity)
                                .divide(
                                        originalQuantity,
                                        2,
                                        java.math.RoundingMode.HALF_UP
                                );
            }

            BigDecimal itemTotal =
                    taxableAmount.add(itemTax);

            PurchaseReturnItem returnItem =
                    new PurchaseReturnItem();

            returnItem.setPurchaseReturn(
                    purchaseReturn
            );

            returnItem.setPurchaseItem(
                    purchaseItem
            );

            returnItem.setProduct(
                    purchaseItem.getProduct()
            );

            returnItem.setQuantity(
                    quantity
            );

            returnItem.setUnitCost(
                    unitCost
            );

            returnItem.setTaxableAmount(
                    taxableAmount
            );

            returnItem.setTaxAmount(
                    itemTax
            );

            returnItem.setTotalAmount(
                    itemTotal
            );

            purchaseReturn.getItems()
                    .add(returnItem);

            subtotal =
                    subtotal.add(
                            taxableAmount
                    );

            taxAmount =
                    taxAmount.add(
                            itemTax
                    );

            totalAmount =
                    totalAmount.add(
                            itemTotal
                    );
        }

        purchaseReturn.setSubtotal(
                subtotal
        );

        purchaseReturn.setTaxAmount(
                taxAmount
        );

        purchaseReturn.setTotalAmount(
                totalAmount
        );

        PurchaseReturn savedReturn =
                purchaseReturnRepository.save(
                        purchaseReturn
                );

        /*
         * Inventory reduction.
         */
        for (PurchaseReturnItem item
                : savedReturn.getItems()) {

            inventoryService.removePurchaseReturnStock(
                    item.getProduct(),
                    business,
                    item.getQuantity(),
                    savedReturn.getId()
            );
        }

        /*
         * Supplier ledger credit.
         */
        supplierLedgerService.createPurchaseReturnEntry(
                business,
                supplier,
                savedReturn
        );

        return savedReturn;
    }

    private String generateReturnNumber() {

        return "PR-"
                + System.currentTimeMillis();
    }
    public PurchaseReturnResponse mapToResponse(
            PurchaseReturn purchaseReturn) {

        PurchaseReturnResponse response =
                new PurchaseReturnResponse();

        response.setId(
                purchaseReturn.getId()
        );

        response.setReturnNumber(
                purchaseReturn.getReturnNumber()
        );

        response.setPurchaseId(
                purchaseReturn.getPurchase().getId()
        );

        response.setPurchaseNumber(
                purchaseReturn.getPurchase().getPurchaseNumber()
        );

        response.setSupplierId(
                purchaseReturn.getSupplier().getId()
        );

        response.setSupplierName(
                purchaseReturn.getSupplier().getName()
        );

        response.setReturnDate(
                purchaseReturn.getReturnDate()
        );

        response.setSubtotal(
                purchaseReturn.getSubtotal()
        );

        response.setTaxAmount(
                purchaseReturn.getTaxAmount()
        );

        response.setTotalAmount(
                purchaseReturn.getTotalAmount()
        );

        response.setStatus(
                purchaseReturn.getStatus().name()
        );

        response.setReason(
                purchaseReturn.getReason()
        );

        response.setNotes(
                purchaseReturn.getNotes()
        );

        List<PurchaseReturnItemResponse> items =
                purchaseReturn.getItems()
                        .stream()
                        .map(item -> {

                            PurchaseReturnItemResponse itemResponse =
                                    new PurchaseReturnItemResponse();

                            itemResponse.setId(
                                    item.getId()
                            );

                            itemResponse.setPurchaseItemId(
                                    item.getPurchaseItem().getId()
                            );

                            itemResponse.setProductId(
                                    item.getProduct().getId()
                            );

                            itemResponse.setProductName(
                                    item.getProduct().getName()
                            );

                            itemResponse.setSku(
                                    item.getProduct().getSku()
                            );

                            itemResponse.setQuantity(
                                    item.getQuantity()
                            );

                            itemResponse.setUnitCost(
                                    item.getUnitCost()
                            );

                            itemResponse.setTaxableAmount(
                                    item.getTaxableAmount()
                            );

                            itemResponse.setTaxAmount(
                                    item.getTaxAmount()
                            );

                            itemResponse.setTotalAmount(
                                    item.getTotalAmount()
                            );

                            return itemResponse;
                        })
                        .toList();

        response.setItems(items);

        return response;
    }
}