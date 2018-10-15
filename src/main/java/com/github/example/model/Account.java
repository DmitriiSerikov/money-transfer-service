package com.github.example.model;


import org.modelmapper.internal.util.Assert;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The {@code Account} class represents bank account in financial domain.
 * <p>
 * Accounts are immutable in particular implementation
 * <p>
 * The class {@code Account} includes methods for deposit and withdraw money from account balance.
 */
public final class Account {
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    private final long id;
    private final Instant creationTime;
    private final BigDecimal balance;

    public Account(final BigDecimal initialBalance) {
        Assert.notNull(initialBalance, "Initial balance");
        Assert.isTrue(initialBalance.compareTo(BigDecimal.ZERO) >= 0, "Account balance should be positive or zero");

        this.id = SEQUENCE.incrementAndGet();
        this.creationTime = Instant.now();
        this.balance = initialBalance;
    }

    private Account(Account account, final BigDecimal changedBalance) {
        Assert.isTrue(changedBalance.compareTo(BigDecimal.ZERO) >= 0, "Account balance should be positive or zero");

        this.id = account.id;
        this.creationTime = account.creationTime;
        this.balance = changedBalance;
    }

    public long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public Account deposit(final BigDecimal amount) {
        return new Account(this, balance.add(amount));
    }

    public Account withdraw(final BigDecimal amount) {
        return new Account(this, balance.subtract(amount));
    }
}
