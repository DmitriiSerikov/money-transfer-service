package com.github.example.model;

import org.modelmapper.internal.util.Assert;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class TransactionBuilder {

    private final String referenceId;
    private final Set<TransactionEntry> transactionEntries;

    private TransactionBuilder(final String referenceId) {
        this.referenceId = referenceId;
        this.transactionEntries = new HashSet<>();
    }

    public TransactionBuilder withdraw(final UUID accountId, final BigDecimal amount) {
        checkOperationAmountPositive(amount);
        final TransactionEntry entry = new TransactionEntry(accountId, amount.negate());
        this.transactionEntries.add(entry);
        return this;
    }

    public TransactionBuilder deposit(final UUID accountId, final BigDecimal amount) {
        checkOperationAmountPositive(amount);
        final TransactionEntry entry = new TransactionEntry(accountId, amount);
        this.transactionEntries.add(entry);
        return this;
    }

    public Transaction build() {
        checkTransactionIsBalanced(transactionEntries);
        return new Transaction(referenceId, transactionEntries);
    }

    public static TransactionBuilder builder(final String referenceId) {
        return new TransactionBuilder(referenceId);
    }

    private static void checkTransactionIsBalanced(final Set<TransactionEntry> transactionEntries) {
        final BigDecimal entriesSum = transactionEntries.stream()
                .map(TransactionEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Assert.isTrue(entriesSum.signum() == 0, "Summary of transaction entries should be zero");
    }

    private static void checkOperationAmountPositive(final BigDecimal amount) {
        Assert.notNull(amount, "Operation amount");
        Assert.isTrue(amount.signum() > 0, "Operation amount should be positive");
    }
}
