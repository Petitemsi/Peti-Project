package com.peti.repository;

import com.peti.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByIsLockedAndIsApproved(boolean isLocked, boolean isApproved);

    List<User> findByIsApproved(boolean isApproved);

    List<User> findByIsLocked(boolean isLocked);

    long countByIsActiveAndIsApproved(boolean isActive, boolean isApproved);
}