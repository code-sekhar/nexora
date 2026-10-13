package com.nexora.user.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.nexora.user.entity.User;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    List<User> findByBusinessId(Long businessId);
    Optional<User> findByIdAndBusinessId(Long id, Long businessId);
}