package com.github.example.dto.request;

import java.io.Serializable;
import java.math.BigDecimal;


public class CommandCreateTransaction implements Serializable {
    private static final long serialVersionUID = 7226383963100484243L;

    private long sourceAccountId;
    private long targetAccountId;
    private BigDecimal amount;

    public long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public long getTargetAccountId() {
        return targetAccountId;
    }

    public void setTargetAccountId(long targetAccountId) {
        this.targetAccountId = targetAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
