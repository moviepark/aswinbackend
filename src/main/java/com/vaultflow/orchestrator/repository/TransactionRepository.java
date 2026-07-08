package com.vaultflow.orchestrator.repository;

import com.vaultflow.orchestrator.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("select t from Transaction t where t.ledger.id = :ledgerId")
    List<Transaction> findByLedgerId(@Param("ledgerId") Long ledgerId);

    @Query("select t from Transaction t where lower(t.ledger.description) like lower(concat('%', :description, '%'))")
    List<Transaction> findByLedgerDescriptionContainingIgnoreCase(@Param("description") String description);
}
