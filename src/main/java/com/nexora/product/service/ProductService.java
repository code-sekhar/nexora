package com.nexora.product.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexora.brand.entity.Brand;
import com.nexora.brand.repository.BrandRepository;
import com.nexora.business.entity.Business;
import com.nexora.category.entity.Category;
import com.nexora.category.repository.CategoryRepository;
import com.nexora.exception.ResourceNotFoundException;
import com.nexora.product.dto.ProductRequest;
import com.nexora.product.dto.ProductResponse;
import com.nexora.product.dto.ProductStatusRequest;
import com.nexora.product.entity.Product;
import com.nexora.product.repository.ProductRepository;
import com.nexora.tax.entity.TaxProfile;
import com.nexora.tax.repository.TaxProfileRepository;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final TaxProfileRepository taxProfileRepository;

    public ProductService(
            ProductRepository productRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            TaxProfileRepository taxProfileRepository) {

        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.taxProfileRepository = taxProfileRepository;
    }

    // CREATE
    public ProductResponse createProduct(
            ProductRequest request,
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        validateSku(
                request.getSku(),
                business.getId(),
                null
        );

        validateBarcode(
                request.getBarcode(),
                business.getId(),
                null
        );

        Category category = categoryRepository
                .findByIdAndBusinessId(
                        request.getCategoryId(),
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"));

        if (!category.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot assign inactive category");
        }

        Brand brand = null;

        if (request.getBrandId() != null) {

            brand = brandRepository
                    .findByIdAndBusinessId(
                            request.getBrandId(),
                            business.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Brand not found"));

            if (!brand.isActive()) {
                throw new IllegalArgumentException(
                        "Cannot assign inactive brand");
            }
        }

        TaxProfile taxProfile = null;

        if (request.getTaxProfileId() != null) {

            taxProfile = taxProfileRepository
                    .findByIdAndBusinessId(
                            request.getTaxProfileId(),
                            business.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Tax profile not found"));

            if (!taxProfile.isActive()) {
                throw new IllegalArgumentException(
                        "Cannot assign inactive tax profile");
            }
        }

        Product product = new Product();

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBrand(brand);
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setHsnSacCode(request.getHsnSacCode());
        product.setTaxProfile(taxProfile);
        product.setUnit(request.getUnit());
        product.setMinimumStock(request.getMinimumStock());
        product.setActive(true);
        product.setBusiness(business);

        LocalDateTime now = LocalDateTime.now();

        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        Product savedProduct =
                productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    // GET ALL
    public List<ProductResponse> getAllProducts(
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        return productRepository
                .findByBusinessId(business.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET BY ID
    public ProductResponse getProductById(
            Long id,
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        Product product = productRepository
                .findByIdAndBusinessId(
                        id,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"));

        return mapToResponse(product);
    }

    // UPDATE
    public ProductResponse updateProduct(
            Long id,
            ProductRequest request,
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        Product product = productRepository
                .findByIdAndBusinessId(
                        id,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"));

        validateSku(
                request.getSku(),
                business.getId(),
                id
        );

        validateBarcode(
                request.getBarcode(),
                business.getId(),
                id
        );

        Category category = categoryRepository
                .findByIdAndBusinessId(
                        request.getCategoryId(),
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"));

        if (!category.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot assign inactive category");
        }

        Brand brand = null;

        if (request.getBrandId() != null) {

            brand = brandRepository
                    .findByIdAndBusinessId(
                            request.getBrandId(),
                            business.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Brand not found"));

            if (!brand.isActive()) {
                throw new IllegalArgumentException(
                        "Cannot assign inactive brand");
            }
        }

        TaxProfile taxProfile = null;

        if (request.getTaxProfileId() != null) {

            taxProfile = taxProfileRepository
                    .findByIdAndBusinessId(
                            request.getTaxProfileId(),
                            business.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Tax profile not found"));

            if (!taxProfile.isActive()) {
                throw new IllegalArgumentException(
                        "Cannot assign inactive tax profile");
            }
        }

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBrand(brand);
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setHsnSacCode(request.getHsnSacCode());
        product.setTaxProfile(taxProfile);
        product.setUnit(request.getUnit());
        product.setMinimumStock(request.getMinimumStock());
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct =
                productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    // UPDATE STATUS
    public ProductResponse updateProductStatus(
            Long id,
            ProductStatusRequest request,
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        Product product = productRepository
                .findByIdAndBusinessId(
                        id,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"));

        product.setActive(request.isActive());
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct =
                productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    // DELETE
    public void deleteProduct(
            Long id,
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        Product product = productRepository
                .findByIdAndBusinessId(
                        id,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"));

        productRepository.delete(product);
    }

    // SKU VALIDATION
    private void validateSku(
            String sku,
            Long businessId,
            Long productId) {

        boolean exists;

        if (productId == null) {

            exists = productRepository
                    .existsBySkuAndBusinessId(
                            sku,
                            businessId);

        } else {

            exists = productRepository
                    .existsBySkuAndBusinessIdAndIdNot(
                            sku,
                            businessId,
                            productId);
        }

        if (exists) {
            throw new IllegalArgumentException(
                    "SKU already exists");
        }
    }

    // BARCODE VALIDATION
    private void validateBarcode(
            String barcode,
            Long businessId,
            Long productId) {

        if (barcode == null ||
                barcode.isBlank()) {
            return;
        }

        boolean exists;

        if (productId == null) {

            exists = productRepository
                    .existsByBarcodeAndBusinessId(
                            barcode,
                            businessId);

        } else {

            exists = productRepository
                    .existsByBarcodeAndBusinessIdAndIdNot(
                            barcode,
                            businessId,
                            productId);
        }

        if (exists) {
            throw new IllegalArgumentException(
                    "Barcode already exists");
        }
    }

    // LOGGED-IN USER
    private User getLoggedInUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));
    }

    // USER BUSINESS
    private Business getUserBusiness(User user) {

        if (user.getBusiness() == null) {
            throw new ResourceNotFoundException(
                    "Business not assigned to user");
        }

        return user.getBusiness();
    }

    // ENTITY → RESPONSE
    private ProductResponse mapToResponse(
            Product product) {

        Long categoryId = null;
        String categoryName = null;

        if (product.getCategory() != null) {

            categoryId =
                    product.getCategory().getId();

            categoryName =
                    product.getCategory().getName();
        }

        Long brandId = null;
        String brandName = null;

        if (product.getBrand() != null) {

            brandId =
                    product.getBrand().getId();

            brandName =
                    product.getBrand().getName();
        }

        Long taxProfileId = null;
        String taxProfileName = null;
        String taxType = null;

        if (product.getTaxProfile() != null) {

            taxProfileId =
                    product.getTaxProfile().getId();

            taxProfileName =
                    product.getTaxProfile().getName();

            taxType =
                    product.getTaxProfile().getTaxType();
        }

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getBarcode(),
                product.getDescription(),
                categoryId,
                categoryName,
                brandId,
                brandName,
                product.getPurchasePrice(),
                product.getSellingPrice(),
                product.getHsnSacCode(),
                taxProfileId,
                taxProfileName,
                taxType,
                product.getUnit(),
                product.getMinimumStock(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
    
}