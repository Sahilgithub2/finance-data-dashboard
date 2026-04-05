package com.finance.config;

import com.finance.enums.Role;
import com.finance.enums.TransactionType;
import com.finance.model.Transaction;
import com.finance.model.User;
import com.finance.repository.TransactionRepository;
import com.finance.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedUsers(
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }
            log.info("Seeding demo users and transactions (empty database)");
            User admin = new User();
            admin.setEmail("admin@finance.local");
            admin.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);

            User analyst = new User();
            analyst.setEmail("analyst@finance.local");
            analyst.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
            analyst.setRole(Role.ANALYST);
            analyst.setActive(true);
            userRepository.save(analyst);

            User viewer = new User();
            viewer.setEmail("viewer@finance.local");
            viewer.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
            viewer.setRole(Role.VIEWER);
            viewer.setActive(true);
            userRepository.save(viewer);

            seedTx(transactionRepository, viewer, "Salary", TransactionType.INCOME, "5000.00", LocalDate.now().minusDays(5));
            seedTx(transactionRepository, viewer, "Groceries", TransactionType.EXPENSE, "120.50", LocalDate.now().minusDays(3));
            seedTx(transactionRepository, viewer, "Freelance", TransactionType.INCOME, "800.00", LocalDate.now().minusMonths(1));
            seedTx(transactionRepository, analyst, "Bonus", TransactionType.INCOME, "2000.00", LocalDate.now().minusDays(10));
            seedTx(transactionRepository, analyst, "Travel", TransactionType.EXPENSE, "450.00", LocalDate.now().minusDays(2));
        };
    }

    private static void seedTx(
            TransactionRepository repo,
            User user,
            String category,
            TransactionType type,
            String amount,
            LocalDate date) {
        Transaction t = new Transaction();
        t.setUser(user);
        t.setCategory(category);
        t.setType(type);
        t.setAmount(new BigDecimal(amount));
        t.setDate(date);
        t.setDeleted(false);
        repo.save(t);
    }
}
