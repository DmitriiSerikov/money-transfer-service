package com.github.example.model;

import org.modelmapper.internal.util.Assert;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The {@code Transaction} class represents money transfer in financial domain.
 * <p>
 * Transaction are immutable in particular implementation.
 * <p>
 * The class {@code Transaction} includes methods for changing status of transaction according to {@link TransactionStatus} values.
 */
public final class Transaction {
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    private final long id;
    private final Instant creationTime;
    private final long sourceAccountId;
    private final long targetAccountId;
    private final BigDecimal amount;
    private final TransactionStatus status;

    public Transaction(final long sourceAccountId, final long targetAccountId, final BigDecimal amount) {
        Assert.notNull(amount, "Transaction amount");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Transaction amount should be positive");
        Assert.isTrue(sourceAccountId > 0, "Source account id for transaction should be positive");
        Assert.isTrue(targetAccountId > 0, "Target account id for transaction should be positive");
        Assert.isTrue(sourceAccountId != targetAccountId, "Transactions not allowed between same account id's");

        this.id = SEQUENCE.incrementAndGet();
        this.creationTime = Instant.now();
        this.status = TransactionStatus.PENDING;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
    }

    private Transaction(final Transaction transaction, final TransactionStatus changedStatus) {
        this.id = transaction.id;
        this.creationTime = transaction.creationTime;
        this.status = changedStatus;
        this.sourceAccountId = transaction.sourceAccountId;
        this.targetAccountId = transaction.targetAccountId;
        this.amount = transaction.amount;
    }

    public long getId() {
        return id;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public long getSourceAccountId() {
        return sourceAccountId;
    }

    public long getTargetAccountId() {
        return targetAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Transaction executed() {
        return new Transaction(this, TransactionStatus.SUCCESS);
    }

    public Transaction failed() {
        return new Transaction(this, TransactionStatus.FAILED);
    }

    public enum TransactionStatus {
        PENDING,
        SUCCESS,
        FAILED
    }
}
