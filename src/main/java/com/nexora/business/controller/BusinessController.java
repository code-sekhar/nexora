package com.nexora.business.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexora.business.dto.BusinessRequest;
import com.nexora.business.entity.Business;
import com.nexora.business.service.BusinessService;
import com.nexora.response.ApiResponse;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Business>> createBusiness(
            @Valid @RequestBody BusinessRequest request) {

        Business business = businessService.createBusiness(request);

        ApiResponse<Business> response = new ApiResponse<>(
                true,
                "Business created successfully",
                business
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
//    @GetMapping()
//    public ResponseEntity<ApiResponse<List<Business>>> getAllBusinesses() {
//
//        List<Business> businesses = businessService.getAllBusinesses();
//
//        ApiResponse<List<Business>> response = new ApiResponse<>(
//                true,
//                "Businesses fetched successfully",
//                businesses
//        );
//
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(response);
//    }
    @GetMapping()
    public ResponseEntity<ApiResponse<List<Business>>> getAllBusinesses(
            Authentication authentication) {

        String email = authentication.getName();

        List<Business> businesses =
                businessService.getAllBusinesses(email);

        ApiResponse<List<Business>> response = new ApiResponse<>(
                true,
                "Businesses fetched successfully",
                businesses
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<Business>> getBusinessById(
//            @PathVariable Long id) {
//
//        Business business = businessService.getBusinessById(id);
//
//        ApiResponse<Business> response = new ApiResponse<>(
//                true,
//                "Business fetched successfully",
//                business
//        );
//
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(response);
//    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Business>> getBusinessById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        Business business =
                businessService.getBusinessById(id, email);

        ApiResponse<Business> response = new ApiResponse<>(
                true,
                "Business fetched successfully",
                business
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse<Business>> updateBusiness(
//            @PathVariable Long id,
//            @Valid @RequestBody BusinessRequest request) {
//
//        Business business = businessService.updateBusiness(id, request);
//
//        ApiResponse<Business> response = new ApiResponse<>(
//                true,
//                "Business updated successfully",
//                business
//        );
//
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(response);
//    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Business>> updateBusiness(
            @PathVariable Long id,
            @Valid @RequestBody BusinessRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Business business =
                businessService.updateBusiness(
                        id,
                        request,
                        email);

        ApiResponse<Business> response = new ApiResponse<>(
                true,
                "Business updated successfully",
                business
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse<Object>> deleteBusiness(
//            @PathVariable Long id) {
//
//        businessService.deleteBusiness(id);
//
//        ApiResponse<Object> response = new ApiResponse<>(
//                true,
//                "Business deleted successfully",
//                null
//        );
//
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(response);
//    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteBusiness(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        businessService.deleteBusiness(id, email);

        ApiResponse<Object> response = new ApiResponse<>(
                true,
                "Business deleted successfully",
                null
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}