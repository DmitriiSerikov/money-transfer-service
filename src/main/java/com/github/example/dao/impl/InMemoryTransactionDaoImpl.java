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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.github.example.model.Transaction.TransactionStatus.PENDING;
import static java.util.Collections.unmodifiableCollection;

@Singleton
public class InMemoryTransactionDaoImpl implements TransactionDao {

    private final Map<Long, Transaction> storage = new ConcurrentHashMap<>();
    private final LockHolder lockHolder;

    @Inject
    public InMemoryTransactionDaoImpl(final LockHolder lockHolder) {
        this.lockHolder = lockHolder;
    }

    @Override
    public Collection<Transaction> findAll() {
        return unmodifiableCollection(storage.values());
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
    public Transaction getBy(final long transactionId) {
        return Optional.ofNullable(storage.get(transactionId))
                .orElseThrow(() -> new EntityNotFoundException("Transaction not exists for id:" + transactionId));
    }

    @Override
    public void update(final Transaction transaction) {
        Assert.notNull(transaction);

        final long transactionId = transaction.getId();

        lockBy(transactionId);
        try {
            storage.put(transactionId, transaction);
        } finally {
            unlockBy(transactionId);
        }
    }

    @Override
    public void lockBy(final long transactionId) {
        lockHolder.acquire(getLockId(transactionId));
    }

    @Override
    public void unlockBy(final long transactionId) {
        lockHolder.release(getLockId(transactionId));
    }

    private String getLockId(final long transactionId) {
        return "Transaction_" + transactionId;
    }
}
