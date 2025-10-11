package com.adisaputera.savingapp.config;

import com.adisaputera.savingapp.model.Account;
import com.adisaputera.savingapp.model.RoleEnum;
import com.adisaputera.savingapp.model.Transaction;
import com.adisaputera.savingapp.model.TypeTransactionEnum;
import com.adisaputera.savingapp.model.User;
import com.adisaputera.savingapp.repository.AccountRepository;
import com.adisaputera.savingapp.repository.TransactionRepository;
import com.adisaputera.savingapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
public class DataLoader {

    private static final String DEFAULT_PASSWORD = "password123";

    @Bean
    @Transactional
    public CommandLineRunner createInitialData(
            UserRepository userRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            int createdUsers = 0;
            int createdAccounts = 0;
            int createdTxs = 0;

            // ==== 1) Admin (tetap) ====
            User admin = ensureUser(
                    userRepository,
                    passwordEncoder,
                    "Adi Saputera",
                    "adi@example.com",
                    RoleEnum.admin,
                    "Jalan Admin No. 1, Majalengka",
                    "08123456789"
            );

            // ==== 2) Seed nasabah nama realistis ====
            List<UserSeed> seeds = new ArrayList<>(List.of(
                    new UserSeed("Shandina Aulia", "shandina@example.com", "Jalan Teratai No. 5, Bandung", "08122000" + rnd4()),
                    new UserSeed("Adrian Eka", "adrian@example.com", "Jalan Melati No. 12, Cimahi", "0898765" + rnd4()),
                    new UserSeed("Rizky Ramadhan", "rizky.ramadhan@example.com", "Jalan Sukajadi No. 21, Bandung", "0812211" + rnd4()),
                    new UserSeed("Putri Maharani", "putri.maharani@example.com", "Jalan Cibogo No. 7, Bandung", "0821222" + rnd4()),
                    new UserSeed("Fajar Nugraha", "fajar.nugraha@example.com", "Jalan Cikutra No. 10, Bandung", "0858223" + rnd4()),
                    new UserSeed("Laras Kusuma", "laras.kusuma@example.com", "Jalan Buah Batu No. 88, Bandung", "0813901" + rnd4()),
                    new UserSeed("Andi Pratama", "andi.pratama@example.com", "Jalan Kopo No. 33, Bandung", "0821990" + rnd4()),
                    new UserSeed("Salsabila Zahra", "salsa.zahra@example.com", "Jalan Asia Afrika No. 1, Bandung", "0813777" + rnd4()),
                    new UserSeed("Rina Puspita", "rina.puspita@example.com", "Jalan Dago Atas No. 14, Bandung", "0812333" + rnd4()),
                    new UserSeed("Dio Mahendra", "dio.mahendra@example.com", "Jalan Antapani No. 4, Bandung", "0838123" + rnd4())
            ));

            // Tambahkan 15 user generik (idempotent by email)
            for (int i = 1; i <= 15; i++) {
                String name = "User " + String.format("%02d", i);
                String email = "user" + String.format("%02d", i) + "@example.com";
                String addr = "Jalan Mawar Blok " + (char) ('A' + (i % 6)) + " No. " + (10 + i) + ", Bandung";
                String phone = "08" + (70000000 + i * 137);
                seeds.add(new UserSeed(name, email, addr, phone));
            }

            // Buat/ambil user nasabahnya
            List<User> nasabahList = new ArrayList<>();
            for (UserSeed s : seeds) {
                User u = ensureUser(userRepository, passwordEncoder, s.fullName, s.email, RoleEnum.nasabah, s.address, s.phone);
                nasabahList.add(u);
            }

            // Hitung user baru yang dibuat (kasar, untuk log)
            createdUsers = (int) nasabahList.stream().filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5))).count();

            // ==== 3) Untuk tiap nasabah, buat 1–3 rekening ====
            for (User n : nasabahList) {
                List<Account> existing = accountRepository.findAllByUserId(n);
                if (!existing.isEmpty()) {
                    System.out.println(">>> " + n.getFullName() + " sudah punya " + existing.size() + " rekening, skip buat baru.");
                    continue;
                }

                int accountCount = rndInt(1, 3); // 1..3
                for (int i = 0; i < accountCount; i++) {
                    boolean active = (i == 0) || rndBool(); // minimal 1 aktif
                    Account acc = Account.builder()
                            .userId(n)
                            .isActive(active)
                            .totalDeposit(0L)   // akan diisi setelah generate transaksi
                            .totalWithdraw(0L)  // akan diisi setelah generate transaksi
                            .balance(0L)        // akan diisi setelah generate transaksi
                            .createdAt(randomDateTimeWithinMonths(8, 6)) // rekening dibuat 6–8 bulan lalu
                            .build();

                    acc = accountRepository.saveAndFlush(acc);

                    // Buat transaksi realistis
                    int txCount = active ? rndInt(15, 45) : rndInt(0, 5); // aktif punya banyak histori
                    List<Transaction> generated = generateTransactionsForAccount(acc, txCount);
                    if (!generated.isEmpty()) {
                        transactionRepository.saveAll(generated);
                        transactionRepository.flush();
                    }

                    // Hitung ulang aggregate
                    long totalDep = generated.stream()
                            .filter(t -> t.getType() == TypeTransactionEnum.deposit)
                            .mapToLong(Transaction::getAmount).sum();
                    long totalWdr = generated.stream()
                            .filter(t -> t.getType() == TypeTransactionEnum.withdraw)
                            .mapToLong(Transaction::getAmount).sum();
                    long lastBalance = generated.isEmpty() ? 0L : generated.get(generated.size() - 1).getBalance();

                    acc.setTotalDeposit(totalDep);
                    acc.setTotalWithdraw(totalWdr);
                    acc.setBalance(lastBalance);
                    accountRepository.save(acc);

                    createdAccounts++;
                    createdTxs += generated.size();

                    System.out.printf(">>> Rekening %s (%s) untuk %s: tx=%d, deposit=%,d, withdraw=%,d, saldo=%,d%n",
                            acc.getAccountCode(),
                            active ? "AKTIF" : "NONAKTIF",
                            n.getFullName(),
                            generated.size(),
                            totalDep, totalWdr, lastBalance
                    );
                }
            }

            // Ringkasan
            long totalUsers = userRepository.count();
            long totalAccounts = accountRepository.count();
            long totalTransactions = transactionRepository.count();
            System.out.println("===== DATA LOADER SUMMARY =====");
            System.out.println("Users total       : " + totalUsers + " (baru dibuat ~" + createdUsers + ")");
            System.out.println("Accounts total    : " + totalAccounts + " (baru dibuat +" + createdAccounts + ")");
            System.out.println("Transactions total: " + totalTransactions + " (baru dibuat +" + createdTxs + ")");
            System.out.println("================================");
        };
    }

    // ---------------------- Helpers ----------------------

    private static class UserSeed {
        String fullName;
        String email;
        String address;
        String phone;
        UserSeed(String fullName, String email, String address, String phone) {
            this.fullName = fullName;
            this.email = email;
            this.address = address;
            this.phone = phone;
        }
    }

    private User ensureUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String fullName,
            String email,
            RoleEnum role,
            String address,
            String phone
    ) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = User.builder()
                    .fullName(fullName)
                    .email(email)
                    .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .role(role)
                    .address(address)
                    .phone(phone)
                    .createdAt(LocalDateTime.now())
                    .build();
            User saved = userRepository.save(u);
            System.out.println(">>> Created user " + fullName + " (" + email + ")");
            return saved;
        });
    }

    private List<Transaction> generateTransactionsForAccount(Account account, int txCount) {
        List<Transaction> txs = new ArrayList<>();
        if (txCount <= 0) return txs;

        // Mulai dari 6 bulan lalu sampai hari ini
        LocalDate start = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        LocalDate end = LocalDate.now();

        long balance = 0L;

        // Template event realistis
        String[] depositNotes = {
                "Gaji Bulanan", "Transfer dari Orang Tua", "Bonus Kinerja", "Refund Marketplace",
                "Penjualan Online", "Pengembalian Dana", "Top-up Dompet Digital", "Transfer Rekan"
        };
        String[] withdrawNotes = {
                "Tarik tunai ATM", "Belanja Supermarket", "Bayar Listrik", "Bayar Air",
                "Cicilan", "Beli Pulsa", "Makan di Restoran", "Transportasi Online", "Belanja E-commerce"
        };

        // Sisipkan pola gaji bulanan (tanggal 25–28)
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            LocalDate salaryDate = cursor.withDayOfMonth(Math.min(cursor.lengthOfMonth(), rndInt(25, 28)));
            long salaryAmount = sampleSalary(); // 2.5–8 jt
            balance += salaryAmount;
            txs.add(buildTx(account, TypeTransactionEnum.deposit, salaryAmount, "Gaji Bulanan", randomTime(salaryDate), balance));
            cursor = cursor.plusMonths(1);
        }

        // Tambah transaksi acak lain sampai cap txCount (di luar gaji)
        int remaining = Math.max(0, txCount - txs.size());
        for (int i = 0; i < remaining; i++) {
            boolean isDeposit = rndBoolWeighted(45); // ~45% deposit, 55% withdraw
            if (isDeposit) {
                long amt = sampleDepositAmount();
                balance += amt;
                LocalDateTime when = randomDateTimeBetween(start, end);
                txs.add(buildTx(account, TypeTransactionEnum.deposit, amt, pick(depositNotes), when, balance));
            } else {
                long amt = sampleWithdrawAmount();
                // Pastikan tidak minus (realistis: bank menolak jika tidak cukup)
                if (amt > balance) {
                    // kecilkan menjadi 10–40% dari balance (min 10k), atau skip jika balance 0
                    if (balance <= 10_000) continue;
                    long cap = Math.max(10_000L, (long) (balance * rndDouble(0.1, 0.4)));
                    amt = Math.min(amt, cap);
                }
                balance -= amt;
                LocalDateTime when = randomDateTimeBetween(start, end);
                txs.add(buildTx(account, TypeTransactionEnum.withdraw, amt, pick(withdrawNotes), when, balance));
            }
        }

        // Urutkan by occurredAt (naik) lalu createdAt
        txs.sort(Comparator.comparing(Transaction::getOccurredAt).thenComparing(Transaction::getCreatedAt));

        // Recalculate balance snapshot in case the random ordering changed sequence
        long running = 0L;
        for (Transaction t : txs) {
            if (t.getType() == TypeTransactionEnum.deposit) {
                running += t.getAmount();
            } else {
                running -= t.getAmount();
            }
            t.setBalance(running);
        }

        return txs;
    }

    private Transaction buildTx(Account acc, TypeTransactionEnum type, long amount, String note, LocalDateTime when, long balanceAfter) {
        return Transaction.builder()
                .accountCode(acc)
                .type(type)
                .amount(amount)
                .balance(balanceAfter)
                .note(note)
                .occurredAt(when)
                .createdAt(when.plusMinutes(rndInt(1, 180)))
                .build();
    }

    // ---------------------- Random Utils ----------------------

    private static String rnd4() {
        int n = ThreadLocalRandom.current().nextInt(1000, 9999);
        return String.valueOf(n);
    }

    private static boolean rndBool() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /** return true ~p% */
    private static boolean rndBoolWeighted(int p) {
        return ThreadLocalRandom.current().nextInt(100) < p;
    }

    private static int rndInt(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    private static double rndDouble(double minInclusive, double maxInclusive) {
        return ThreadLocalRandom.current().nextDouble(minInclusive, maxInclusive);
    }

    private static String pick(String[] arr) {
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    private static LocalDateTime randomDateTimeWithinMonths(int maxMonthsBack, int minMonthsBack) {
        int monthsBack = rndInt(minMonthsBack, maxMonthsBack);
        LocalDate base = LocalDate.now().minusMonths(monthsBack);
        int day = rndInt(1, Math.min(28, base.lengthOfMonth()));
        return LocalDateTime.of(base.withDayOfMonth(day), LocalTime.of(rndInt(8, 20), rndInt(0, 59)));
    }

    private static LocalDateTime randomDateTimeBetween(LocalDate start, LocalDate end) {
        long startEpoch = start.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC);
        long endEpoch = end.atTime(23, 59, 59).toEpochSecond(java.time.ZoneOffset.UTC);
        long rand = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        return LocalDateTime.ofEpochSecond(rand, 0, java.time.ZoneOffset.UTC);
    }

    private static LocalDateTime randomTime(LocalDate day) {
        return LocalDateTime.of(day, LocalTime.of(rndInt(8, 18), rndInt(0, 59)));
    }

    private static long sampleSalary() {
        // 2.5–8 jt dengan sedikit variasi ribuan (lebih real)
        long base = rndInt(2_500_000, 8_000_000);
        long jitter = rndInt(0, 90) * 1_000L;
        return base + jitter;
    }

    private static long sampleDepositAmount() {
        // 50k–2jt untuk transfer/topup/bonus
        int[] steps = {50_000, 75_000, 100_000, 150_000, 200_000, 250_000, 300_000, 500_000, 1_000_000, 1_500_000, 2_000_000};
        return steps[rndInt(0, steps.length - 1)];
    }

    private static long sampleWithdrawAmount() {
        // 20k–1.5jt untuk belanja/ATM/bayar tagihan
        int[] steps = {20_000, 50_000, 75_000, 100_000, 150_000, 200_000, 250_000, 300_000, 400_000, 500_000, 750_000, 1_000_000, 1_250_000, 1_500_000};
        return steps[rndInt(0, steps.length - 1)];
    }
}
