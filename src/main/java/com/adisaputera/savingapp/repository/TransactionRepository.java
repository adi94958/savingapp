package com.adisaputera.savingapp.repository;

import com.adisaputera.savingapp.model.Account;
import com.adisaputera.savingapp.model.Transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findByAccountCode_AccountCode(String accountCode, Pageable pageable);
    
    Page<Transaction> findByAccountCode_AccountCodeAndOccurredAtBetweenAndNoteContainingIgnoreCase(
        String accountCode, 
        LocalDateTime from, 
        LocalDateTime to, 
        String keyword, 
        Pageable pageable
    );
    
    List<Transaction> findByAccountCodeAndOccurredAtBetween(
        Account account, 
        LocalDateTime from, 
        LocalDateTime to
    );
}
