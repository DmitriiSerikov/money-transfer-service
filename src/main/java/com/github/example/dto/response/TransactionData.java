package com.github.example.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Detailed information about the transaction")
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

    @Schema(type = "string", format = "uuid", description = "Unique identifier of the transaction")
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Schema(type = "string", description = "Unique value used to handle submitted duplicates")
    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    @Schema(type = "string", description = "Transaction processing status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Schema(type = "string", format = "date-time", description = "Instant when the transaction was created")
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Schema(type = "string", format = "date-time", description = "Instant when the transaction was last updated")
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Schema(type = "string", format = "date-time", description = "Instant when the transaction was completed")
    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    @Schema(type = "string", description = "Reason code for failed transaction status")
    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    @ArraySchema(uniqueItems = true, schema = @Schema(implementation = TransactionEntryData.class))
    public Set<TransactionEntryData> getEntries() {
        return entries;
    }

    public void setEntries(Set<TransactionEntryData> entries) {
        this.entries = entries;
    }
}
