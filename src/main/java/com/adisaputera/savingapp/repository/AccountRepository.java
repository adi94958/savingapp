package com.adisaputera.savingapp.repository;

import com.adisaputera.savingapp.model.Account;
import com.adisaputera.savingapp.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserId(User userId);
    List<Account> findAllByUserId(User user);
    Page<Account> findAllByUserId(User user, Pageable pageable);
    Optional<Account> findByAccountCode(String accountCode);
    Page<Account> findAllByAccountCode(Account accountCode, Pageable pageable);
    Long countByIsActive(Boolean isActive);
    
    // Method untuk filter by userId dengan UUID
    @Query("SELECT a FROM Account a WHERE a.userId.id = :userId")
    Page<Account> findByUserIdId(@Param("userId") UUID userId, Pageable pageable);
    
    // Method untuk search by accountCode
    @Query("SELECT a FROM Account a WHERE LOWER(a.accountCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Account> findByAccountCodeContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);
    
    // Method untuk filter by userId dan search accountCode
    @Query("SELECT a FROM Account a WHERE a.userId.id = :userId AND LOWER(a.accountCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Account> findByUserIdIdAndAccountCodeContainingIgnoreCase(@Param("userId") UUID userId, @Param("keyword") String keyword, Pageable pageable);
}
