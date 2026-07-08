package com.vaultflow.orchestrator.config;

import com.vaultflow.orchestrator.model.Account;
import com.vaultflow.orchestrator.model.Ledger;
import com.vaultflow.orchestrator.model.User;
import com.vaultflow.orchestrator.repository.AccountRepository;
import com.vaultflow.orchestrator.repository.LedgerRepository;
import com.vaultflow.orchestrator.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               AccountRepository accountRepository,
                               LedgerRepository ledgerRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfMissing(userRepository, passwordEncoder, "Vault Admin", "admin@vaultflow.io", "ADMIN", "USR-FIN-001");
            createUserIfMissing(userRepository, passwordEncoder, "Vault Treasurer", "treasurer@vaultflow.io", "TREASURER", "USR-FIN-002");
            createUserIfMissing(userRepository, passwordEncoder, "Vault Auditor", "auditor@vaultflow.io", "AUDITOR", "USR-FIN-003");
            createUserIfMissing(userRepository, passwordEncoder, "Vault User", "user@vaultflow.io", "USER", "USR-FIN-004");

            if (ledgerRepository.count() == 0) {
                ledgerRepository.save(new Ledger("Default Transaction Ledger"));
                ledgerRepository.save(new Ledger("Corporate Operations Ledger"));
            }

            if (accountRepository.count() == 0) {
                accountRepository.save(new Account("ACC-VAULT-001", "Primary Savings", "SAVINGS", 50000.0, 4.5));
                accountRepository.save(new Account("ACC-VAULT-002", "Operations Checking", "CHECKING", 10000.0, 0.0));
                accountRepository.save(new Account("ACC-VAULT-003", "Investment Reserve", "INVESTMENT", 25000.0, 7.0));
            }
        };
    }

    private void createUserIfMissing(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     String name,
                                     String email,
                                     String role,
                                     String userId) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(role);
            user.setUserId(userId);
            user.setOrganizationId("CORP-VAULT-01");
            userRepository.save(user);
        }
    }
}
