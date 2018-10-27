package com.github.example.dto.request;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;


public class CommandPerformTransfer implements Serializable {
    private static final long serialVersionUID = 7226383963100484243L;

    private String referenceId;
    private UUID sourceAccountId;
    private UUID targetAccountId;
    private BigDecimal amount;

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(UUID sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public UUID getTargetAccountId() {
        return targetAccountId;
    }

    public void setTargetAccountId(UUID targetAccountId) {
        this.targetAccountId = targetAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
