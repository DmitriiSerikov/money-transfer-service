package com.github.example.dao.impl;

import com.github.example.dao.TransactionDao;
import com.github.example.exception.EntityAlreadyExistsException;
import com.github.example.exception.EntityNotFoundException;
import com.github.example.holder.LockHolder;
import com.github.example.model.Transaction;
import org.modelmapper.internal.util.Assert;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static com.github.example.model.Transaction.TransactionStatus.PENDING;
import static java.util.Comparator.comparing;

@Singleton
public class InMemoryTransactionDaoImpl implements TransactionDao {

    private final ConcurrentMap<UUID, Transaction> storage = new ConcurrentHashMap<>();
    private final LockHolder lockHolder;

    @Inject
    public InMemoryTransactionDaoImpl(final LockHolder lockHolder) {
        this.lockHolder = lockHolder;
    }

    @Override
    public Collection<Transaction> findAll() {
        return storage.values().stream()
                .sorted(comparing(Transaction::getCreatedAt))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Transaction> findPending(int limit) {
        return findAll().stream()
                .filter(transaction -> PENDING.equals(transaction.getStatus()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public Transaction insert(final Transaction transaction) {
        Assert.notNull(transaction);

        return storage.compute(transaction.getId(), (id, previousValue) -> {
            if (previousValue != null) {
                throw new EntityAlreadyExistsException("Transaction already exists for id:" + id);
            }
            return transaction;
        });
    }

    @Override
    public Transaction getBy(final UUID transactionId) {
        return Optional.ofNullable(storage.get(transactionId))
                .orElseThrow(() -> new EntityNotFoundException("Transaction not exists for id:" + transactionId));
    }

    @Override
    public void update(final Transaction transaction) {
        Assert.notNull(transaction);

        final UUID transactionId = transaction.getId();

        lockBy(transactionId);
        try {
            storage.put(transactionId, transaction);
        } finally {
            unlockBy(transactionId);
        }
    }

    @Override
    public void lockBy(final UUID transactionId) {
        lockHolder.acquire(getLockId(transactionId));
    }

    @Override
    public void unlockBy(final UUID transactionId) {
        lockHolder.release(getLockId(transactionId));
    }

    private String getLockId(final UUID transactionId) {
        return "Transaction_" + transactionId;
    }
}
