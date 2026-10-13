package com.nexora.user.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nexora.user.dto.UserRequest;
import com.nexora.user.dto.UserResponse;
import com.nexora.user.dto.UserUpdateRequest;
import com.nexora.user.entity.User;
import com.nexora.user.exception.UserAlreadyExistsException;
import com.nexora.user.repository.UserRepository;
import java.util.List;
import com.nexora.business.entity.Business;
import com.nexora.business.repository.BusinessRepository;
import com.nexora.exception.ResourceNotFoundException;

import com.nexora.user.dto.UserStatusRequest;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final BusinessRepository businessRepository;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			BusinessRepository businessRepository) {

		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.businessRepository = businessRepository;
	}

//	public User createUser(UserRequest request) {
//
//		if (userRepository.existsByEmail(request.getEmail())) {
//			throw new UserAlreadyExistsException("User already exists");
//		}
//
//		User user = new User();
//
//		user.setName(request.getName());
//		user.setEmail(request.getEmail());
//
//		user.setPassword(passwordEncoder.encode(request.getPassword()));
//
//		user.setRole("OWNER");
//		user.setActive(true);
//
//		user.setCreatedAt(LocalDateTime.now());
//		user.setUpdatedAt(LocalDateTime.now());
//
//		return userRepository.save(user);
//	}
	public User createUser(
	        UserRequest request,
	        String email) {

	    User loggedInUser = userRepository.findByEmail(email)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("User not found"));

	    if (loggedInUser.getBusiness() == null) {
	        throw new ResourceNotFoundException(
	                "Business not assigned to user");
	    }

	    // Only OWNER can create users for now
	    if (!loggedInUser.getRole().equals("OWNER") && !loggedInUser.getRole().equals("MANAGER")) {
	        throw new IllegalArgumentException(
	                "Only owner or manager can create users");
	    }

	    // Only MANAGER or STAFF can be created through this API
	    if (loggedInUser.getRole().equals("MANAGER")
	            && !request.getRole().equals("STAFF")) {

	        throw new IllegalArgumentException(
	                "Manager can only create STAFF users");
	    }

	    if (!request.getRole().equals("MANAGER")
	            && !request.getRole().equals("STAFF")) {

	        throw new IllegalArgumentException(
	                "You can only create MANAGER or STAFF users");
	    }

	    if (userRepository.existsByEmail(request.getEmail())) {
	        throw new UserAlreadyExistsException(
	                "User already exists");
	    }

	    User user = new User();

	    user.setName(request.getName());
	    user.setEmail(request.getEmail());

	    user.setPassword(
	            passwordEncoder.encode(request.getPassword())
	    );

	    user.setRole(request.getRole());

	    user.setActive(true);

	    // Automatically assign the new user
	    // to the logged-in owner's business
	    user.setBusiness(loggedInUser.getBusiness());

	    user.setCreatedAt(LocalDateTime.now());
	    user.setUpdatedAt(LocalDateTime.now());

	    return userRepository.save(user);
	}

	public List<UserResponse> getAllUsers(String email) {

		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getBusiness() == null) {
			throw new ResourceNotFoundException("Business not assigned to user");
		}

		Long businessId = loggedInUser.getBusiness().getId();

		List<User> users = userRepository.findByBusinessId(businessId);

		return users.stream().map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail(),
				user.getRole(), user.getCreatedAt(), user.getUpdatedAt())).toList();
	}

	public UserResponse getUserById(Long userId, String email) {

		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getBusiness() == null) {
			throw new ResourceNotFoundException("Business not assigned to user");
		}

		Long businessId = loggedInUser.getBusiness().getId();

		User user = userRepository.findByIdAndBusinessId(userId, businessId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt(),
				user.getUpdatedAt());
	}

	public UserResponse updateUser(Long userId, String email, UserUpdateRequest request) {

		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getBusiness() == null) {
			throw new ResourceNotFoundException("Business not assigned to user");
		}

		Long businessId = loggedInUser.getBusiness().getId();

		User user = userRepository.findByIdAndBusinessId(userId, businessId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setUpdatedAt(LocalDateTime.now());

		User updatedUser = userRepository.save(user);

		return new UserResponse(updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail(),
				updatedUser.getRole(), updatedUser.getCreatedAt(), updatedUser.getUpdatedAt());
	}

	public UserResponse updateUserStatus(Long userId, String email, UserStatusRequest request) {

		User loggedInUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getBusiness() == null) {
			throw new ResourceNotFoundException("Business not assigned to user");
		}

		Long businessId = loggedInUser.getBusiness().getId();

		User user = userRepository.findByIdAndBusinessId(userId, businessId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (loggedInUser.getId().equals(user.getId())) {
			throw new IllegalArgumentException("You cannot change your own account status");
		}

		// ROLE AUTHORIZATION
		if (loggedInUser.getRole().equals("OWNER")) {

			if (user.getRole().equals("OWNER")) {
				throw new IllegalArgumentException("Owner cannot change another owner's account status");
			}

		} else if (loggedInUser.getRole().equals("MANAGER")) {

			if (!user.getRole().equals("STAFF")) {
				throw new IllegalArgumentException("Manager can only change staff account status");
			}

		} else if (loggedInUser.getRole().equals("STAFF")) {

			throw new IllegalArgumentException("Staff cannot change user account status");

		} else {

			throw new IllegalArgumentException("You are not authorized to change user account status");
		}

		user.setActive(request.isActive());
		user.setUpdatedAt(LocalDateTime.now());

		User updatedUser = userRepository.save(user);

		return new UserResponse(updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail(),
				updatedUser.getRole(), updatedUser.getCreatedAt(), updatedUser.getUpdatedAt());
	}

//	public UserResponse assignBusiness(Long userId, Long businessId) {
//
//		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//		Business business = businessRepository.findById(businessId)
//				.orElseThrow(() -> new ResourceNotFoundException("Business not found"));
//
//		user.setBusiness(business);
//		user.setUpdatedAt(LocalDateTime.now());
//
//		User savedUser = userRepository.save(user);
//
//		return new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole(),
//				savedUser.getCreatedAt(), savedUser.getUpdatedAt());
//	}
	public UserResponse assignBusiness(
	        Long userId,
	        Long businessId,
	        String email) {

	    // Logged-in user
	    User loggedInUser = userRepository.findByEmail(email)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("User not found"));

	    // Logged-in user must have a business
	    if (loggedInUser.getBusiness() == null) {
	        throw new ResourceNotFoundException(
	                "Business not assigned to logged-in user");
	    }

	    // Only OWNER can assign users
	    if (!loggedInUser.getRole().equals("OWNER")) {
	        throw new IllegalArgumentException(
	                "Only owner can assign users to a business");
	    }

	    Long loggedInBusinessId =
	            loggedInUser.getBusiness().getId();

	    // Target business must be owner's own business
	    if (!loggedInBusinessId.equals(businessId)) {
	        throw new IllegalArgumentException(
	                "You can only assign users to your own business");
	    }

	    // Find target user
	    User user = userRepository.findById(userId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("User not found"));

	    // User already belongs to a business
	    if (user.getBusiness() != null) {
	        throw new IllegalArgumentException(
	                "User is already assigned to a business");
	    }

	    user.setBusiness(loggedInUser.getBusiness());
	    user.setUpdatedAt(LocalDateTime.now());

	    User savedUser = userRepository.save(user);

	    return new UserResponse(
	            savedUser.getId(),
	            savedUser.getName(),
	            savedUser.getEmail(),
	            savedUser.getRole(),
	            savedUser.getCreatedAt(),
	            savedUser.getUpdatedAt()
	    );
	}
}