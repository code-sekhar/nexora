package com.nexora.sales.returnmodule.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.business.entity.Business;
import com.nexora.exception.ResourceNotFoundException;
import com.nexora.response.ApiResponse;
import com.nexora.sales.returnmodule.dto.SalesReturnRequest;
import com.nexora.sales.returnmodule.dto.SalesReturnResponse;
import com.nexora.sales.returnmodule.entity.SalesReturn;
import com.nexora.sales.returnmodule.service.SalesReturnService;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@RestController
@RequestMapping("/api/sales-returns")
public class SalesReturnController {

    private final SalesReturnService salesReturnService;

    private final UserRepository userRepository;

    public SalesReturnController(
            SalesReturnService salesReturnService,
            UserRepository userRepository) {

        this.salesReturnService = salesReturnService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createReturn(
            @RequestBody SalesReturnRequest request,
            Authentication authentication) {

        User user =
                userRepository
                        .findByEmail(authentication.getName())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        Business business =
                user.getBusiness();

        if (business == null) {
            throw new ResourceNotFoundException(
                    "Business not assigned to user"
            );
        }

        SalesReturn data =
                salesReturnService.createReturn(
                        business,
                        request
                );

        SalesReturnResponse response =
                salesReturnService.mapToResponse(data);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Sales return created successfully",
                        response
                )
        );
    }
}