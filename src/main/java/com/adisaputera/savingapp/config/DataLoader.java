package com.adisaputera.savingapp.config;

import com.adisaputera.savingapp.model.Account;
import com.adisaputera.savingapp.model.RoleEnum;
import com.adisaputera.savingapp.model.User;
import com.adisaputera.savingapp.repository.AccountRepository;
import com.adisaputera.savingapp.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataLoader {

    @Bean
    @Transactional
    public CommandLineRunner createInitialData(
            UserRepository userRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // ==== Staff ====
            userRepository.findByEmail("adi@example.com")
                .orElseGet(() -> {
                    User newStaff = User.builder()
                            .fullName("Adi Saputera")
                            .email("adi@example.com")
                            .password(passwordEncoder.encode("password123"))
                            .role(RoleEnum.staff)
                            .address("Jalan Staff No. 1")
                            .phone("08123456789")
                            .createdAt(LocalDateTime.now())
                            .build();
                    return userRepository.save(newStaff);
                });

            // ==== Nasabah 1 ====
            User nasabah1 = userRepository.findByEmail("shandina@example.com")
                    .orElseGet(() -> {
                        User newNasabah = User.builder()
                                .fullName("Shandina Aulia")
                                .email("shandina@example.com")
                                .password(passwordEncoder.encode("password123"))
                                .role(RoleEnum.nasabah)
                                .address("Jalan Nasabah No. 1")
                                .phone("08123456789")
                                .createdAt(LocalDateTime.now())
                                .build();
                        return userRepository.save(newNasabah);
                    });

            // ==== Nasabah 2 ====
            User nasabah2 = userRepository.findByEmail("adrian@example.com")
                    .orElseGet(() -> {
                        User newNasabah = User.builder()
                                .fullName("Adrian Eka")
                                .email("adrian@example.com")
                                .password(passwordEncoder.encode("password123"))
                                .role(RoleEnum.nasabah)
                                .address("Jalan Nasabah No. 2")
                                .phone("08987654321")
                                .createdAt(LocalDateTime.now())
                                .build();
                        return userRepository.save(newNasabah);
                    });

            // ==== Buat rekening ====
            createAccountsForNasabah(nasabah1, accountRepository);
            createAccountsForNasabah(nasabah2, accountRepository);
        };
    }

    private void createAccountsForNasabah(User nasabah, AccountRepository accountRepository) {
        if (nasabah.getRole() == RoleEnum.nasabah) {
            List<Account> existingAccounts = accountRepository.findAllByUserId(nasabah);

            if (existingAccounts.isEmpty()) {
                // Rekening aktif
                Account activeAccount = Account.builder()
                        .userId(nasabah)
                        .isActive(true)
                        .totalDeposit(1_000_000L)
                        .totalWithdraw(200_000L)
                        .balance(800_000L)
                        .createdAt(LocalDateTime.now())
                        .build();

                // Rekening nonaktif
                Account inactiveAccount = Account.builder()
                        .userId(nasabah)
                        .isActive(false)
                        .totalDeposit(500_000L)
                        .totalWithdraw(500_000L)
                        .balance(0L)
                        .createdAt(LocalDateTime.now())
                        .build();

                // Simpan pertama kali (accountCode masih null)
                activeAccount = accountRepository.saveAndFlush(activeAccount);
                inactiveAccount = accountRepository.saveAndFlush(inactiveAccount);

                // Simpan lagi untuk update accountCode (karena diisi @PostPersist)
                if (activeAccount.getAccountCode() == null) {
                    activeAccount.setAccountCode(String.format("ACC-%06d", activeAccount.getId()));
                    accountRepository.saveAndFlush(activeAccount);
                }

                if (inactiveAccount.getAccountCode() == null) {
                    inactiveAccount.setAccountCode(String.format("ACC-%06d", inactiveAccount.getId()));
                    accountRepository.saveAndFlush(inactiveAccount);
                }

                System.out.println(">>> 2 rekening untuk " + nasabah.getFullName() + " berhasil dibuat:");
                System.out.println("    - " + activeAccount.getAccountCode() + " (Aktif, Saldo: " + activeAccount.getBalance() + ")");
                System.out.println("    - " + inactiveAccount.getAccountCode() + " (Nonaktif, Saldo: " + inactiveAccount.getBalance() + ")");
            } else {
                System.out.println(">>> " + nasabah.getFullName() + " sudah punya rekening, skip.");
            }
        }
    }
}
