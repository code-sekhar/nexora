package com.nexora.category.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexora.business.entity.Business;
import com.nexora.category.dto.CategoryRequest;
import com.nexora.category.dto.CategoryResponse;
import com.nexora.category.dto.CategoryStatusRequest;
import com.nexora.category.entity.Category;
import com.nexora.category.repository.CategoryRepository;
import com.nexora.exception.ResourceNotFoundException;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            UserRepository userRepository) {

        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }


    // CREATE
    public CategoryResponse createCategory(
            CategoryRequest request,
            String email) {

        User loggedInUser = getLoggedInUser(email);

        Business business = getUserBusiness(loggedInUser);

        if (categoryRepository.existsByNameAndBusinessId(
                request.getName(),
                business.getId())) {

            throw new IllegalArgumentException(
                    "Category already exists");
        }

        Category category = new Category();

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setActive(true);
        category.setBusiness(business);

        // Parent / Subcategory
        if (request.getParentId() != null) {

            Category parent = categoryRepository
                    .findByIdAndBusinessId(
                            request.getParentId(),
                            business.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Parent category not found"));

            category.setParent(parent);
        }

        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        Category savedCategory =
                categoryRepository.save(category);

        return mapToResponse(savedCategory);
    }


    // GET ALL
    public List<CategoryResponse> getAllCategories(
            String email) {

        User loggedInUser = getLoggedInUser(email);

        Business business = getUserBusiness(loggedInUser);

        return categoryRepository
                .findByBusinessId(business.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // GET BY ID
    public CategoryResponse getCategoryById(
            Long id,
            String email) {

        User loggedInUser = getLoggedInUser(email);

        Business business = getUserBusiness(loggedInUser);

        Category category = categoryRepository
                .findByIdAndBusinessId(
                        id,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"));

        return mapToResponse(category);
    }


    // UPDATE
    public CategoryResponse updateCategory(
            Long id,
            CategoryRequest request,
            String email) {

        User loggedInUser = getLoggedInUser(email);

        Business business = getUserBusiness(loggedInUser);

        Category category = categoryRepository
                .findByIdAndBusinessId(
                        id,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"));

        if (categoryRepository
                .existsByNameAndBusinessIdAndIdNot(
                        request.getName(),
                        business.getId(),
                        id)) {

            throw new IllegalArgumentException(
                    "Category already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        // Parent / Subcategory
        if (request.getParentId() != null) {

            if (request.getParentId().equals(id)) {

                throw new IllegalArgumentException(
                        "Category cannot be its own parent");
            }

            Category parent = categoryRepository
                    .findByIdAndBusinessId(
                            request.getParentId(),
                            business.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Parent category not found"));

            category.setParent(parent);

        } else {

            category.setParent(null);
        }

        category.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory =
                categoryRepository.save(category);

        return mapToResponse(updatedCategory);
    }


    // DELETE
//    public void deleteCategory(
//            Long id,
//            String email) {
//
//        User loggedInUser = getLoggedInUser(email);
//
//        Business business = getUserBusiness(loggedInUser);
//
//        Category category = categoryRepository
//                .findByIdAndBusinessId(
//                        id,
//                        business.getId())
//                .orElseThrow(() ->
//                        new ResourceNotFoundException(
//                                "Category not found"));
//
//        categoryRepository.delete(category);
//    }
    public void deleteCategory(
            Long id,
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        Category category = categoryRepository
                .findByIdAndBusinessId(
                        id,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"));

        if (categoryRepository.existsByParentId(id)) {
            throw new IllegalArgumentException(
                    "Cannot delete category because it has subcategories");
        }

        categoryRepository.delete(category);
    }


    // Logged-in user
    private User getLoggedInUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));
    }


    // User's business
    private Business getUserBusiness(User user) {

        if (user.getBusiness() == null) {

            throw new ResourceNotFoundException(
                    "Business not assigned to user");
        }

        return user.getBusiness();
    }


    // Entity → DTO
    private CategoryResponse mapToResponse(
            Category category) {

        Long parentId = null;

        if (category.getParent() != null) {
            parentId = category.getParent().getId();
        }

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                parentId,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
    //Update status
    public CategoryResponse updateCategoryStatus(
            Long id,
            CategoryStatusRequest request,
            String email) {

        User loggedInUser = getLoggedInUser(email);
        Business business = getUserBusiness(loggedInUser);

        Category category = categoryRepository
                .findByIdAndBusinessId(
                        id,
                        business.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"));

        category.setActive(request.isActive());
        category.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory =
                categoryRepository.save(category);

        return mapToResponse(updatedCategory);
    }
    
}