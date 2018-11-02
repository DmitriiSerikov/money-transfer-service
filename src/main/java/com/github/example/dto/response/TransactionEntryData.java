package com.github.example.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Detailed information about the transaction entry")
public class TransactionEntryData implements Serializable {
    private static final long serialVersionUID = -9078811038269355748L;

    private UUID id;
    private UUID accountId;
    private BigDecimal amount;

    @Schema(type = "string", format = "uuid", description = "Unique identifier of the transaction entry")
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Schema(type = "string", format = "uuid", description = "Unique identifier of the account the transaction is associated with")
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    @Schema(type = "number", description = "Transaction amount")
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
