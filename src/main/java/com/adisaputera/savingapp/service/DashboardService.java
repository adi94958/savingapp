package com.adisaputera.savingapp.service;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.response.AdminDashboardResponseDTO;
import com.adisaputera.savingapp.dto.response.NasabahDashboardResponseDTO;
import com.adisaputera.savingapp.exception.ResourceNotFoundException;
import com.adisaputera.savingapp.model.Account;
import com.adisaputera.savingapp.model.RoleEnum;
import com.adisaputera.savingapp.model.Transaction;
import com.adisaputera.savingapp.model.User;
import com.adisaputera.savingapp.repository.AccountRepository;
import com.adisaputera.savingapp.repository.TransactionRepository;
import com.adisaputera.savingapp.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import com.adisaputera.savingapp.util.UserUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public ApiResponse<AdminDashboardResponseDTO> getAdminDashboard() {
        Long totalNasabah = userRepository.countByRole(RoleEnum.nasabah);
        
        Long totalAccountActive = accountRepository.countByIsActive(true);
        Long totalAccountInactive = accountRepository.countByIsActive(false);
                
        List<Account> allAccounts = accountRepository.findAll();
        Long totalDeposit = allAccounts.stream()
                .mapToLong(Account::getTotalDeposit)
                .sum();
        Long totalWithdraw = allAccounts.stream()
                .mapToLong(Account::getTotalWithdraw)
                .sum();
        Long totalBalance = allAccounts.stream()
                .mapToLong(Account::getBalance)
                .sum();

        AdminDashboardResponseDTO dashboard = AdminDashboardResponseDTO.builder()
                .totalNasabah(totalNasabah)
                .totalAccountActive(totalAccountActive)
                .totalAccountInactive(totalAccountInactive)
                .totalDeposit(totalDeposit)
                .totalWithdraw(totalWithdraw)
                .totalBalance(totalBalance)
                .build();

        return ApiResponse.success("Admin dashboard data retrieved successfully", dashboard);
    }

    public ApiResponse<NasabahDashboardResponseDTO> getNasabahDashboard(
            String accountCode, 
            LocalDate from, 
            LocalDate to,
            Boolean status) {
        
        User user = UserUtil.getCurrentLoggedInUser(userRepository);

        List<Account> accounts;
        if (accountCode != null && !accountCode.trim().isEmpty()) {
            Optional<Account> accountOpt = accountRepository.findByAccountCode(accountCode);
            if (accountOpt.isEmpty() || !accountOpt.get().getUserId().getId().equals(user.getId())) {
                throw new ResourceNotFoundException("Account not found or doesn't belong to user");
            }
            accounts = List.of(accountOpt.get());
        } else {
            accounts = accountRepository.findAllByUserId(user);
        }

        if (status != null) {
            accounts = accounts.stream()
                    .filter(account -> account.getIsActive().equals(status))
                    .toList();
        }

        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("No accounts found for this nasabah");
        }

        LocalDateTime fromDateTime = (from != null) ? from.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime toDateTime = (to != null) ? to.atTime(LocalTime.MAX) : LocalDateTime.now();

        Long totalDeposit = 0L;
        Long totalWithdraw = 0L;
        Long totalBalance = 0L;
        Integer totalAccountsActive = 0;
        Integer totalAccountsInactive = 0;

        for (Account account : accounts) {
            List<Transaction> transactions = transactionRepository.findByAccountCodeAndOccurredAtBetween(
                    account, fromDateTime, toDateTime);
            
            Long accountDeposit = transactions.stream()
                    .filter(t -> t.getType().name().equals("deposit"))
                    .mapToLong(Transaction::getAmount)
                    .sum();
            
            Long accountWithdraw = transactions.stream()
                    .filter(t -> t.getType().name().equals("withdraw"))
                    .mapToLong(Transaction::getAmount)
                    .sum();
            
            totalDeposit += accountDeposit;
            totalWithdraw += accountWithdraw;
            totalBalance += account.getBalance();

            if (account.getIsActive()) {
                totalAccountsActive++;
            } else {
                totalAccountsInactive++;
            }
        }

        NasabahDashboardResponseDTO dashboard = NasabahDashboardResponseDTO.builder()
                .totalDeposit(totalDeposit)
                .totalWithdraw(totalWithdraw)
                .totalBalance(totalBalance)
                .totalAccounts(accounts.size())
                .totalAccountsActive(totalAccountsActive)
                .totalAccountInActive(totalAccountsInactive)
                .build();

        return ApiResponse.success("Nasabah dashboard data retrieved successfully", dashboard);
    }
}
