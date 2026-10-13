package com.nexora.brand.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexora.brand.dto.BrandRequest;
import com.nexora.brand.dto.BrandResponse;
import com.nexora.brand.dto.BrandStatusRequest;
import com.nexora.brand.entity.Brand;
import com.nexora.brand.repository.BrandRepository;
import com.nexora.business.entity.Business;
import com.nexora.exception.ResourceNotFoundException;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@Service
public class BrandService {

    private final BrandRepository brandRepository;
    private final UserRepository userRepository;

    public BrandService(
            BrandRepository brandRepository,
            UserRepository userRepository) {

        this.brandRepository = brandRepository;
        this.userRepository = userRepository;
    }

    // CREATE
    public BrandResponse createBrand(
            BrandRequest request,
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        if (brandRepository.existsByNameAndBusinessId(
                request.getName(),
                business.getId())) {

            throw new IllegalArgumentException(
                    "Brand already exists");
        }

        Brand brand = new Brand();

        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogo(request.getLogo());
        brand.setActive(true);
        brand.setBusiness(business);
        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(LocalDateTime.now());

        Brand savedBrand = brandRepository.save(brand);

        return mapToResponse(savedBrand);
    }

    // GET ALL
    public List<BrandResponse> getAllBrands(
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        return brandRepository
                .findByBusinessId(business.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET BY ID
    public BrandResponse getBrandById(
            Long id,
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        Brand brand = brandRepository
                .findByIdAndBusinessId(
                        id,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Brand not found"));

        return mapToResponse(brand);
    }

    // UPDATE
    public BrandResponse updateBrand(
            Long id,
            BrandRequest request,
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        Brand brand = brandRepository
                .findByIdAndBusinessId(
                        id,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Brand not found"));

        if (brandRepository
                .existsByNameAndBusinessIdAndIdNot(
                        request.getName(),
                        business.getId(),
                        id)) {

            throw new IllegalArgumentException(
                    "Brand already exists");
        }

        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogo(request.getLogo());
        brand.setUpdatedAt(LocalDateTime.now());

        Brand updatedBrand =
                brandRepository.save(brand);

        return mapToResponse(updatedBrand);
    }

    // UPDATE STATUS
    public BrandResponse updateBrandStatus(
            Long id,
            BrandStatusRequest request,
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        Brand brand = brandRepository
                .findByIdAndBusinessId(
                        id,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Brand not found"));

        brand.setActive(request.isActive());
        brand.setUpdatedAt(LocalDateTime.now());

        Brand updatedBrand =
                brandRepository.save(brand);

        return mapToResponse(updatedBrand);
    }

    // DELETE
    public void deleteBrand(
            Long id,
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        Brand brand = brandRepository
                .findByIdAndBusinessId(
                        id,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Brand not found"));

        brandRepository.delete(brand);
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

    // ENTITY => RESPONSE
    private BrandResponse mapToResponse(
            Brand brand) {

        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getDescription(),
                brand.getLogo(),
                brand.isActive(),
                brand.getCreatedAt(),
                brand.getUpdatedAt()
        );
    }
}