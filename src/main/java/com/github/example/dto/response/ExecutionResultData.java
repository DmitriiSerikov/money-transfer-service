package com.github.example.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Basic information about transaction processing")
public class ExecutionResultData implements Serializable {
    private static final long serialVersionUID = 329809307794998363L;

    private UUID id;
    private String status;
    private Instant createdAt;
    private Instant completedAt;

    @Schema(type = "string", format = "uuid", description = "Unique identifier of the created transaction")
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Schema(type = "string", allowableValues = "PENDING,SUCCESS,FAILED", description = "Transaction processing status")
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

    @Schema(type = "string", format = "date-time", description = "Instant when the transaction was completed")
    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
