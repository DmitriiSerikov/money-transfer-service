package com.github.example.model;

import org.modelmapper.internal.util.Assert;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public final class TransactionEntry {

    private final UUID id;
    private final UUID accountId;
    private final BigDecimal amount;

    public TransactionEntry(final UUID accountId, final BigDecimal amount) {
        Assert.notNull(accountId, "Entry account");
        Assert.notNull(amount, "Entry amount");
        Assert.isTrue(amount.signum() != 0, "Entry amount should not be zero");

        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.amount = amount;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TransactionEntry)) return false;
        TransactionEntry that = (TransactionEntry) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
