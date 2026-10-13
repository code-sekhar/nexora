package com.nexora.auth.service;



import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nexora.user.entity.User;
import com.nexora.user.exception.UserAlreadyExistsException;
import com.nexora.user.repository.UserRepository;
import com.nexora.auth.dto.LoginRequest;
import com.nexora.auth.exception.InvalidCredentialsException;


import org.springframework.transaction.annotation.Transactional;

import com.nexora.auth.dto.RegisterRequest;
import com.nexora.auth.dto.RegisterResponse;
import com.nexora.business.entity.Business;
import com.nexora.business.exception.BusinessAlreadyExistsException;
import com.nexora.business.repository.BusinessRepository;
import com.nexora.user.dto.UserResponse;
import java.time.LocalDateTime;

import java.security.SecureRandom;

import com.nexora.auth.entity.PasswordResetToken;
import com.nexora.auth.repository.PasswordResetTokenRepository;
import com.nexora.auth.dto.ForgotPasswordRequest;
import com.nexora.auth.dto.VerifyOtpRequest;
import com.nexora.auth.dto.ResetPasswordRequest;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BusinessRepository businessRepository;
    
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       BusinessRepository businessRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       EmailService emailService
                       ) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.businessRepository = businessRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
        		.orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

        	throw new InvalidCredentialsException("Invalid email or password");
        }
        if (!user.isActive()) {
            throw new InvalidCredentialsException(
                    "Account is inactive"
            );
        }

        return jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );
    }
    
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // Check if user already exists
        if (userRepository.existsByEmail(request.getOwnerEmail())) {
            throw new UserAlreadyExistsException(
                    "User already exists with this email");
        }

        // Check if business already exists
        if (businessRepository.existsByBusinessName(
                request.getBusinessName())) {

            throw new BusinessAlreadyExistsException(
                    "Business already exists");
        }

        // Create Business
        Business business = new Business();

        business.setBusinessName(request.getBusinessName());
        business.setBusinessType(request.getBusinessType());
        business.setPhone(request.getPhone());
        business.setEmail(request.getEmail());
        business.setAddress(request.getAddress());

        business.setCreatedAt(LocalDateTime.now());
        business.setUpdatedAt(LocalDateTime.now());

        Business savedBusiness =
                businessRepository.save(business);


        // Create Owner User
        User user = new User();

        user.setName(request.getOwnerName());
        user.setEmail(request.getOwnerEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole("OWNER");
        user.setActive(true);

        user.setBusiness(savedBusiness);

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser =
                userRepository.save(user);


        // Generate JWT
        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getRole()
        );


        // Prepare User Response
        UserResponse userResponse =
                new UserResponse(
                        savedUser.getId(),
                        savedUser.getName(),
                        savedUser.getEmail(),
                        savedUser.getRole(),
                        savedUser.getCreatedAt(),
                        savedUser.getUpdatedAt()
                );


        // Final Registration Response
        return new RegisterResponse(
                token,
                userResponse,
                savedBusiness
        );
    }
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "No account found with this email"));

        if (!user.isActive()) {
            throw new InvalidCredentialsException(
                    "Account is inactive");
        }

        // Generate 6 digit OTP
        SecureRandom secureRandom = new SecureRandom();

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1000000)
        );

        // Invalidate previous OTP
        passwordResetTokenRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .ifPresent(oldToken -> {
                    oldToken.setUsed(true);
                    passwordResetTokenRepository.save(oldToken);
                });

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setOtp(otp);
        resetToken.setUser(user);
        resetToken.setUsed(false);
        resetToken.setVerified(false);
        resetToken.setCreatedAt(LocalDateTime.now());
        resetToken.setExpiresAt(
                LocalDateTime.now().plusMinutes(10)
        );

        passwordResetTokenRepository.save(resetToken);

        // Send email
        emailService.sendPasswordResetOtp(
                user.getEmail(),
                otp
        );
    }
    @Transactional
    public void verifyOtp(VerifyOtpRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or OTP"));

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Invalid or expired OTP"));

        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new InvalidCredentialsException(
                    "OTP has expired");
        }

        if (!resetToken.getOtp()
                .equals(request.getOtp())) {

            throw new InvalidCredentialsException(
                    "Invalid OTP");
        }

        // Mark OTP as verified
        resetToken.setVerified(true);

        passwordResetTokenRepository.save(resetToken);
    }
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or OTP"));

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Invalid or expired OTP"));

        // OTP must be verified first
        if (!resetToken.isVerified()) {
            throw new InvalidCredentialsException(
                    "OTP verification required");
        }

        // Make sure the OTP supplied during reset
        // is the same OTP that was verified
        if (!resetToken.getOtp().equals(request.getOtp())) {
            throw new InvalidCredentialsException(
                    "Invalid OTP");
        }

        // Check expiry
        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new InvalidCredentialsException(
                    "OTP has expired");
        }

        // Update password
        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // OTP can no longer be reused
        resetToken.setUsed(true);

        passwordResetTokenRepository.save(resetToken);
    }
    
    
    
    
}