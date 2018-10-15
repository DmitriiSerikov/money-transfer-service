package com.github.example.service;

import com.github.example.exception.CouldNotAcquireLockException;
import com.github.example.model.Transaction;
import com.github.example.model.Transaction.TransactionStatus;


public interface TransactionExecutionService {

    /**
     * Executes money transfer operation for transactions
     * in {@link TransactionStatus#PENDING} status with specified limit.
     *
     * @param limit the number of transaction to be fetched for execution
     */
    void executePending(int limit);

    /**
     * Executes money transfer operation in accordance with transaction.
     *
     * @param transaction the transaction for execution
     * @throws CouldNotAcquireLockException if transaction is already locked by another thread
     * @throws IllegalStateException        if transaction is already executed
     */
    void execute(Transaction transaction);
}
