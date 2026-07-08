package com.vaultflow.orchestrator.service;

import com.vaultflow.orchestrator.dto.LedgerDTO;
import com.vaultflow.orchestrator.model.Ledger;
import com.vaultflow.orchestrator.repository.LedgerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final AuditService auditService;

    public LedgerService(LedgerRepository ledgerRepository, AuditService auditService) {
        this.ledgerRepository = ledgerRepository;
        this.auditService = auditService;
    }

    public Page<LedgerDTO> getAllLedgers(Pageable pageable) {
        return ledgerRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional
    public LedgerDTO createLedger(LedgerDTO dto) {
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Ledger description is required");
        }
        Ledger ledger = new Ledger(dto.getDescription());
        Ledger saved = ledgerRepository.save(ledger);
        auditService.logAction("LEDGER_CREATED:" + saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public LedgerDTO updateLedger(Long id, LedgerDTO dto) {
        Ledger ledger = ledgerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ledger not found"));
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Ledger description is required");
        }
        ledger.setDescription(dto.getDescription());
        Ledger saved = ledgerRepository.save(ledger);
        auditService.logAction("LEDGER_UPDATED:" + saved.getId());
        return toDTO(saved);
    }

    private LedgerDTO toDTO(Ledger ledger) {
        LedgerDTO dto = new LedgerDTO();
        dto.setId(ledger.getId());
        dto.setDescription(ledger.getDescription());
        dto.setCreatedAt(ledger.getCreatedAt());
        return dto;
    }
}
