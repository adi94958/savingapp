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

import java.text.Normalizer;
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
            ensureUser(
                    userRepository,
                    passwordEncoder,
                    "Adi Saputera",
                    "adi@example.com",
                    RoleEnum.admin,
                    "Jalan Admin No. 1, Majalengka",
                    "08123456789"
            );

            // ==== 2) Seed nasabah dengan data realistis ====
            // Kamu bisa atur jumlahnya di sini
            int targetNasabah = 30;

            // Base nama (first/last) untuk kombinasi
            String[] firstNames = {
                    "Rizky","Muhammad","Nurul","Dewi","Aulia","Rafi","Fajar","Dio","Putri","Salsa",
                    "Laras","Andi","Dimas","Maya","Nadia","Rina","Farhan","Bagas","Yusuf","Ari",
                    "Siti","Anisa","Kenan","Daffa","Adrian","Shandina","Nabila","Vina","Rizka","Hendra",
                    "Arif","Rahma","Lutfi","Fadli","Adit","Bayu","Indah","Nanda","Ilham","Wulan"
            };
            String[] lastNames = {
                    "Ramadhan","Saputra","Pratama","Mahendra","Kusuma","Zahra","Puspita","Nugraha","Wijaya","Putra",
                    "Hidayat","Permata","Siregar","Kurniawan","Santoso","Utami","Prameswari","Gunawan","Pangestu","Cahyadi",
                    "Wibowo","Handayani","Andini","Maulida","Maulana","Fauziah","Lestari","Fadhilah","Pradana","Setiawan"
            };

            // Kota + kecamatan/kelurahan + nama jalan yang umum
            String[] cities = {
                    "Bandung","Cimahi","Majalengka","Jakarta Selatan","Bekasi","Depok","Bogor","Tangerang",
                    "Surabaya","Sleman","Yogyakarta","Semarang","Solo","Malang","Denpasar","Makassar","Medan","Padang"
            };
            String[] districts = {
                    "Kiaracondong","Coblong","Sukajadi","Antapani","Buah Batu","Cimahi Tengah","Parongpong","Lembang",
                    "Kebayoran Baru","Setiabudi","Cipayung","Cinere","Sukmajaya","Ciledug","Bojongsoang","Dayeuhkolot",
                    "Lowokwaru","Tegalsari","Umbulharjo","Jetis"
            };
            String[] streets = {
                    "Jl. Melati","Jl. Mawar","Jl. Dahlia","Jl. Kenanga","Jl. Cempaka","Jl. Teratai","Jl. Flamboyan",
                    "Jl. Anggrek","Jl. Bougenville","Jl. Kamboja","Jl. Asia Afrika","Jl. Dago","Jl. Cikutra","Jl. Kopo",
                    "Jl. Pasteur","Jl. Cibogo","Jl. Antapani","Jl. Sukabumi","Jl. Merdeka","Jl. Dipatiukur"
            };

            // Domain email realistis
            String[] emailDomains = {"gmail.com","yahoo.com","outlook.com","hotmail.com"};

            // Prefix operator Indonesia
            String[] phonePrefixes = {
                    "0811","0812","0813","0814","0815","0816","0817","0818","0819", // Telkomsel
                    "0821","0822","0823",                                           // Telkomsel/AS
                    "0851","0852","0853",                                           // Indosat
                    "0895","0896","0897","0898","0899"                              // Tri/IM3/Axis
            };

            // Set untuk menjamin unik
            Set<String> usedEmails = new HashSet<>();
            Set<String> usedPhones = new HashSet<>();

            List<UserSeed> seeds = new ArrayList<>();

            // Tambahkan beberapa fixed (contoh nyata)
            seeds.add(new UserSeed("Shandina Aulia","shandina@example.com","Jl. Teratai No. 5, Kiaracondong, Bandung","08122000"+rndDigits(4)));
            seeds.add(new UserSeed("Adrian Eka","adrian@example.com","Jl. Melati No. 12, Cimahi Tengah, Cimahi","0898765"+rndDigits(4)));
            seeds.add(new UserSeed("Rizky Ramadhan","rizky.ramadhan@example.com","Jl. Sukabumi No. 21, Coblong, Bandung","0812211"+rndDigits(4)));
            seeds.add(new UserSeed("Putri Maharani","putri.maharani@example.com","Jl. Cibogo No. 7, Sukajadi, Bandung","0821222"+rndDigits(4)));
            seeds.add(new UserSeed("Fajar Nugraha","fajar.nugraha@example.com","Jl. Cikutra No. 10, Antapani, Bandung","0858223"+rndDigits(4)));
            seeds.add(new UserSeed("Laras Kusuma","laras.kusuma@example.com","Jl. Buah Batu No. 88, Buah Batu, Bandung","0813901"+rndDigits(4)));
            seeds.add(new UserSeed("Andi Pratama","andi.pratama@example.com","Jl. Kopo No. 33, Bojongsoang, Bandung","0821990"+rndDigits(4)));
            seeds.add(new UserSeed("Salsabila Zahra","salsa.zahra@example.com","Jl. Asia Afrika No. 1, Sumur Bandung, Bandung","0813777"+rndDigits(4)));
            seeds.add(new UserSeed("Rina Puspita","rina.puspita@example.com","Jl. Dago Atas No. 14, Coblong, Bandung","0812333"+rndDigits(4)));
            seeds.add(new UserSeed("Dio Mahendra","dio.mahendra@example.com","Jl. Antapani No. 4, Antapani, Bandung","0838123"+rndDigits(4)));

            // Generate sisanya secara realistis
            int need = Math.max(0, targetNasabah - seeds.size());
            for (int i = 0; i < need; i++) {
                String fullName = (pick(firstNames) + " " + pick(lastNames)).trim();
                String email = uniqueEmailFromName(fullName, emailDomains, usedEmails);
                String address = randomAddress(streets, districts, cities);
                String phone = uniquePhone(phonePrefixes, usedPhones);
                seeds.add(new UserSeed(fullName, email, address, phone));
            }

            // Buat/ambil user nasabahnya (idempotent by email)
            List<User> nasabahList = new ArrayList<>();
            for (UserSeed s : seeds) {
                // normalize dan pastikan unik untuk fixed seeds juga
                String email = ensureUniqueEmail(s.email, usedEmails);
                String phone = ensureUniquePhone(s.phone, usedPhones);

                User u = ensureUser(userRepository, passwordEncoder, s.fullName, email, RoleEnum.nasabah, s.address, phone);
                nasabahList.add(u);
            }

            createdUsers = (int) nasabahList.stream()
                    .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5)))
                    .count();

            // ==== 3) Untuk tiap nasabah, buat 1–3 rekening + histori realistis ====
            for (User n : nasabahList) {
                List<Account> existing = accountRepository.findAllByUserId(n);
                if (!existing.isEmpty()) {
                    System.out.println(">>> " + n.getFullName() + " sudah punya " + existing.size() + " rekening, skip buat baru.");
                    continue;
                }

                int accountCount = rndInt(1, 3);
                for (int i = 0; i < accountCount; i++) {
                    boolean active = (i == 0) || rndBool();
                    Account acc = Account.builder()
                            .userId(n)
                            .isActive(active)
                            .totalDeposit(0L)
                            .totalWithdraw(0L)
                            .balance(0L)
                            .createdAt(randomDateTimeWithinMonths(8, 6))
                            .build();

                    acc = accountRepository.saveAndFlush(acc);

                    int txCount = active ? rndInt(15, 45) : rndInt(0, 5);
                    List<Transaction> generated = generateTransactionsForAccount(acc, txCount);
                    if (!generated.isEmpty()) {
                        transactionRepository.saveAll(generated);
                        transactionRepository.flush();
                    }

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

    // ====== Realistic generator ======

    private static String uniqueEmailFromName(String fullName, String[] domains, Set<String> used) {
        String base = slugify(fullName).replace("-", ".");
        String domain = pick(domains);
        String candidate = base + "@" + domain;
        int attempt = 1;
        while (used.contains(candidate)) {
            candidate = base + (attempt++) + "@" + domain;
        }
        used.add(candidate);
        return candidate;
    }

    private static String ensureUniqueEmail(String email, Set<String> used) {
        if (!used.contains(email)) {
            used.add(email);
            return email;
        }
        String local = email.substring(0, email.indexOf('@'));
        String domain = email.substring(email.indexOf('@'));
        int attempt = 1;
        String candidate = local + attempt + domain;
        while (used.contains(candidate)) {
            attempt++;
            candidate = local + attempt + domain;
        }
        used.add(candidate);
        return candidate;
    }

    private static String uniquePhone(String[] prefixes, Set<String> used) {
        String candidate;
        do {
            String prefix = pick(prefixes);
            // total digit 11–13 wajar di ID
            int totalLen = rndInt(11, 13);
            int need = totalLen - prefix.length();
            candidate = prefix + rndDigits(need);
        } while (used.contains(candidate));
        used.add(candidate);
        return candidate;
    }

    private static String ensureUniquePhone(String phone, Set<String> used) {
        if (!used.contains(phone)) {
            used.add(phone);
            return phone;
        }
        String candidate;
        do {
            candidate = phone + rndDigits(1);
        } while (used.contains(candidate));
        used.add(candidate);
        return candidate;
    }

    private static String randomAddress(String[] streets, String[] districts, String[] cities) {
        String street = pick(streets);
        int no = rndInt(1, 199);
        String dist = pick(districts);
        String city = pick(cities);
        int rt = rndInt(1, 12);
        int rw = rndInt(1, 12);
        return String.format("%s No. %d, RT %02d/RW %02d, %s, %s", street, no, rt, rw, dist, city);
    }

    private static String slugify(String input) {
        // hilangkan aksen & karakter non-alfanumerik → huruf kecil + dash
        String now = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        now = now.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-");
        return now.replaceAll("(^-|-$)", "");
    }

    // ====== Transaksi realistis (punyamu dipertahankan) ======

    private List<Transaction> generateTransactionsForAccount(Account account, int txCount) {
        List<Transaction> txs = new ArrayList<>();
        if (txCount <= 0) return txs;

        LocalDate start = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        LocalDate end = LocalDate.now();

        long balance = 0L;

        String[] depositNotes = {
                "Gaji Bulanan", "Transfer dari Orang Tua", "Bonus Kinerja", "Refund Marketplace",
                "Penjualan Online", "Pengembalian Dana", "Top-up Dompet Digital", "Transfer Rekan"
        };
        String[] withdrawNotes = {
                "Tarik tunai ATM", "Belanja Supermarket", "Bayar Listrik", "Bayar Air",
                "Cicilan", "Beli Pulsa", "Makan di Restoran", "Transportasi Online", "Belanja E-commerce"
        };

        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            LocalDate salaryDate = cursor.withDayOfMonth(Math.min(cursor.lengthOfMonth(), rndInt(25, 28)));
            long salaryAmount = sampleSalary();
            balance += salaryAmount;
            txs.add(buildTx(account, TypeTransactionEnum.deposit, salaryAmount, "Gaji Bulanan", randomTime(salaryDate), balance));
            cursor = cursor.plusMonths(1);
        }

        int remaining = Math.max(0, txCount - txs.size());
        for (int i = 0; i < remaining; i++) {
            boolean isDeposit = rndBoolWeighted(45);
            if (isDeposit) {
                long amt = sampleDepositAmount();
                balance += amt;
                LocalDateTime when = randomDateTimeBetween(start, end);
                txs.add(buildTx(account, TypeTransactionEnum.deposit, amt, pick(depositNotes), when, balance));
            } else {
                long amt = sampleWithdrawAmount();
                if (amt > balance) {
                    if (balance <= 10_000) continue;
                    long cap = Math.max(10_000L, (long) (balance * rndDouble(0.1, 0.4)));
                    amt = Math.min(amt, cap);
                }
                balance -= amt;
                LocalDateTime when = randomDateTimeBetween(start, end);
                txs.add(buildTx(account, TypeTransactionEnum.withdraw, amt, pick(withdrawNotes), when, balance));
            }
        }

        txs.sort(Comparator.comparing(Transaction::getOccurredAt).thenComparing(Transaction::getCreatedAt));

        long running = 0L;
        for (Transaction t : txs) {
            if (t.getType() == TypeTransactionEnum.deposit) running += t.getAmount();
            else running -= t.getAmount();
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

    private static String rndDigits(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(ThreadLocalRandom.current().nextInt(0, 10));
        return sb.toString();
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

    private static <T> T pick(T[] arr) {
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
        long base = rndInt(2_500_000, 8_000_000);
        long jitter = rndInt(0, 90) * 1_000L;
        return base + jitter;
    }

    private static long sampleDepositAmount() {
        int[] steps = {50_000, 75_000, 100_000, 150_000, 200_000, 250_000, 300_000, 500_000, 1_000_000, 1_500_000, 2_000_000};
        return steps[rndInt(0, steps.length - 1)];
    }

    private static long sampleWithdrawAmount() {
        int[] steps = {20_000, 50_000, 75_000, 100_000, 150_000, 200_000, 250_000, 300_000, 400_000, 500_000, 750_000, 1_000_000, 1_250_000, 1_500_000};
        return steps[rndInt(0, steps.length - 1)];
    }
}
    