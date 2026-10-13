package com.nexora.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexora.response.ApiResponse;
import com.nexora.user.dto.UserRequest;
import com.nexora.user.dto.UserResponse;
import com.nexora.user.dto.UserUpdateRequest;
import com.nexora.user.entity.User;
import com.nexora.user.service.UserService;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;

import com.nexora.user.dto.UserStatusRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserRequest request,
            Authentication authentication) {

    	String email = authentication.getName();

    	User user = userService.createUser(
    	        request,
    	        email
    	);
        UserResponse UserResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        ApiResponse<UserResponse> response = new ApiResponse<>(
                true,
                "User created successfully",
                UserResponse
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            Authentication authentication) {

        String email = authentication.getName();

        List<UserResponse> users =
                userService.getAllUsers(email);

        ApiResponse<List<UserResponse>> response =
                new ApiResponse<>(
                        true,
                        "Users fetched successfully",
                        users
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
//    @PutMapping("/{userId}/business/{businessId}")
//    public ResponseEntity<ApiResponse<UserResponse>> assignBusiness(
//            @PathVariable Long userId,
//            @PathVariable Long businessId) {
//
//        UserResponse userResponse =
//                userService.assignBusiness(userId, businessId);
//
//        ApiResponse<UserResponse> response =
//                new ApiResponse<>(
//                        true,
//                        "Business assigned successfully",
//                        userResponse
//                );
//
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(response);
//    }
    @PutMapping("/{userId}/business/{businessId}")
    public ResponseEntity<ApiResponse<UserResponse>> assignBusiness(
            @PathVariable Long userId,
            @PathVariable Long businessId,
            Authentication authentication) {

        String email = authentication.getName();

        UserResponse userResponse =
                userService.assignBusiness(
                        userId,
                        businessId,
                        email);

        ApiResponse<UserResponse> response =
                new ApiResponse<>(
                        true,
                        "Business assigned successfully",
                        userResponse);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long userId,
            Authentication authentication) {

        String email = authentication.getName();

        UserResponse userResponse =
                userService.getUserById(userId, email);

        ApiResponse<UserResponse> response =
                new ApiResponse<>(
                        true,
                        "User fetched successfully",
                        userResponse
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        UserResponse userResponse =
                userService.updateUser(
                        userId,
                        email,
                        request
                );

        ApiResponse<UserResponse> response =
                new ApiResponse<>(
                        true,
                        "User updated successfully",
                        userResponse
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        UserResponse userResponse =
                userService.updateUserStatus(
                        userId,
                        email,
                        request
                );

        String message = request.isActive()
                ? "User activated successfully"
                : "User deactivated successfully";

        ApiResponse<UserResponse> response =
                new ApiResponse<>(
                        true,
                        message,
                        userResponse
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}