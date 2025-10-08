package com.adisaputera.savingapp.service;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.message.MetadataResponse;
import com.adisaputera.savingapp.dto.request.AccountCreateRequestDTO;
import com.adisaputera.savingapp.model.Account;
import com.adisaputera.savingapp.model.User;
import com.adisaputera.savingapp.repository.AccountRepository;
import com.adisaputera.savingapp.repository.UserRepository;
import com.adisaputera.savingapp.dto.response.AccountResponseDTO;
import com.adisaputera.savingapp.dto.response.UserResponseDTO;
import com.adisaputera.savingapp.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public ApiResponse<AccountResponseDTO> createAccount(AccountCreateRequestDTO request) {
        Optional<User> userOpt = userRepository.findById(UUID.fromString(request.getUserId()));
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("Account not found");
        }

        User user = userOpt.get();

        Account account = Account.builder()
                .userId(user)
                .isActive(true)
                .totalDeposit(0L)
                .totalWithdraw(0L)
                .balance(0L)
                .build();
        Account savedAccount = accountRepository.save(account);

        AccountResponseDTO accountDto = AccountResponseDTO.builder()
                .accountCode(savedAccount.getAccountCode())
                .isActive(savedAccount.getIsActive())
                .totalDeposit(savedAccount.getTotalDeposit())
                .totalWithdraw(savedAccount.getTotalWithdraw())
                .balance(savedAccount.getBalance())
                .user(UserResponseDTO.builder()
                    .id(user.getId().toString())
                    .fullName(user.getFullName())
                    .build())
                .createdAt(savedAccount.getCreatedAt().toString())
                .build();

        return ApiResponse.success("Account created successfully", accountDto);
    }

    public ApiResponse<List<AccountResponseDTO>> getAccountList(int page, int perPage, String sortDirection, UUID userId, String keyword) {
        int pageIndex = page > 0 ? page -1 : 0;
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        Pageable pageable = PageRequest.of(pageIndex, perPage, sort);

        Page<Account> accountPage;
        if (StringUtils.hasText(keyword)) {
            accountPage = accountRepository.findByUserIdFullNameContainingIgnoreCase(keyword, pageable);
        } else {
            accountPage = accountRepository.findAll(pageable);
        }

        if (accountPage.isEmpty()) {
            throw new ResourceNotFoundException("Account not found");
        }

        // Konversi entitas User ke DTO
        List<AccountResponseDTO> accountDtos = accountPage.getContent().stream()
            .map(account -> {
                User user = account.getUserId();

                return AccountResponseDTO.builder()
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
                    .build();
            })
            .collect(Collectors.toList());

        // Buat objek pagination
        MetadataResponse pagination = MetadataResponse.builder()
            .page(accountPage.getNumber() + 1)
            .size(accountPage.getSize())
            .total(accountPage.getTotalPages())
            .build();

        return ApiResponse.success("Account retrieved successfully", accountDtos, pagination);
    }

    public ApiResponse<String> updateAccountStatus(String accountCode, Boolean status) {
        Optional<Account> accountOpt = accountRepository.findAll().stream()
            .filter(acc -> acc.getAccountCode().equals(accountCode))
            .findFirst();

        if (accountOpt.isEmpty()) {
            throw new ResourceNotFoundException("Account not found");
        }

        Account account = accountOpt.get();
        account.setIsActive(status);
        accountRepository.save(account);

        String message = status ? "Account activated successfully" : "Account deactivated successfully";
        return ApiResponse.success(message, null);
    }

    public ApiResponse<String> deleteAccount(String accountCode) {
        Optional<Account> accountOpt = accountRepository.findAll().stream()
            .filter(account -> account.getAccountCode().equals(accountCode))
            .findFirst();

        if (accountOpt.isEmpty()) {
            throw new ResourceNotFoundException("Account not found");
        }

        Account account = accountOpt.get();
        accountRepository.delete(account);

        return ApiResponse.success("Account deleted successfully", null);
    }

    public ApiResponse<List<AccountResponseDTO>> getAccountByUserId(String userId) {
        // Validasi user exists
        Optional<User> userOpt = userRepository.findById(UUID.fromString(userId));
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        User user = userOpt.get();
        List<Account> accounts = accountRepository.findAllByUserId(user);

        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("Account", "userId", userId);
        }

        // Konversi ke DTO
        List<AccountResponseDTO> accountDtos = accounts.stream()
            .map(account -> AccountResponseDTO.builder()
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
            .collect(Collectors.toList());

        return ApiResponse.success("Accounts retrieved successfully", accountDtos);
    }
}
