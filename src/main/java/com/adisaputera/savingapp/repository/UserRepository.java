package com.adisaputera.savingapp.repository;

import com.adisaputera.savingapp.model.RoleEnum;
import com.adisaputera.savingapp.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Page<User> findByFullNameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<User> findByRole(RoleEnum role, Pageable pageable);
    Page<User> findByRoleAndFullNameContainingIgnoreCase(RoleEnum role, String keyword, Pageable pageable);
    Long countByRole(RoleEnum role);
}
