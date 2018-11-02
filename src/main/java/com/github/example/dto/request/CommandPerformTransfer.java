package com.github.example.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Initial data for creation of the transfer transaction")
public class CommandPerformTransfer implements Serializable {
    private static final long serialVersionUID = 7226383963100484243L;

    private String referenceId;
    private UUID sourceAccountId;
    private UUID targetAccountId;
    private BigDecimal amount;

    @Schema(type = "string", minLength = 1, maxLength = 40, required = true,
            description = "Unique value used to handle submitted duplicates")
    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    @Schema(type = "string", format = "uuid", required = true,
            description = "Unique identifier of a source account")
    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(UUID sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    @Schema(type = "string", format = "uuid", required = true,
            description = "Unique identifier of a target account")
    public UUID getTargetAccountId() {
        return targetAccountId;
    }

    public void setTargetAccountId(UUID targetAccountId) {
        this.targetAccountId = targetAccountId;
    }

    @Schema(type = "number", minimum = "0", required = true,
            description = "Transaction amount")
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
