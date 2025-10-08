package com.adisaputera.savingapp.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.message.MetadataResponse;
import com.adisaputera.savingapp.dto.response.AccountResponseDTO;
import com.adisaputera.savingapp.dto.response.TransactionResponseDTO;
import com.adisaputera.savingapp.dto.response.UserResponseDTO;
import com.adisaputera.savingapp.exception.ResourceNotFoundException;
import com.adisaputera.savingapp.model.Account;
import com.adisaputera.savingapp.model.Transaction;
import com.adisaputera.savingapp.model.User;
import com.adisaputera.savingapp.repository.AccountRepository;
import com.adisaputera.savingapp.repository.TransactionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    TransactionRepository transactionRepository;
    AccountRepository accountRepository;

    public ApiResponse<List<TransactionResponseDTO>> getTransactionByAccountCode(int page, int perPage, String accountCode, String sortDirection, String sortBy, LocalDate from, LocalDate to, String keyword) {
        Optional<Account> accountOpt = accountRepository.findByAccountCode(accountCode);
        if(accountOpt.isEmpty()) {
            throw new ResourceNotFoundException("Account", sortBy, accountCode);
        }

        int pageIndex = page > 0 ? page -1 : 0;
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        Pageable pageable = PageRequest.of(pageIndex, perPage, sort);

        from = (from == null) ? to : from;
        to   = (to == null)   ? from : to;
        
        Page<Transaction> transactionPage;
        Account account = accountOpt.get();
        if((from == null && to == null)) {
            transactionPage = transactionRepository.findByAccount_AccountCode(account.getAccountCode(), pageable);
        } else {
            transactionPage = transactionRepository.findByAccount_FullNameContainingIgonoreCaseAndAccountCodeAndCreatedAtBetween(account.getAccountCode(), from, to, keyword, pageable);
        }
        List<TransactionResponseDTO> transactionDtos = transactionPage.getContent().stream()
            .map(transaction -> {
                Account account = transaction.getAccount();
                User user = account.getUserId();
                
                return TransactionResponseDTO.builder()
                    .transactionId(transaction.getTransactionId())
                    .account(AccountResponseDTO.builder()
                        .accountCode(account.getAccountCode())
                        .isActive(account.getIsActive())
                        .totalDeposit(account.getTotalDeposit())
                        .totalWithdraw(account.getTotalWithdraw())
                        .balance(account.getBalance())
                        .user(UserResponseDTO.builder()
                            .id(user.getId().toString())
                            .fullName(user.getFullName())
                            .build())
                        .createdAt(account.getCreatedAt().toString())
                        .build())
                    .type(transaction.getType())
                    .amount(transaction.getAmount())
                    .note(transaction.getNote())
                    .occurredAt(transaction.getOccurredAt().toString())
                    .createdAt(transaction.getCreatedAt().toString())
                    .build();
            })
            .collect(Collectors.toList());

        MetadataResponse metadata = MetadataResponse.builder()
            .page(transactionPage.getNumber() + 1)
            .size(transactionPage.getSize())
            .total(transactionPage.getTotalPages())
            .build();
        return ApiResponse.success(transactionDtos, metadata);
    }
}