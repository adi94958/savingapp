package com.adisaputera.savingapp.repository;

import com.adisaputera.savingapp.model.Transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findByAccount_AccountCode(String accountCode, Pageable pageable);
    Page<Transaction> findByAccount_FullNameContainingIgonoreCaseAndAccountCodeAndCreatedAtBetween(String accountCode, LocalDate from, LocalDate to, String keyword, Pageable pageable);
}
