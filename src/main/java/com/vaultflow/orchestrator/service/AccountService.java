package com.vaultflow.orchestrator.service;

import com.vaultflow.orchestrator.dto.AccountDTO;
import com.vaultflow.orchestrator.model.Account;
import com.vaultflow.orchestrator.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuditService auditService;

    public AccountService(AccountRepository accountRepository, AuditService auditService) {
        this.accountRepository = accountRepository;
        this.auditService = auditService;
    }

    public List<AccountDTO> getAllAccounts() {
        return accountRepository.findAll().stream().map(this::toDTO).toList();
    }

    public AccountDTO getAccount(Long id) {
        return accountRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
    }

    @Transactional
    public AccountDTO createAccount(AccountDTO dto) {
        validateAccount(dto);

        if (dto.getAccountReference() != null && !dto.getAccountReference().isBlank()
                && accountRepository.existsByAccountReference(dto.getAccountReference())) {
            throw new IllegalArgumentException("Account reference already exists");
        }

        Account account = new Account();
        account.setAccountReference(dto.getAccountReference());
        account.setAccountName(dto.getAccountName());
        account.setAccountType(dto.getAccountType());
        account.setBalance(dto.getBalance() == null ? 0.0 : dto.getBalance());
        account.setInterestRate(dto.getInterestRate());

        Account saved = accountRepository.save(account);
        auditService.logAction("ACCOUNT_CREATED:" + saved.getAccountReference());
        return toDTO(saved);
    }

    @Transactional
    public AccountDTO updateAccount(Long id, AccountDTO dto) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        if (dto.getAccountName() != null && !dto.getAccountName().isBlank()) {
            account.setAccountName(dto.getAccountName());
        }
        if (dto.getAccountType() != null && !dto.getAccountType().isBlank()) {
            account.setAccountType(dto.getAccountType());
        }
        if (dto.getBalance() != null) {
            if (dto.getBalance() < 0) {
                throw new IllegalArgumentException("Balance cannot be negative");
            }
            account.setBalance(dto.getBalance());
        }
        if (dto.getInterestRate() != null) {
            account.setInterestRate(dto.getInterestRate());
        }
        if (dto.getAccountReference() != null && !dto.getAccountReference().isBlank()) {
            account.setAccountReference(dto.getAccountReference());
        }

        Account saved = accountRepository.save(account);
        auditService.logAction("ACCOUNT_UPDATED:" + saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        accountRepository.delete(account);
        auditService.logAction("ACCOUNT_DELETED:" + id);
    }

    private void validateAccount(AccountDTO dto) {
        if (dto.getAccountName() == null || dto.getAccountName().isBlank()) {
            throw new IllegalArgumentException("Account name is required");
        }
        if (dto.getAccountType() == null || dto.getAccountType().isBlank()) {
            throw new IllegalArgumentException("Account type is required");
        }
        if (dto.getBalance() != null && dto.getBalance() < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
    }

    private AccountDTO toDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountReference(account.getAccountReference());
        dto.setAccountName(account.getAccountName());
        dto.setAccountType(account.getAccountType());
        dto.setBalance(account.getBalance());
        dto.setInterestRate(account.getInterestRate());
        return dto;
    }
}
