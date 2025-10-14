package com.adisaputera.savingapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.message.MetadataResponse;
import com.adisaputera.savingapp.dto.request.CreateTransactionRequestDTO;
import com.adisaputera.savingapp.dto.response.TransactionResponseDTO;
import com.adisaputera.savingapp.exception.BadRequestException;
import com.adisaputera.savingapp.exception.ForbiddenException;
import com.adisaputera.savingapp.exception.ResourceNotFoundException;
import com.adisaputera.savingapp.model.Account;
import com.adisaputera.savingapp.model.Transaction;
import com.adisaputera.savingapp.model.TypeTransactionEnum;
import com.adisaputera.savingapp.model.User;
import com.adisaputera.savingapp.repository.AccountRepository;
import com.adisaputera.savingapp.repository.TransactionRepository;
import com.adisaputera.savingapp.repository.UserRepository;
import com.adisaputera.savingapp.util.UserUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public ApiResponse<List<TransactionResponseDTO>> getTransactionByAccountCodeForAdmin(int page, int perPage, String accountCode, String sortDirection, String sortBy, LocalDate from, LocalDate to) {
        // Admin bisa akses semua account tanpa validasi ownership
        Optional<Account> accountOpt = accountRepository.findByAccountCode(accountCode);
        if(accountOpt.isEmpty()) {
            throw new ResourceNotFoundException("Account not found");
        }
        
        Account account = accountOpt.get();
    return getTransactionListByAccount(page, perPage, account, sortDirection, sortBy, from, to);
    }

    public ApiResponse<List<TransactionResponseDTO>> getTransactionByAccountCodeForNasabah(int page, int perPage, String accountCode, String sortDirection, String sortBy, LocalDate from, LocalDate to) {
        // Nasabah hanya bisa akses account milik sendiri
        User currentUser = UserUtil.getCurrentLoggedInUser(userRepository);
        
        Optional<Account> accountOpt = accountRepository.findByAccountCode(accountCode);
        if(accountOpt.isEmpty()) {
            throw new ResourceNotFoundException("Account not found");
        }
        
        Account account = accountOpt.get();
        
        // Validasi ownership
        if (!account.getUserId().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only access your own account transactions");
        }
        
    return getTransactionListByAccount(page, perPage, account, sortDirection, sortBy, from, to);
    }

    // Helper method untuk logic yang sama
    private ApiResponse<List<TransactionResponseDTO>> getTransactionListByAccount(int page, int perPage, Account account, String sortDirection, String sortBy, LocalDate from, LocalDate to) {
        if ((from != null && to == null) || (from == null && to != null)) {
            throw new BadRequestException("Both 'from' and 'to' parameters must be provided together or both should be empty");
        }

        int pageIndex = page > 0 ? page - 1 : 0;
        Sort sort = sortDirection.equals("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(pageIndex, perPage, sort);

        // Set default values untuk filter
        LocalDateTime fromDateTime = (from != null) ? from.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime toDateTime = (to != null) ? to.atTime(LocalTime.MAX) : LocalDateTime.now();
        Page<Transaction> transactionPage;
        if (from != null || to != null) {
            transactionPage = transactionRepository.findByAccountCode_AccountCodeAndOccurredAtBetween(
                account.getAccountCode(), fromDateTime, toDateTime, pageable);
        } else {
            transactionPage = transactionRepository.findByAccountCode_AccountCode(account.getAccountCode(), pageable);
        }

        if (transactionPage.isEmpty()) {
            throw new ResourceNotFoundException("Transaction", "accountCode", account.getAccountCode());
        }

        List<TransactionResponseDTO> transactionDtos = transactionPage.getContent().stream()
            .map(transaction -> {
                Account transactionAccount = transaction.getAccountCode();
                
                return TransactionResponseDTO.builder()
                    .transactionId(transaction.getId())
                    .accountCode(transactionAccount.getAccountCode())
                    .type(transaction.getType())
                    .amount(transaction.getAmount())
                    .balance(transaction.getBalance())
                    .note(transaction.getNote())
                    .occurredAt(transaction.getOccurredAt())
                    .createdAt(transaction.getCreatedAt())
                    .build();
            })
            .collect(Collectors.toList());

        MetadataResponse metadata = MetadataResponse.builder()
            .page(transactionPage.getNumber() + 1)
            .size(transactionPage.getSize())
            .total(transactionPage.getTotalPages())
            .build();
            
        return ApiResponse.success("Transactions retrieved successfully", transactionDtos, metadata);
    }

    public ApiResponse<TransactionResponseDTO> createTransaction(CreateTransactionRequestDTO request) {
        // validation account exists
        Optional<Account> accountOpt = accountRepository.findByAccountCode(request.getAccountCode());
        if (accountOpt.isEmpty()) {
            throw new ResourceNotFoundException("Account", "accountCode", request.getAccountCode());
        }

        Account account = accountOpt.get();

        // validation account is active
        if (!account.getIsActive()) {
            throw new BadRequestException("Account " + request.getAccountCode() + " is not active");
        }

        // validation withdraw balance
        if (request.getType() == TypeTransactionEnum.withdraw) {
            if (account.getBalance() < request.getAmount()) {
                throw new BadRequestException("Withdrawal rejected: Insufficient balance. Current balance: " + account.getBalance() + ", Requested amount: " + request.getAmount());
            }
        }

        // Update balance account dan hitung new balance
        Long newBalance;
        if (request.getType() == TypeTransactionEnum.deposit) {
            account.setTotalDeposit(account.getTotalDeposit() + request.getAmount());
            newBalance = account.getBalance() + request.getAmount();
            account.setBalance(newBalance);
        } else if (request.getType() == TypeTransactionEnum.withdraw) {
            account.setTotalWithdraw(account.getTotalWithdraw() + request.getAmount());
            newBalance = account.getBalance() - request.getAmount();
            account.setBalance(newBalance);
        } else {
            newBalance = account.getBalance();
        }

        // Create new transaction dengan balance snapshot
        Transaction transaction = Transaction.builder()
                .accountCode(account)
                .type(request.getType())
                .amount(request.getAmount())
                .balance(newBalance)
                .note(request.getNote())
                .occurredAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        // Save transaction
        transaction = transactionRepository.saveAndFlush(transaction);

        // Save account
        accountRepository.saveAndFlush(account);

        // Build response
        TransactionResponseDTO responseDTO = TransactionResponseDTO.builder()
                .transactionId(transaction.getId())
                .accountCode(account.getAccountCode())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balance(account.getBalance())
                .note(transaction.getNote())
                .occurredAt(transaction.getOccurredAt())
                .createdAt(transaction.getCreatedAt())
                .build();

        return ApiResponse.success("Transaction created successfully", responseDTO);
    }
}