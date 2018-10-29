package com.github.example.service;

import com.github.example.model.Transaction.TransactionStatus;

import java.util.UUID;


public interface TransactionExecutionService {

    /**
     * Executes money transfer operation for transactions
     * in {@link TransactionStatus#PENDING} status with specified limit.
     *
     * @param limit the number of transaction to be fetched for execution
     */
    void executePending(int limit);

    /**
     * Executes operations in accordance with transaction
     * retrieved by the unique identifier of transaction.
     *
     * @param transactionId the unique identifier of transaction for execution
     * @return {@code true} if transaction is executed by identifier
     */
    boolean executeBy(UUID transactionId);
}
