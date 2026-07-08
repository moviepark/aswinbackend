package com.vaultflow.orchestrator.controller;

import com.vaultflow.orchestrator.dto.LedgerDTO;
import com.vaultflow.orchestrator.service.LedgerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ledgers")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN','TREASURER')")
    public ResponseEntity<Page<LedgerDTO>> getLedgers(Pageable pageable) {
        return ResponseEntity.ok(ledgerService.getAllLedgers(pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TREASURER')")
    public ResponseEntity<LedgerDTO> createLedger(@RequestBody LedgerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ledgerService.createLedger(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TREASURER')")
    public ResponseEntity<LedgerDTO> updateLedger(@PathVariable Long id, @RequestBody LedgerDTO dto) {
        return ResponseEntity.ok(ledgerService.updateLedger(id, dto));
    }
}
