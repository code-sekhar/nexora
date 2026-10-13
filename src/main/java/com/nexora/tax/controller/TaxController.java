package com.nexora.tax.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexora.response.ApiResponse;
import com.nexora.tax.dto.TaxProfileRequest;
import com.nexora.tax.dto.TaxProfileResponse;
import com.nexora.tax.dto.TaxRateRequest;
import com.nexora.tax.dto.TaxRateResponse;
import com.nexora.tax.dto.TaxRateStatusRequest;
import com.nexora.tax.service.TaxService;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/taxes")
public class TaxController {

    private final TaxService taxService;

    public TaxController(TaxService taxService) {
        this.taxService = taxService;
    }

    // =========================================
    // Create Tax Profile
    // =========================================

    @PostMapping("/profiles")
    public ResponseEntity<ApiResponse<TaxProfileResponse>> createTaxProfile(
            @Valid @RequestBody TaxProfileRequest request) {

        TaxProfileResponse profile =
                taxService.createTaxProfile(request);

        ApiResponse<TaxProfileResponse> response =
                new ApiResponse<>(
                        true,
                        "Tax profile created successfully",
                        profile
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================
    // Get All Tax Profiles
    // =========================================

    @GetMapping("/profiles")
    public ResponseEntity<ApiResponse<List<TaxProfileResponse>>> getAllTaxProfiles() {

        List<TaxProfileResponse> profiles =
                taxService.getAllTaxProfiles();

        ApiResponse<List<TaxProfileResponse>> response =
                new ApiResponse<>(
                        true,
                        "Tax profiles fetched successfully",
                        profiles
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // =========================================
    // Get Tax Profile By ID
    // =========================================

    @GetMapping("/profiles/{id}")
    public ResponseEntity<ApiResponse<TaxProfileResponse>> getTaxProfile(
            @PathVariable Long id) {

        TaxProfileResponse profile =
                taxService.getTaxProfileById(id);

        ApiResponse<TaxProfileResponse> response =
                new ApiResponse<>(
                        true,
                        "Tax profile fetched successfully",
                        profile
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // =========================================
    // Create Tax Rate
    // =========================================

    @PostMapping("/profiles/{profileId}/rates")
    public ResponseEntity<ApiResponse<TaxRateResponse>> createTaxRate(
            @PathVariable Long profileId,
            @Valid @RequestBody TaxRateRequest request) {

        TaxRateResponse rate =
                taxService.createTaxRate(
                        profileId,
                        request
                );

        ApiResponse<TaxRateResponse> response =
                new ApiResponse<>(
                        true,
                        "Tax rate created successfully",
                        rate
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================
    // Get Tax Rates
    // =========================================

    @GetMapping("/profiles/{profileId}/rates")
    public ResponseEntity<ApiResponse<List<TaxRateResponse>>> getTaxRates(
            @PathVariable Long profileId) {

        List<TaxRateResponse> rates =
                taxService.getTaxRates(profileId);

        ApiResponse<List<TaxRateResponse>> response =
                new ApiResponse<>(
                        true,
                        "Tax rates fetched successfully",
                        rates
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
    @PatchMapping("/profiles/{taxProfileId}/rates/{rateId}/status")
    public ResponseEntity<ApiResponse<TaxRateResponse>> updateTaxRateStatus(
            @PathVariable Long taxProfileId,
            @PathVariable Long rateId,
            @Valid @RequestBody TaxRateStatusRequest request,
            Authentication authentication
    ) {

        TaxRateResponse response =
                taxService.updateTaxRateStatus(
                        taxProfileId,
                        rateId,
                        request.getActive(),
                        authentication.getName()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Tax rate status updated successfully",
                        response
                )
        );
    }
}