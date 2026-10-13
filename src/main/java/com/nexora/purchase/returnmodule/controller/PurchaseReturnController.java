package com.nexora.purchase.returnmodule.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.business.entity.Business;
import com.nexora.exception.ResourceNotFoundException;
import com.nexora.purchase.returnmodule.dto.PurchaseReturnRequest;
import com.nexora.purchase.returnmodule.dto.PurchaseReturnResponse;
import com.nexora.purchase.returnmodule.entity.PurchaseReturn;
import com.nexora.purchase.returnmodule.service.PurchaseReturnService;
import com.nexora.response.ApiResponse;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@RestController
@RequestMapping("/api/purchase-returns")
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    private final UserRepository userRepository;

    public PurchaseReturnController(
            PurchaseReturnService purchaseReturnService,
            UserRepository userRepository) {

        this.purchaseReturnService =
                purchaseReturnService;

        this.userRepository =
                userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createReturn(
            @RequestBody PurchaseReturnRequest request,
            Authentication authentication) {

        User user =
                getUser(
                        authentication.getName()
                );

        Business business =
                getBusiness(user);

        PurchaseReturn data =
                purchaseReturnService.createReturn(
                        business,
                        request
                );
        PurchaseReturnResponse response =
                purchaseReturnService.mapToResponse(data);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Purchase return created successfully",
                        response
                )
        );
    }

    private User getUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private Business getBusiness(User user) {

        if (user.getBusiness() == null) {

            throw new ResourceNotFoundException(
                    "Business not assigned to user"
            );
        }

        return user.getBusiness();
    }
}