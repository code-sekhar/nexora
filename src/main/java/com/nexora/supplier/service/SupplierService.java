package com.nexora.supplier.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.business.repository.BusinessRepository;
import com.nexora.supplier.dto.SupplierRequest;
import com.nexora.supplier.dto.SupplierResponse;
import com.nexora.supplier.entity.Supplier;
import com.nexora.supplier.exception.SupplierAlreadyExistsException;
import com.nexora.supplier.repository.SupplierRepository;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    public SupplierService(
            SupplierRepository supplierRepository,
            UserRepository userRepository,
            BusinessRepository businessRepository) {

        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
    }


    private User getCurrentUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new RuntimeException("User not found")
                );
    }


    private Business getCurrentBusiness(String email) {

        User user = getCurrentUser(email);

        if (user.getBusiness() == null) {
            throw new RuntimeException(
                "User is not assigned to any business"
            );
        }

        return user.getBusiness();
    }


    @Transactional
    public SupplierResponse createSupplier(
            SupplierRequest request,
            String email) {

        Business business = getCurrentBusiness(email);

        if (supplierRepository.existsByPhoneAndBusinessId(
                request.getPhone(),
                business.getId())) {

            throw new SupplierAlreadyExistsException(
                "Supplier with this phone already exists"
            );
        }

        Supplier supplier = new Supplier();

        supplier.setName(request.getName());
        supplier.setCompanyName(request.getCompanyName());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setCountry(request.getCountry());
        supplier.setTaxRegistrationNumber(
            request.getTaxRegistrationNumber()
        );

        supplier.setOpeningDue(
            request.getOpeningDue() == null
                ? BigDecimal.ZERO
                : request.getOpeningDue()
        );

        supplier.setNotes(request.getNotes());
        supplier.setActive(true);
        supplier.setBusiness(business);

        Supplier saved = supplierRepository.save(supplier);

        return mapToResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers(
            String email) {

        Business business = getCurrentBusiness(email);

        return supplierRepository
                .findByBusinessId(business.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(
            Long id,
            String email) {

        Business business = getCurrentBusiness(email);

        Supplier supplier = supplierRepository
                .findByIdAndBusinessId(
                    id,
                    business.getId()
                )
                .orElseThrow(() ->
                    new RuntimeException(
                        "Supplier not found"
                    )
                );

        return mapToResponse(supplier);
    }


    @Transactional
    public SupplierResponse updateSupplier(
            Long id,
            SupplierRequest request,
            String email) {

        Business business = getCurrentBusiness(email);

        Supplier supplier = supplierRepository
                .findByIdAndBusinessId(
                    id,
                    business.getId()
                )
                .orElseThrow(() ->
                    new RuntimeException(
                        "Supplier not found"
                    )
                );

        if (supplierRepository
                .existsByPhoneAndBusinessIdAndIdNot(
                    request.getPhone(),
                    business.getId(),
                    id
                )) {

            throw new SupplierAlreadyExistsException(
                "Another supplier with this phone already exists"
            );
        }

        supplier.setName(request.getName());
        supplier.setCompanyName(request.getCompanyName());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setCountry(request.getCountry());
        supplier.setTaxRegistrationNumber(
            request.getTaxRegistrationNumber()
        );

        supplier.setOpeningDue(
            request.getOpeningDue() == null
                ? BigDecimal.ZERO
                : request.getOpeningDue()
        );

        supplier.setNotes(request.getNotes());

        Supplier updated =
            supplierRepository.save(supplier);

        return mapToResponse(updated);
    }


    @Transactional
    public SupplierResponse updateStatus(
            Long id,
            Boolean active,
            String email) {

        Business business = getCurrentBusiness(email);

        Supplier supplier = supplierRepository
                .findByIdAndBusinessId(
                    id,
                    business.getId()
                )
                .orElseThrow(() ->
                    new RuntimeException(
                        "Supplier not found"
                    )
                );

        supplier.setActive(active);

        Supplier updated =
            supplierRepository.save(supplier);

        return mapToResponse(updated);
    }


    private SupplierResponse mapToResponse(
            Supplier supplier) {

        SupplierResponse response =
            new SupplierResponse();

        response.setId(supplier.getId());
        response.setName(supplier.getName());
        response.setCompanyName(
            supplier.getCompanyName()
        );
        response.setPhone(supplier.getPhone());
        response.setEmail(supplier.getEmail());
        response.setAddress(supplier.getAddress());
        response.setCity(supplier.getCity());
        response.setState(supplier.getState());
        response.setCountry(supplier.getCountry());

        response.setTaxRegistrationNumber(
            supplier.getTaxRegistrationNumber()
        );

        response.setOpeningDue(
            supplier.getOpeningDue()
        );

        // Purchase/Payment module 
        //  actual ledger-based due calculate
        response.setCurrentDue(
            supplier.getOpeningDue()
        );

        response.setNotes(supplier.getNotes());
        response.setActive(supplier.isActive());
        response.setCreatedAt(supplier.getCreatedAt());
        response.setUpdatedAt(supplier.getUpdatedAt());

        return response;
    }
}