package com.nexora.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByBusinessId(Long businessId);

    Optional<Category> findByIdAndBusinessId(
            Long id,
            Long businessId
    );

    boolean existsByNameAndBusinessId(
            String name,
            Long businessId
    );

    boolean existsByNameAndBusinessIdAndIdNot(
            String name,
            Long businessId,
            Long id
    );
    boolean existsByParentId(Long parentId);
}