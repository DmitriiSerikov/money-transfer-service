package com.github.example.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "Initial data for creation of the bank account")
public class CommandCreateAccount implements Serializable {
    private static final long serialVersionUID = -4842260737441570738L;

    private BigDecimal initialBalance;

    @Schema(type = "number", minimum = "0", required = true,
            description = "Initial balance of the bank account")
    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}
