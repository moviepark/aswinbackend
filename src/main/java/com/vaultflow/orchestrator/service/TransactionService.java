package com.vaultflow.orchestrator.service;

import com.vaultflow.orchestrator.dto.TransactionDTO;
import com.vaultflow.orchestrator.exception.InsufficientFundsException;
import com.vaultflow.orchestrator.model.Account;
import com.vaultflow.orchestrator.model.Ledger;
import com.vaultflow.orchestrator.model.Transaction;
import com.vaultflow.orchestrator.repository.AccountRepository;
import com.vaultflow.orchestrator.repository.LedgerRepository;
import com.vaultflow.orchestrator.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;
    private final AuditService auditService;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              LedgerRepository ledgerRepository,
                              AuditService auditService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.ledgerRepository = ledgerRepository;
        this.auditService = auditService;
    }

    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream().map(this::toDTO).toList();
    }

    public List<TransactionDTO> getByLedger(String param) {
        if (param == null || param.isBlank()) {
            return getAllTransactions();
        }
        try {
            Long ledgerId = Long.parseLong(param);
            return transactionRepository.findByLedgerId(ledgerId).stream().map(this::toDTO).toList();
        } catch (NumberFormatException ignored) {
            return transactionRepository.findByLedgerDescriptionContainingIgnoreCase(param).stream().map(this::toDTO).toList();
        }
    }

    public TransactionDTO getTransaction(Long id) {
        return transactionRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
    }

    @Transactional
    public TransactionDTO createTransaction(TransactionDTO dto) {
        validateTransaction(dto);

        Account source = resolveAccount(dto.getSourceAccountId(), dto.getSource(), "Source account not found");
        Account target = resolveAccount(dto.getTargetAccountId(), dto.getTarget(), "Target account not found");

        if (source.getId().equals(target.getId())) {
            throw new IllegalArgumentException("Source and target accounts must be different");
        }
        if (source.getBalance() == null || source.getBalance() < dto.getAmount()) {
            throw new InsufficientFundsException("Insufficient funds for transaction");
        }

        source.setBalance(source.getBalance() - dto.getAmount());
        target.setBalance((target.getBalance() == null ? 0.0 : target.getBalance()) + dto.getAmount());
        accountRepository.save(source);
        accountRepository.save(target);

        Transaction transaction = new Transaction();
        transaction.setAmount(dto.getAmount());
        transaction.setReferenceId(dto.getReferenceId());
        transaction.setSourceAccount(source);
        transaction.setTargetAccount(target);
        transaction.setStatus(dto.getStatus() == null || dto.getStatus().isBlank() ? "PENDING" : dto.getStatus().toUpperCase());
        transaction.setLedger(resolveLedger(dto.getLedgerId()));

        Transaction saved = transactionRepository.save(transaction);
        auditService.logAction("TRANSACTION_CREATED:" + saved.getReferenceId());
        return toDTO(saved);
    }

    @Transactional
    public TransactionDTO updateTransaction(Long id, TransactionDTO dto) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            transaction.setStatus(dto.getStatus().toUpperCase());
        }
        if (dto.getReferenceId() != null && !dto.getReferenceId().isBlank()) {
            transaction.setReferenceId(dto.getReferenceId());
        }

        Transaction saved = transactionRepository.save(transaction);
        auditService.logAction("TRANSACTION_UPDATED:" + saved.getReferenceId() + ":" + saved.getStatus());
        return toDTO(saved);
    }

    @Transactional
    public TransactionDTO reverseTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        if ("REVERSED".equalsIgnoreCase(transaction.getStatus())) {
            throw new IllegalArgumentException("Transaction already reversed");
        }

        Account source = transaction.getSourceAccount();
        Account target = transaction.getTargetAccount();
        Double amount = transaction.getAmount();

        if (target.getBalance() == null || target.getBalance() < amount) {
            throw new InsufficientFundsException("Insufficient funds to reverse transaction");
        }

        target.setBalance(target.getBalance() - amount);
        source.setBalance((source.getBalance() == null ? 0.0 : source.getBalance()) + amount);
        accountRepository.save(source);
        accountRepository.save(target);

        transaction.setStatus("REVERSED");
        transaction.setReversedAt(new Date());
        Transaction saved = transactionRepository.save(transaction);
        auditService.logAction("TRANSACTION_REVERSED:" + saved.getReferenceId());
        return toDTO(saved);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        String referenceId = transaction.getReferenceId();
        transactionRepository.delete(transaction);
        auditService.logAction("TRANSACTION_DELETED:" + referenceId);
    }

    private void validateTransaction(TransactionDTO dto) {
        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }
        if (dto.getReferenceId() == null || dto.getReferenceId().isBlank()) {
            throw new IllegalArgumentException("Reference ID is required");
        }
    }

    private Account resolveAccount(Long id, String reference, String notFoundMessage) {
        if (id != null) {
            return accountRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(notFoundMessage));
        }
        if (reference != null && !reference.isBlank()) {
            try {
                Long parsedId = Long.parseLong(reference);
                return accountRepository.findById(parsedId).orElseThrow(() -> new EntityNotFoundException(notFoundMessage));
            } catch (NumberFormatException ignored) {
                return accountRepository.findByAccountReference(reference)
                        .orElseThrow(() -> new EntityNotFoundException(notFoundMessage + ": " + reference));
            }
        }
        throw new EntityNotFoundException(notFoundMessage);
    }

    private Ledger resolveLedger(Long ledgerId) {
        if (ledgerId != null) {
            return ledgerRepository.findById(ledgerId).orElseThrow(() -> new EntityNotFoundException("Ledger not found"));
        }
        List<Ledger> ledgers = ledgerRepository.findAll();
        if (!ledgers.isEmpty()) {
            return ledgers.get(0);
        }
        return ledgerRepository.save(new Ledger("Default Transaction Ledger"));
    }

    public TransactionDTO toDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setReferenceId(transaction.getReferenceId());
        dto.setStatus(transaction.getStatus());
        if (transaction.getSourceAccount() != null) {
            dto.setSourceAccountId(transaction.getSourceAccount().getId());
            dto.setSource(transaction.getSourceAccount().getAccountReference());
            dto.setSourceAccountName(transaction.getSourceAccount().getAccountName());
        }
        if (transaction.getTargetAccount() != null) {
            dto.setTargetAccountId(transaction.getTargetAccount().getId());
            dto.setTarget(transaction.getTargetAccount().getAccountReference());
            dto.setTargetAccountName(transaction.getTargetAccount().getAccountName());
        }
        if (transaction.getLedger() != null) {
            dto.setLedgerId(transaction.getLedger().getId());
        }
        return dto;
    }
}
