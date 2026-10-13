package com.nexora.expense.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.expense.dto.ExpenseCategoryRequest;
import com.nexora.expense.dto.ExpenseCategoryResponse;
import com.nexora.expense.entity.ExpenseCategory;
import com.nexora.expense.repository.ExpenseCategoryRepository;

@Service
@Transactional
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository repository;

    public ExpenseCategoryService(
            ExpenseCategoryRepository repository) {
        this.repository = repository;
    }

    public ExpenseCategoryResponse create(
            Business business,
            ExpenseCategoryRequest request) {

        validateRequest(request);

        String name = request.getName().trim();

        if (repository.existsByBusinessIdAndName(
                business.getId(),
                name)) {

            throw new IllegalArgumentException(
                    "Expense category already exists: " + name
            );
        }

        ExpenseCategory category =
                new ExpenseCategory();

        category.setBusiness(business);
        category.setName(name);
        category.setDescription(
                request.getDescription()
        );

        if (request.getActive() != null) {
            category.setActive(
                    request.getActive()
            );
        }

        ExpenseCategory saved =
                repository.save(category);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseCategoryResponse> getAll(
            Business business) {

        return repository
                .findByBusinessIdOrderByNameAsc(
                        business.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseCategoryResponse getById(
            Business business,
            Long id) {

        ExpenseCategory category =
                repository
                        .findByIdAndBusinessId(
                                id,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Expense category not found"
                                )
                        );

        return mapToResponse(category);
    }

    public ExpenseCategoryResponse update(
            Business business,
            Long id,
            ExpenseCategoryRequest request) {

        validateRequest(request);

        ExpenseCategory category =
                repository
                        .findByIdAndBusinessId(
                                id,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Expense category not found"
                                )
                        );

        String name = request.getName().trim();

        if (repository
                .existsByBusinessIdAndNameAndIdNot(
                        business.getId(),
                        name,
                        id
                )) {

            throw new IllegalArgumentException(
                    "Expense category already exists: " + name
            );
        }

        category.setName(name);
        category.setDescription(
                request.getDescription()
        );

        if (request.getActive() != null) {
            category.setActive(
                    request.getActive()
            );
        }

        ExpenseCategory updated =
                repository.save(category);

        return mapToResponse(updated);
    }

    public void delete(
            Business business,
            Long id) {

        ExpenseCategory category =
                repository
                        .findByIdAndBusinessId(
                                id,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Expense category not found"
                                )
                        );

        /*
         * Later when Expense entity is connected,
         * hard delete should be prevented if expenses
         * are using this category.
         *
         * For now we deactivate instead.
         */
        category.setActive(false);

        repository.save(category);
    }

    private void validateRequest(
            ExpenseCategoryRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request is required"
            );
        }

        if (request.getName() == null ||
                request.getName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Expense category name is required"
            );
        }

        if (request.getName().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Expense category name cannot exceed 100 characters"
            );
        }
    }

    private ExpenseCategoryResponse mapToResponse(
            ExpenseCategory category) {

        ExpenseCategoryResponse response =
                new ExpenseCategoryResponse();

        response.setId(category.getId());

        response.setBusinessId(
                category.getBusiness().getId()
        );

        response.setName(
                category.getName()
        );

        response.setDescription(
                category.getDescription()
        );

        response.setActive(
                category.getActive()
        );

        response.setCreatedAt(
                category.getCreatedAt()
        );

        response.setUpdatedAt(
                category.getUpdatedAt()
        );

        return response;
    }
}