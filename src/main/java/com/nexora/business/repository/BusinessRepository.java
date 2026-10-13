package com.nexora.business.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.nexora.business.entity.Business;

public interface BusinessRepository extends JpaRepository<Business, Long> {
	boolean existsByBusinessName(String businessName);
}