package com.github.example.model;

import org.modelmapper.internal.util.Assert;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;

/**
 * The {@code Transaction} class represents money transfer in financial domain.
 * <p>
 * Transaction are immutable in particular implementation.
 * <p>
 * The class {@code Transaction} includes methods for changing status of transaction according to {@link TransactionStatus} values.
 */
public final class Transaction {

    private final UUID id;
    private final String referenceId;
    private final UUID sourceAccountId;
    private final UUID targetAccountId;
    private final BigDecimal amount;
    private final TransactionStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant completedAt;
    private final String reasonCode;

    public Transaction(final String referenceId, final UUID sourceAccountId, final UUID targetAccountId, final BigDecimal amount) {
        Assert.notNull(amount, "Transaction amount");
        Assert.isTrue(!isNullOrEmpty(referenceId), "Reference identifier should be not blank string");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Transaction amount should be positive");
        Assert.isTrue(!sourceAccountId.equals(targetAccountId), "Transactions to the same account is not allowed");

        Instant currentInstant = Instant.now();
        this.id = UUID.randomUUID();
        this.createdAt = currentInstant;
        this.updatedAt = currentInstant;
        this.completedAt = null;
        this.reasonCode = null;
        this.status = TransactionStatus.PENDING;
        this.referenceId = referenceId;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
    }

    private Transaction(final Transaction transaction, final TransactionStatus changedStatus, final String reasonCode) {
        Instant currentInstant = Instant.now();
        this.id = transaction.id;
        this.createdAt = transaction.createdAt;
        this.updatedAt = currentInstant;
        this.completedAt = currentInstant;
        this.status = changedStatus;
        this.reasonCode = reasonCode;
        this.referenceId = transaction.referenceId;
        this.sourceAccountId = transaction.sourceAccountId;
        this.targetAccountId = transaction.targetAccountId;
        this.amount = transaction.amount;
    }

    public UUID getId() {
        return id;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public UUID getTargetAccountId() {
        return targetAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public Transaction executed() {
        return new Transaction(this, TransactionStatus.SUCCESS, this.reasonCode);
    }

    public Transaction failed(final String reasonCode) {
        return new Transaction(this, TransactionStatus.FAILED, reasonCode);
    }

    public enum TransactionStatus {
        PENDING,
        SUCCESS,
        FAILED
    }
}
