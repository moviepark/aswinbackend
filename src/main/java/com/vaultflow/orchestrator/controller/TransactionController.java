package com.vaultflow.orchestrator.controller;

import com.vaultflow.orchestrator.dto.TransactionDTO;
import com.vaultflow.orchestrator.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TREASURER','ADMIN')")
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(dto));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database unreachable");
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','TREASURER','ADMIN','AUDITOR')")
    public ResponseEntity<List<TransactionDTO>> getTransactions(@RequestParam(required = false) String ledgerId) {
        if (ledgerId == null || ledgerId.isBlank()) {
            return ResponseEntity.ok(transactionService.getAllTransactions());
        }
        return ResponseEntity.ok(transactionService.getByLedger(ledgerId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','TREASURER','ADMIN','AUDITOR')")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TREASURER','ADMIN')")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable Long id, @RequestBody TransactionDTO dto) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','TREASURER','ADMIN')")
    public ResponseEntity<String> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok("Transaction deleted successfully.");
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAnyRole('TREASURER','ADMIN')")
    public ResponseEntity<TransactionDTO> reverseTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.reverseTransaction(id));
    }
}
