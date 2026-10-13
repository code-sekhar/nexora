package com.nexora.business.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import com.nexora.business.dto.BusinessRequest;
import com.nexora.business.entity.Business;
import com.nexora.business.exception.BusinessAlreadyExistsException;
//import com.nexora.business.exception.ResourceNotFoundException;
import com.nexora.exception.ResourceNotFoundException;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;
import com.nexora.business.repository.BusinessRepository;
import java.util.List;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    public BusinessService(BusinessRepository businessRepository,UserRepository userRepository) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
    }

    public Business createBusiness(BusinessRequest request) {
    	if (businessRepository.existsByBusinessName(request.getBusinessName())) {
            //throw new RuntimeException("Business already exists");
            throw new BusinessAlreadyExistsException("Business already exists");
        }

        Business business = new Business();

        business.setBusinessName(request.getBusinessName());
        business.setBusinessType(request.getBusinessType());
        business.setPhone(request.getPhone());
        business.setEmail(request.getEmail());
        business.setAddress(request.getAddress());
        business.setCountryCode(request.getCountryCode());
        business.setCurrencyCode(request.getCurrencyCode());
        business.setTaxSystem(request.getTaxSystem());
        business.setTaxRegistrationNumber(
                request.getTaxRegistrationNumber()
        );

        business.setCreatedAt(LocalDateTime.now());
        business.setUpdatedAt(LocalDateTime.now());

        return businessRepository.save(business);
    }
//    public List<Business> getAllBusinesses() {
//        return businessRepository.findAll();
//    }
    public List<Business> getAllBusinesses(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.getBusiness() == null) {
            throw new ResourceNotFoundException(
                    "Business not assigned to user");
        }

        return List.of(user.getBusiness());
    }
//    public Business getBusinessById(Long id) {
//
//        return businessRepository.findById(id)
//        		.orElseThrow(() -> new ResourceNotFoundException("Business not found"));
//    }
    public Business getBusinessById(
            Long id,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.getBusiness() == null) {
            throw new ResourceNotFoundException(
                    "Business not assigned to user");
        }

        if (!user.getBusiness().getId().equals(id)) {
            throw new ResourceNotFoundException(
                    "Business not found");
        }

        return user.getBusiness();
    }
//    public Business updateBusiness(Long id, BusinessRequest request) {
//
//        Business business = businessRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
//
//        business.setBusinessName(request.getBusinessName());
//        business.setBusinessType(request.getBusinessType());
//        business.setPhone(request.getPhone());
//        business.setEmail(request.getEmail());
//        business.setAddress(request.getAddress());
//
//        business.setUpdatedAt(LocalDateTime.now());
//
//        return businessRepository.save(business);
//    }
    public Business updateBusiness(
            Long id,
            BusinessRequest request,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.getBusiness() == null) {
            throw new ResourceNotFoundException(
                    "Business not assigned to user");
        }

        if (!user.getBusiness().getId().equals(id)) {
            throw new ResourceNotFoundException(
                    "Business not found");
        }

        Business business = user.getBusiness();

        business.setBusinessName(request.getBusinessName());
        business.setBusinessType(request.getBusinessType());
        business.setPhone(request.getPhone());
        business.setEmail(request.getEmail());
        business.setAddress(request.getAddress());
        business.setCountryCode(request.getCountryCode());
        business.setCurrencyCode(request.getCurrencyCode());
        business.setTaxSystem(request.getTaxSystem());
        business.setTaxRegistrationNumber(
                request.getTaxRegistrationNumber()
        );

        business.setUpdatedAt(LocalDateTime.now());

        return businessRepository.save(business);
    }
//    public void deleteBusiness(Long id) {
//
//        Business business = businessRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
//
//        businessRepository.delete(business);
//    }
    public void deleteBusiness(
            Long id,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.getBusiness() == null) {
            throw new ResourceNotFoundException(
                    "Business not assigned to user");
        }

        if (!user.getBusiness().getId().equals(id)) {
            throw new ResourceNotFoundException(
                    "Business not found");
        }

        Business business = user.getBusiness();

        businessRepository.delete(business);
    }
    
}