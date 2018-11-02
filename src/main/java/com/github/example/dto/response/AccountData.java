package com.github.example.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Basic information about the bank account")
public class AccountData implements Serializable {
    private static final long serialVersionUID = -4311698332553337798L;

    private UUID id;
    private BigDecimal balance;
    private Instant createdAt;
    private Instant updatedAt;

    @Schema(type = "string", format = "uuid", description = "Unique identifier of the bank account")
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Schema(type = "number", description = "Available balance of the bank account")
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Schema(type = "string", format = "date-time", description = "Instant when the account was created")
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Schema(type = "string", format = "date-time", description = "Instant when the account was last updated")
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
