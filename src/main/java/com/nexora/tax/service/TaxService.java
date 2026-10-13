package com.nexora.tax.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.exception.ResourceNotFoundException;
import com.nexora.tax.dto.TaxProfileRequest;
import com.nexora.tax.dto.TaxProfileResponse;
import com.nexora.tax.dto.TaxRateRequest;
import com.nexora.tax.dto.TaxRateResponse;
import com.nexora.tax.entity.TaxProfile;
import com.nexora.tax.entity.TaxRate;
import com.nexora.tax.exception.TaxProfileAlreadyExistsException;
import com.nexora.tax.repository.TaxProfileRepository;
import com.nexora.tax.repository.TaxRateRepository;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@Service
@Transactional
public class TaxService {

    private final TaxProfileRepository taxProfileRepository;
    private final TaxRateRepository taxRateRepository;
    private final UserRepository userRepository;

    public TaxService(
            TaxProfileRepository taxProfileRepository,
            TaxRateRepository taxRateRepository,
            UserRepository userRepository) {

        this.taxProfileRepository = taxProfileRepository;
        this.taxRateRepository = taxRateRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // Current User
    // =========================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));
    }

    // =========================
    // Current Business
    // =========================

    private Business getCurrentBusiness() {

        User user = getCurrentUser();

        if (user.getBusiness() == null) {
            throw new ResourceNotFoundException(
                    "Business not assigned to user"
            );
        }

        return user.getBusiness();
    }

    // =========================
    // Create Tax Profile
    // =========================

    public TaxProfileResponse createTaxProfile(
            TaxProfileRequest request) {

        Business business = getCurrentBusiness();

        if (taxProfileRepository.existsByCodeAndBusinessId(
                request.getCode(),
                business.getId())) {

            throw new TaxProfileAlreadyExistsException(
                    "Tax profile code already exists"
            );
        }

        TaxProfile profile = new TaxProfile();

        profile.setName(request.getName());
        profile.setCode(request.getCode());
        profile.setTaxType(request.getTaxType());
        profile.setActive(true);
        profile.setBusiness(business);

        LocalDateTime now = LocalDateTime.now();

        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);

        TaxProfile savedProfile =
                taxProfileRepository.save(profile);

        return mapProfile(savedProfile);
    }

    // =========================
    // Get All Tax Profiles
    // =========================

    @Transactional(readOnly = true)
    public List<TaxProfileResponse> getAllTaxProfiles() {

        Business business = getCurrentBusiness();

        return taxProfileRepository
                .findByBusinessId(business.getId())
                .stream()
                .map(this::mapProfile)
                .toList();
    }

    // =========================
    // Get Tax Profile By ID
    // =========================

    @Transactional(readOnly = true)
    public TaxProfileResponse getTaxProfileById(Long id) {

        Business business = getCurrentBusiness();

        TaxProfile profile =
                taxProfileRepository
                        .findByIdAndBusinessId(
                                id,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Tax profile not found"
                                ));

        return mapProfile(profile);
    }

    // =========================
    // Create Tax Rate
    // =========================

    public TaxRateResponse createTaxRate(
            Long profileId,
            TaxRateRequest request) {

        Business business = getCurrentBusiness();

        TaxProfile profile =
                taxProfileRepository
                        .findByIdAndBusinessId(
                                profileId,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Tax profile not found"
                                ));

        TaxRate rate = new TaxRate();

        rate.setName(request.getName());
        rate.setComponent(request.getComponent());
        rate.setRate(request.getRate());
        rate.setActive(true);
        rate.setTaxProfile(profile);

        LocalDateTime now = LocalDateTime.now();

        rate.setCreatedAt(now);
        rate.setUpdatedAt(now);

        TaxRate savedRate =
                taxRateRepository.save(rate);

        return mapRate(savedRate);
    }

    // =========================
    // Get Tax Rates
    // =========================

    @Transactional(readOnly = true)
    public List<TaxRateResponse> getTaxRates(
            Long profileId) {

        Business business = getCurrentBusiness();

        // First verify that this profile belongs
        // to the current business.
        taxProfileRepository
                .findByIdAndBusinessId(
                        profileId,
                        business.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tax profile not found"
                        ));

        return taxRateRepository
                .findByTaxProfileId(profileId)
                .stream()
                .map(this::mapRate)
                .toList();
    }

    // =========================
    // Map TaxProfile
    // =========================

    private TaxProfileResponse mapProfile(
            TaxProfile profile) {

        TaxProfileResponse response =
                new TaxProfileResponse();

        response.setId(profile.getId());
        response.setName(profile.getName());
        response.setCode(profile.getCode());
        response.setTaxType(profile.getTaxType());
        response.setActive(profile.isActive());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());

        return response;
    }

    // =========================
    // Map TaxRate
    // =========================

    private TaxRateResponse mapRate(
            TaxRate rate) {

        TaxRateResponse response =
                new TaxRateResponse();

        response.setId(rate.getId());
        response.setName(rate.getName());
        response.setComponent(rate.getComponent());
        response.setRate(rate.getRate());
        response.setActive(rate.isActive());

        response.setTaxProfileId(
                rate.getTaxProfile().getId()
        );

        response.setCreatedAt(rate.getCreatedAt());
        response.setUpdatedAt(rate.getUpdatedAt());

        return response;
    }
    @Transactional
    public TaxRateResponse updateTaxRateStatus(
            Long taxProfileId,
            Long rateId,
            Boolean active,
            String email
    ) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        Business business = user.getBusiness();

        TaxProfile taxProfile = taxProfileRepository
                .findByIdAndBusinessId(
                        taxProfileId,
                        business.getId()
                )
                .orElseThrow(() ->
                        new RuntimeException("Tax profile not found")
                );

        TaxRate taxRate = taxRateRepository
                .findByIdAndTaxProfileId(
                        rateId,
                        taxProfile.getId()
                )
                .orElseThrow(() ->
                        new RuntimeException("Tax rate not found")
                );

        taxRate.setActive(active);
        taxRate.setUpdatedAt(LocalDateTime.now());

        TaxRate savedRate = taxRateRepository.save(taxRate);

        return mapTaxRateToResponse(savedRate);
        
    }
    private TaxRateResponse mapTaxRateToResponse(TaxRate taxRate) {

        TaxRateResponse response = new TaxRateResponse();

        response.setId(taxRate.getId());
        response.setName(taxRate.getName());
        response.setComponent(taxRate.getComponent());
        response.setRate(taxRate.getRate());
        response.setActive(taxRate.isActive());
        response.setTaxProfileId(taxRate.getTaxProfile().getId());
        response.setCreatedAt(taxRate.getCreatedAt());
        response.setUpdatedAt(taxRate.getUpdatedAt());

        return response;
    }
}