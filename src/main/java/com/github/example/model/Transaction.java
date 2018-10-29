package com.github.example.model;

import org.modelmapper.internal.util.Assert;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static io.micronaut.core.util.CollectionUtils.isNotEmpty;
import static io.micronaut.core.util.StringUtils.hasText;
import static java.util.Collections.unmodifiableSet;

public final class Transaction {

    private final UUID id;
    private final String referenceId;
    private final TransactionStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant completedAt;
    private final String reasonCode;
    private final Set<TransactionEntry> entries;

    public Transaction(final String referenceId, final Set<TransactionEntry> entries) {
        Assert.isTrue(hasText(referenceId), "Reference identifier should be not blank string");
        Assert.isTrue(isNotEmpty(entries), "Transactions entries should not be empty");
        Assert.isTrue(entries.size() > 1, "Transactions should have at least two entries");

        Instant currentInstant = Instant.now();
        this.id = UUID.randomUUID();
        this.createdAt = currentInstant;
        this.updatedAt = currentInstant;
        this.completedAt = null;
        this.reasonCode = null;
        this.status = TransactionStatus.PENDING;
        this.referenceId = referenceId;
        this.entries = unmodifiableSet(entries);
    }

    private Transaction(final Transaction transaction, final TransactionStatus changedStatus) {
        this(transaction, changedStatus, transaction.reasonCode);
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
        this.entries = transaction.entries;
    }

    public UUID getId() {
        return id;
    }

    public String getReferenceId() {
        return referenceId;
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

    public Set<TransactionEntry> getEntries() {
        return entries;
    }

    public Transaction executed() {
        return new Transaction(this, TransactionStatus.SUCCESS);
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
