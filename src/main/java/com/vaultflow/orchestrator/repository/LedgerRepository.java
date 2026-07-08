package com.vaultflow.orchestrator.repository;

import com.vaultflow.orchestrator.model.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {
}
