package com.nexora.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexora.auth.dto.ForgotPasswordRequest;
import com.nexora.auth.dto.LoginRequest;
import com.nexora.auth.dto.LoginResponse;
import com.nexora.auth.dto.RegisterRequest;
import com.nexora.auth.dto.RegisterResponse;
import com.nexora.auth.dto.ResetPasswordRequest;
import com.nexora.auth.dto.VerifyOtpRequest;
import com.nexora.auth.service.AuthService;
import com.nexora.response.ApiResponse;
import com.nexora.user.dto.UserResponse;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService,
                          UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        String token = authService.login(request);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        LoginResponse loginResponse =
                new LoginResponse(token, userResponse);

        ApiResponse<LoginResponse> response =
                new ApiResponse<>(
                        true,
                        "Login successful",
                        loginResponse
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        RegisterResponse registerResponse =
                authService.register(request);

        ApiResponse<RegisterResponse> response =
                new ApiResponse<>(
                        true,
                        "Registration successful",
                        registerResponse
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        ApiResponse<Void> response =
                new ApiResponse<>(
                        true,
                        "OTP sent to your email",
                        null
                );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        authService.verifyOtp(request);

        ApiResponse<Void> response =
                new ApiResponse<>(
                        true,
                        "OTP verified successfully",
                        null
                );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        ApiResponse<Void> response =
                new ApiResponse<>(
                        true,
                        "Password reset successfully",
                        null
                );

        return ResponseEntity.ok(response);
    }
}