package com.adisaputera.savingapp.repository;

import com.adisaputera.savingapp.model.Account;
import com.adisaputera.savingapp.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserId(User userId);
    Page<Account> findByUserIdFullNameContainingIgnoreCase(String keyword, Pageable pageable);
    List<Account> findAllByUserId(User user);
    Optional<Account> findByAccountCode(String accountCode);
    Page<Account> findAllByAccountCode(Account accountCode);
}
