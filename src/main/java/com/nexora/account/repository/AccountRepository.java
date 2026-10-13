package com.nexora.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.account.entity.Account;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository
        extends JpaRepository<Account, Long> {

    List<Account> findByBusinessIdOrderByNameAsc(
            Long businessId
    );

    Optional<Account> findByIdAndBusinessId(
            Long id,
            Long businessId
    );
    
    @Query(value = """
    	    SELECT *
    	    FROM accounts
    	    WHERE id = :id
    	    AND business_id = :businessId
    	    FOR UPDATE
    	    """, nativeQuery = true)
    Optional<Account> findAccountForUpdate(
    	    @Param("id") Long id,
    	    @Param("businessId") Long businessId
    );

    boolean existsByBusinessIdAndName(
            Long businessId,
            String name
    );

    boolean existsByBusinessIdAndNameAndIdNot(
            Long businessId,
            String name,
            Long id
    );
    
    
}