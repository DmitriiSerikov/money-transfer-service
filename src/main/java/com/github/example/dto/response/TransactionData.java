package com.github.example.dto.response;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class TransactionData implements Serializable {
    private static final long serialVersionUID = -1598024062752939479L;

    private UUID id;
    private String referenceId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    private String reasonCode;
    private Set<TransactionEntryData> entries;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public Set<TransactionEntryData> getEntries() {
        return entries;
    }

    public void setEntries(Set<TransactionEntryData> entries) {
        this.entries = entries;
    }
}
