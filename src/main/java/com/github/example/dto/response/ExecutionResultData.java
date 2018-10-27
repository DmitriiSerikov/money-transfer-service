package com.github.example.dto.response;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class ExecutionResultData implements Serializable {
    private static final long serialVersionUID = 329809307794998363L;

    private UUID id;
    private String status;
    private Instant createdAt;
    private Instant completedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
