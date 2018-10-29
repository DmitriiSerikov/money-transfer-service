package com.github.example.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class TransactionEntryData implements Serializable {
    private static final long serialVersionUID = -9078811038269355748L;

    private UUID id;
    private UUID accountId;
    private BigDecimal amount;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
