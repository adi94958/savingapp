package com.adisaputera.savingapp.config;

import com.adisaputera.savingapp.model.Account;
import com.adisaputera.savingapp.model.RoleEnum;
import com.adisaputera.savingapp.model.Transaction;
import com.adisaputera.savingapp.model.TypeTransactionEnum;
import com.adisaputera.savingapp.model.User;
import com.adisaputera.savingapp.repository.AccountRepository;
import com.adisaputera.savingapp.repository.TransactionRepository;
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
            TransactionRepository transactionRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // ==== Staff ====
            User staff = userRepository.findByEmail("adi@example.com")
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
                    User savedStaff = userRepository.save(newStaff);
                    System.out.println(">>> Staff UUID: " + savedStaff.getId());
                    return savedStaff;
                });
            System.out.println(">>> Staff " + staff.getFullName() + " - UUID: " + staff.getId());

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
                        User savedNasabah = userRepository.save(newNasabah);
                        System.out.println(">>> Nasabah 1 UUID: " + savedNasabah.getId());
                        return savedNasabah;
                    });
            System.out.println(">>> Nasabah 1 " + nasabah1.getFullName() + " - UUID: " + nasabah1.getId());

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
                        User savedNasabah = userRepository.save(newNasabah);
                        System.out.println(">>> Nasabah 2 UUID: " + savedNasabah.getId());
                        return savedNasabah;
                    });
            System.out.println(">>> Nasabah 2 " + nasabah2.getFullName() + " - UUID: " + nasabah2.getId());

            // ==== Buat rekening ====
            createAccountsForNasabah(nasabah1, accountRepository, transactionRepository);
            createAccountsForNasabah(nasabah2, accountRepository, transactionRepository);
        };
    }

    private void createAccountsForNasabah(User nasabah, AccountRepository accountRepository, TransactionRepository transactionRepository) {
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

                // Simpan accounts
                activeAccount = accountRepository.saveAndFlush(activeAccount);
                inactiveAccount = accountRepository.saveAndFlush(inactiveAccount);

                // Buat sample transaksi untuk rekening aktif
                createSampleTransactions(activeAccount, transactionRepository);

                System.out.println(">>> 2 rekening untuk " + nasabah.getFullName() + " berhasil dibuat:");
                System.out.println("    - " + activeAccount.getAccountCode() + " (Aktif, Saldo: " + activeAccount.getBalance() + ")");
                System.out.println("    - " + inactiveAccount.getAccountCode() + " (Nonaktif, Saldo: " + inactiveAccount.getBalance() + ")");
            } else {
                System.out.println(">>> " + nasabah.getFullName() + " sudah punya rekening, skip.");
            }
        }
    }

    private void createSampleTransactions(Account account, TransactionRepository transactionRepository) {
        Long currentBalance = 0L;
        
        // Transaksi deposit pertama
        currentBalance += 500_000L;
        Transaction deposit1 = Transaction.builder()
                .accountCode(account)
                .type(TypeTransactionEnum.deposit)
                .amount(500_000L)
                .balance(currentBalance) // 500,000
                .note("Setoran awal")
                .occurredAt(LocalDateTime.now().minusDays(10))
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();

        // Transaksi deposit kedua
        currentBalance += 500_000L;
        Transaction deposit2 = Transaction.builder()
                .accountCode(account)
                .type(TypeTransactionEnum.deposit)
                .amount(500_000L)
                .balance(currentBalance) // 1,000,000
                .note("Setoran tambahan")
                .occurredAt(LocalDateTime.now().minusDays(5))
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        // Transaksi withdraw
        currentBalance -= 200_000L;
        Transaction withdraw1 = Transaction.builder()
                .accountCode(account)
                .type(TypeTransactionEnum.withdraw)
                .amount(200_000L)
                .balance(currentBalance) // 800,000
                .note("Tarik tunai ATM")
                .occurredAt(LocalDateTime.now().minusDays(2))
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        // Simpan transaksi
        transactionRepository.saveAndFlush(deposit1);
        transactionRepository.saveAndFlush(deposit2);
        transactionRepository.saveAndFlush(withdraw1);

        System.out.println("    >>> 3 transaksi sample untuk " + account.getAccountCode() + " berhasil dibuat dengan balance snapshot");
    }
}
