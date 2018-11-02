package com.github.example.model;


import org.modelmapper.internal.util.Assert;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class Account {

    private final UUID id;
    private final BigDecimal balance;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Account(final BigDecimal initialBalance) {
        checkBalanceIsPositiveOrZero(initialBalance);

        Instant currentInstant = Instant.now();
        this.id = UUID.randomUUID();
        this.createdAt = currentInstant;
        this.updatedAt = currentInstant;
        this.balance = initialBalance;
    }

    private Account(Account account, final BigDecimal changedBalance) {
        checkBalanceIsPositiveOrZero(changedBalance);

        this.id = account.id;
        this.createdAt = account.createdAt;
        this.updatedAt = Instant.now();
        this.balance = changedBalance;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Account addBy(final TransactionEntry entry) {
        Assert.notNull(entry, "Transaction entry");

        return new Account(this, balance.add(entry.getAmount()));
    }

    private static void checkBalanceIsPositiveOrZero(final BigDecimal balance) {
        Assert.notNull(balance, "Account balance");
        Assert.isTrue(balance.signum() >= 0, "Account balance should be positive or zero");
    }
}
