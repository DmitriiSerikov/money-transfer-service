package com.github.example.dao;

import com.github.example.exception.CouldNotAcquireLockException;
import com.github.example.exception.EntityAlreadyExistsException;
import com.github.example.exception.EntityNotFoundException;
import com.github.example.model.Transaction;
import com.github.example.model.Transaction.TransactionStatus;

import java.util.Collection;


public interface TransactionDao {

    /**
     * Returns a collection of all transactions from the storage.
     *
     * @return all transactions in the storage
     */
    Collection<Transaction> findAll();

    /**
     * Returns a collection of transactions limited by {@code limit}
     * with {@link TransactionStatus#PENDING} status from the storage.
     *
     * @return transactions int the storage filtered by predicate
     */
    Collection<Transaction> findPending(int limit);

    /**
     * Stores newly created transaction into the storage.
     *
     * @param transaction the transaction entity for storing into the storage
     * @return the transaction entity successfully persisted into the storage
     * @throws EntityAlreadyExistsException if transaction with the unique identificator is already present
     */
    Transaction insert(Transaction transaction);

    /**
     * Returns transaction by the unique identificator of transaction
     * in case it's already present in the storage.
     *
     * @param transactionId the unique identificator of transaction
     * @return returns transaction found by unique identificator
     * @throws EntityNotFoundException if storage doesn't contain transaction with specified unique identificator
     */
    Transaction getBy(long transactionId);

    /**
     * Update transaction in the storage in case it already present
     * otherwise puts it into the storage.
     *
     * @param transaction the transaction entity for storing into the storage
     */
    void update(Transaction transaction);

    /**
     * Acquires the lock for transaction entity by the unique identificator.
     *
     * @param transactionId the unique identificator of transaction
     * @throws CouldNotAcquireLockException if lock is already acquired or thread is interrupted
     */
    void lockBy(long transactionId);

    /**
     * Releases lock for transaction entity by the unique identificator.
     *
     * @param transactionId the unique identificator of transaction
     */
    void unlockBy(long transactionId);
}
