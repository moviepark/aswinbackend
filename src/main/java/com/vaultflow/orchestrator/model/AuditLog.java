package com.vaultflow.orchestrator.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "tbl_audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    private String performedBy;

    public AuditLog() {
    }

    public AuditLog(String action, String performedBy) {
        this.action = action;
        this.performedBy = performedBy;
    }

    @PrePersist
    public void onCreate() {
        this.timestamp = new Date();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
}
