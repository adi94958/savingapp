package com.adisaputera.savingapp.service;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.message.MetadataResponse;
import com.adisaputera.savingapp.dto.request.ChangeAccountStatusRequestDTO;
import com.adisaputera.savingapp.dto.request.CreateAccountRequestDTO;
import com.adisaputera.savingapp.model.Account;
import com.adisaputera.savingapp.model.User;
import com.adisaputera.savingapp.repository.AccountRepository;
import com.adisaputera.savingapp.repository.UserRepository;
import com.adisaputera.savingapp.util.UserUtil;
import com.adisaputera.savingapp.dto.response.AccountDetailResponseDTO;
import com.adisaputera.savingapp.dto.response.AccountListResponseDTO;
import com.adisaputera.savingapp.dto.response.AccountMeResponseDTO;
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

    public ApiResponse<AccountListResponseDTO> createAccount(CreateAccountRequestDTO request) {
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

        AccountListResponseDTO accountDto = AccountListResponseDTO.builder()
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

    public ApiResponse<List<AccountListResponseDTO>> getAccountList(int page, int perPage, String sortDirection, String sortBy, UUID userId, String keyword) {
        int pageIndex = page > 0 ? page -1 : 0;
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageIndex, perPage, sort);

        Page<Account> accountPage;
        
        // Filter by userId and keyword
        if (userId != null && StringUtils.hasText(keyword)) {
            // Filter by specific user and search by accountCode
            accountPage = accountRepository.findByUserIdIdAndAccountCodeContainingIgnoreCase(userId, keyword, pageable);
        } else if (userId != null) {
            // Filter by specific user only
            accountPage = accountRepository.findByUserIdId(userId, pageable);
        } else if (StringUtils.hasText(keyword)) {
            // Search by accountCode only
            accountPage = accountRepository.findByAccountCodeContainingIgnoreCase(keyword, pageable);
        } else {
            // No filter, get all accounts
            accountPage = accountRepository.findAll(pageable);
        }

        // Konversi entitas User ke DTO
        List<AccountListResponseDTO> accountDtos = accountPage.getContent().stream()
            .map(account -> {
                User user = account.getUserId();

                return AccountListResponseDTO.builder()
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

    public ApiResponse<String> updateAccountStatus(ChangeAccountStatusRequestDTO request) {        
        Optional<Account> accountOpt = accountRepository.findAll().stream()
            .filter(acc -> acc.getAccountCode().equals(request.getAccountCode()))
            .findFirst();

        if (accountOpt.isEmpty()) {
            throw new ResourceNotFoundException("Account not found");
        }

        Account account = accountOpt.get();
        account.setIsActive(request.getIsActive());
        accountRepository.save(account);

        String message = Boolean.TRUE.equals(request.getIsActive()) ? "Account activated successfully" : "Account deactivated successfully";
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

    public ApiResponse<AccountDetailResponseDTO> getAccountDetail(String accountCode) {
        Optional<Account> accountOpt = accountRepository.findByAccountCode(accountCode);

        if (accountOpt.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with code: " + accountCode);
        }

        Account account = accountOpt.get();
        User user = account.getUserId();

        AccountDetailResponseDTO accountDetail = AccountDetailResponseDTO.builder()
                .accountCode(account.getAccountCode())
                .isActive(account.getIsActive())
                .balance(account.getBalance().doubleValue())
                .totalDeposit(account.getTotalDeposit().doubleValue())
                .totalWithdraw(account.getTotalWithdraw().doubleValue())
                .createdAt(account.getCreatedAt() != null ? account.getCreatedAt().toString() : "")
                .user(UserResponseDTO.builder()
                        .id(user.getId().toString())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .address(user.getAddress())
                        .phone(user.getPhone())
                        .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "")
                        .build())
                .build();

        return ApiResponse.success("Account detail retrieved successfully", accountDetail);
    }

    public ApiResponse<List<AccountMeResponseDTO>> getAccountMe(int page, int perPage, String sortDirection, String sortBy, String keyword) {
        User user = UserUtil.getCurrentLoggedInUser(userRepository);
        
        int pageIndex = page > 0 ? page - 1 : 0;
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageIndex, perPage, sort);

        Page<Account> accountPage;

        if (StringUtils.hasText(keyword)) {
            // Search user's accounts by accountCode
            accountPage = accountRepository.findByUserIdIdAndAccountCodeContainingIgnoreCase(user.getId(), keyword, pageable);
        } else {
            // Get all user's accounts
            accountPage = accountRepository.findAllByUserId(user, pageable);
        }

        if (accountPage.isEmpty()) {
            throw new ResourceNotFoundException("Account not found");
        }


        // Konversi entitas Account ke DTO
        List<AccountMeResponseDTO> accountDtos = accountPage.getContent().stream()
            .map(account -> AccountMeResponseDTO.builder()
                .accountCode(account.getAccountCode())
                .isActive(account.getIsActive())
                .totalDeposit(account.getTotalDeposit())
                .totalWithdraw(account.getTotalWithdraw())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt().toString())
                .build())
            .collect(Collectors.toList());

        // Buat objek pagination
        MetadataResponse pagination = MetadataResponse.builder()
            .page(accountPage.getNumber() + 1)
            .size(accountPage.getSize())
            .total(accountPage.getTotalPages())
            .build();

        return ApiResponse.success("Account retrieved successfully", accountDtos, pagination);
    }
}
