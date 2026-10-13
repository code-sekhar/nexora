package com.nexora.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.exception.ResourceNotFoundException;
import com.nexora.inventory.dto.InventoryResponse;
import com.nexora.inventory.dto.StockAdjustmentRequest;
import com.nexora.inventory.dto.StockMovementResponse;
import com.nexora.inventory.entity.Inventory;
import com.nexora.inventory.entity.StockMovement;
import com.nexora.inventory.repository.InventoryRepository;
import com.nexora.inventory.repository.StockMovementRepository;
import com.nexora.product.entity.Product;
import com.nexora.product.repository.ProductRepository;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // GET ALL INVENTORY
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory(
            String email) {

        User user = getLoggedInUser(email);
        Business business = getUserBusiness(user);

        return inventoryRepository
                .findByBusinessId(business.getId())
                .stream()
                .map(this::mapInventory)
                .toList();
    }

    // GET PRODUCT INVENTORY
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProduct(
            Long productId,
            String email) {

        User user = getLoggedInUser(email);
        Business business = getUserBusiness(user);

        Inventory inventory = inventoryRepository
                .findByProductIdAndBusinessId(
                        productId,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found"));

        return mapInventory(inventory);
    }

    // ADJUST STOCK
    public InventoryResponse adjustStock(
            Long productId,
            StockAdjustmentRequest request,
            String email) {

        User user = getLoggedInUser(email);
        Business business = getUserBusiness(user);

        Product product = productRepository
                .findByIdAndBusinessId(
                        productId,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"));

        if (!product.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot adjust stock for inactive product");
        }

        Inventory inventory = inventoryRepository
                .findByProductIdAndBusinessId(
                        productId,
                        business.getId())
                .orElseGet(() -> {

                    Inventory newInventory =
                            new Inventory();

                    newInventory.setProduct(product);
                    newInventory.setBusiness(business);
                    newInventory.setQuantity(
                            BigDecimal.ZERO);
                    newInventory.setReservedQuantity(
                            BigDecimal.ZERO);

                    LocalDateTime now =
                            LocalDateTime.now();

                    newInventory.setCreatedAt(now);
                    newInventory.setUpdatedAt(now);

                    return newInventory;
                });

        BigDecimal quantityBefore =
                inventory.getQuantity();

        BigDecimal adjustment =
                request.getQuantity();

        BigDecimal quantityAfter;

        StockMovement.MovementType movementType;

        switch (request.getType()) {

            case OPENING:

                if (quantityBefore.compareTo(
                        BigDecimal.ZERO) != 0) {

                    throw new IllegalArgumentException(
                            "Opening stock can only be added when current stock is zero");
                }

                quantityAfter =
                        quantityBefore.add(adjustment);

                movementType =
                        StockMovement.MovementType.OPENING;

                break;

            case ADJUSTMENT_IN:

                quantityAfter =
                        quantityBefore.add(adjustment);

                movementType =
                        StockMovement.MovementType.ADJUSTMENT_IN;

                break;

            case ADJUSTMENT_OUT:

                quantityAfter =
                        quantityBefore.subtract(adjustment);

                movementType =
                        StockMovement.MovementType.ADJUSTMENT_OUT;

                break;

            case DAMAGE:

                quantityAfter =
                        quantityBefore.subtract(adjustment);

                movementType =
                        StockMovement.MovementType.DAMAGE;

                break;

            default:

                throw new IllegalArgumentException(
                        "Invalid stock adjustment type");
        }

        if (quantityAfter.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Insufficient stock");
        }

        BigDecimal reservedQuantity =
                inventory.getReservedQuantity();

        if (quantityAfter.compareTo(
                reservedQuantity) < 0) {

            throw new IllegalArgumentException(
                    "Stock cannot be lower than reserved quantity");
        }

        inventory.setQuantity(quantityAfter);
        inventory.setUpdatedAt(LocalDateTime.now());

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        StockMovement movement =
                new StockMovement();

        movement.setProduct(product);
        movement.setBusiness(business);
        movement.setType(movementType);
        movement.setQuantity(adjustment);
        movement.setQuantityBefore(quantityBefore);
        movement.setQuantityAfter(quantityAfter);
        movement.setReferenceType("MANUAL");
        movement.setReason(request.getReason());
        movement.setCreatedAt(LocalDateTime.now());

        stockMovementRepository.save(movement);

        return mapInventory(savedInventory);
    }

    // GET STOCK MOVEMENTS
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovements(
            Long productId,
            String email) {

        User user = getLoggedInUser(email);
        Business business = getUserBusiness(user);

        productRepository
                .findByIdAndBusinessId(
                        productId,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"));

        return stockMovementRepository
                .findByProductIdAndBusinessIdOrderByCreatedAtDesc(
                        productId,
                        business.getId())
                .stream()
                .map(this::mapMovement)
                .toList();
    }

    // ENTITY → INVENTORY RESPONSE
    private InventoryResponse mapInventory(
            Inventory inventory) {

        Product product =
                inventory.getProduct();

        BigDecimal quantity =
                inventory.getQuantity();

        BigDecimal reserved =
                inventory.getReservedQuantity();

        BigDecimal available =
                quantity.subtract(reserved);

        Integer minimumStock =
                product.getMinimumStock();

        boolean lowStock =
                available.compareTo(
                        BigDecimal.valueOf(
                                minimumStock)) <= 0;

        return new InventoryResponse(
                inventory.getId(),
                product.getId(),
                product.getName(),
                product.getSku(),
                quantity,
                reserved,
                available,
                minimumStock,
                lowStock,
                inventory.getUpdatedAt()
        );
    }

    // ENTITY → MOVEMENT RESPONSE
    private StockMovementResponse mapMovement(
            StockMovement movement) {

        Product product =
                movement.getProduct();

        return new StockMovementResponse(
                movement.getId(),
                product.getId(),
                product.getName(),
                movement.getType().name(),
                movement.getQuantity(),
                movement.getQuantityBefore(),
                movement.getQuantityAfter(),
                movement.getReferenceType(),
                movement.getReferenceId(),
                movement.getReason(),
                movement.getCreatedAt()
        );
    }

    // LOGGED-IN USER
    private User getLoggedInUser(
            String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));
    }

    // USER BUSINESS
    private Business getUserBusiness(
            User user) {

        if (user.getBusiness() == null) {
            throw new ResourceNotFoundException(
                    "Business not assigned to user");
        }

        return user.getBusiness();
    }
    
    @Transactional
    public void addPurchaseStock(
            Product product,
            Business business,
            BigDecimal quantity,
            Long purchaseId) {

        Inventory inventory =
            inventoryRepository
                .findByProductIdAndBusinessId(
                    product.getId(),
                    business.getId()
                )
                .orElse(null);

        if (inventory == null) {

            inventory = new Inventory();

            inventory.setProduct(product);
            inventory.setBusiness(business);
            inventory.setQuantity(BigDecimal.ZERO);
            inventory.setReservedQuantity(BigDecimal.ZERO);
            inventory.setCreatedAt(LocalDateTime.now());
        }

        BigDecimal before = inventory.getQuantity();

        BigDecimal after =
            before.add(quantity);

        inventory.setQuantity(after);
        inventory.setUpdatedAt(LocalDateTime.now());

        inventoryRepository.save(inventory);


        StockMovement movement =
            new StockMovement();

        movement.setProduct(product);
        movement.setBusiness(business);

        movement.setType(
            StockMovement.MovementType.PURCHASE
        );

        movement.setQuantity(quantity);
        movement.setQuantityBefore(before);
        movement.setQuantityAfter(after);

        movement.setReferenceType("PURCHASE");
        movement.setReferenceId(purchaseId);
        movement.setReason("Purchase received");

        movement.setCreatedAt(LocalDateTime.now());

        stockMovementRepository.save(movement);
    }
    @Transactional
    public void removePurchaseStock(
            Product product,
            Business business,
            BigDecimal quantity,
            Long purchaseId
    ) {

        Inventory inventory =
                inventoryRepository
                        .findByProductIdAndBusinessId(
                                product.getId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Inventory not found"
                                )
                        );

        BigDecimal before =
                inventory.getQuantity();

        BigDecimal after =
                before.subtract(quantity);

        // Cannot make stock negative
        if (after.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "Cannot cancel purchase because stock is insufficient"
            );
        }

        // Cannot go below reserved stock
        if (after.compareTo(
                inventory.getReservedQuantity()
        ) < 0) {

            throw new RuntimeException(
                    "Cannot cancel purchase because stock is reserved"
            );
        }

        inventory.setQuantity(after);
        inventory.setUpdatedAt(LocalDateTime.now());

        inventoryRepository.save(inventory);

        // Stock movement
        StockMovement movement =
                new StockMovement();

        movement.setProduct(product);
        movement.setBusiness(business);

        movement.setType(
                StockMovement.MovementType.PURCHASE_RETURN
        );

        movement.setQuantity(quantity);
        movement.setQuantityBefore(before);
        movement.setQuantityAfter(after);

        movement.setReferenceType("PURCHASE");
        movement.setReferenceId(purchaseId);

        movement.setReason(
                "Purchase cancelled"
        );

        movement.setCreatedAt(
                LocalDateTime.now()
        );

        stockMovementRepository.save(movement);
    }
    @Transactional
    public void removeSaleStock(
            Product product,
            Business business,
            BigDecimal quantity,
            Long invoiceId) {

        Inventory inventory =
                inventoryRepository
                        .findByProductIdAndBusinessId(
                                product.getId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Inventory not found"
                                )
                        );

        BigDecimal quantityBefore =
                inventory.getQuantity();

        BigDecimal availableStock =
                quantityBefore.subtract(
                        inventory.getReservedQuantity()
                );

        if (availableStock.compareTo(quantity) < 0) {

            throw new RuntimeException(
                    "Insufficient stock for product: "
                            + product.getName()
            );
        }

        BigDecimal quantityAfter =
                quantityBefore.subtract(quantity);

        inventory.setQuantity(quantityAfter);

        inventoryRepository.save(inventory);

        StockMovement movement =
                new StockMovement();

        movement.setProduct(product);
        movement.setBusiness(business);

        movement.setQuantity(quantity);

        movement.setQuantityBefore(
                quantityBefore
        );

        movement.setQuantityAfter(
                quantityAfter
        );

        movement.setType(
                StockMovement.MovementType.SALE
        );

        movement.setReason(
                "Sale through invoice"
        );

        movement.setReferenceId(
                invoiceId
        );

        movement.setReferenceType(
                "INVOICE"
        );

        stockMovementRepository.save(movement);
    }
    @Transactional
    public void addSaleReturnStock(
            Product product,
            Business business,
            BigDecimal quantity,
            Long salesReturnId) {

        Inventory inventory =
                inventoryRepository
                        .findByProductIdAndBusinessId(
                                product.getId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Inventory not found for product: "
                                                + product.getName()
                                )
                        );

        if (quantity == null ||
                quantity.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Sales return quantity must be greater than zero"
            );
        }

        BigDecimal quantityBefore =
                inventory.getQuantity();

        BigDecimal quantityAfter =
                quantityBefore.add(quantity);

        inventory.setQuantity(quantityAfter);
        inventory.setUpdatedAt(LocalDateTime.now());

        inventoryRepository.save(inventory);

        StockMovement movement =
                new StockMovement();

        movement.setProduct(product);
        movement.setBusiness(business);

        movement.setQuantity(quantity);

        movement.setQuantityBefore(
                quantityBefore
        );

        movement.setQuantityAfter(
                quantityAfter
        );

        movement.setType(
                StockMovement.MovementType.SALE_RETURN
        );

        movement.setReason(
                "Sales return"
        );

        movement.setReferenceId(
                salesReturnId
        );

        movement.setReferenceType(
                "SALES_RETURN"
        );

        movement.setCreatedAt(
                LocalDateTime.now()
        );

        stockMovementRepository.save(movement);
    }
    @Transactional
    public void removePurchaseReturnStock(
            Product product,
            Business business,
            BigDecimal quantity,
            Long purchaseReturnId) {

        Inventory inventory =
                inventoryRepository
                        .findByProductIdAndBusinessId(
                                product.getId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Inventory not found for product: "
                                                + product.getName()
                                )
                        );

        BigDecimal quantityBefore =
                inventory.getQuantity();

        BigDecimal availableStock =
                quantityBefore.subtract(
                        inventory.getReservedQuantity()
                );

        if (availableStock.compareTo(quantity) < 0) {
            throw new RuntimeException(
                    "Insufficient available stock for purchase return: "
                            + product.getName()
            );
        }

        BigDecimal quantityAfter =
                quantityBefore.subtract(quantity);

        if (quantityAfter.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "Purchase return cannot make stock negative"
            );
        }

        inventory.setQuantity(quantityAfter);
        inventory.setUpdatedAt(LocalDateTime.now());

        inventoryRepository.save(inventory);

        StockMovement movement =
                new StockMovement();

        movement.setProduct(product);
        movement.setBusiness(business);

        movement.setType(
                StockMovement.MovementType.PURCHASE_RETURN
        );

        movement.setQuantity(quantity);

        movement.setQuantityBefore(
                quantityBefore
        );

        movement.setQuantityAfter(
                quantityAfter
        );

        movement.setReferenceType(
                "PURCHASE_RETURN"
        );

        movement.setReferenceId(
                purchaseReturnId
        );

        movement.setReason(
                "Purchase return"
        );

        movement.setCreatedAt(
                LocalDateTime.now()
        );

        stockMovementRepository.save(movement);
    }
}