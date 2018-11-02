package com.github.example.service;

import com.github.example.dto.request.CommandPerformTransfer;
import com.github.example.exception.EntityNotFoundException;
import com.github.example.model.Transaction;

import java.util.Collection;
import java.util.UUID;


public interface TransactionService {

    /**
     * Returns all transactions.
     *
     * @return the collection of all transactions
     */
    Collection<Transaction> getAll();

    /**
     * Returns transaction by the unique identifier of transaction.
     *
     * @param transactionId the unique identifier of transaction
     * @return the transaction found by unique identifier
     * @throws EntityNotFoundException if transaction is not found by unique identifier
     */
    Transaction getById(UUID transactionId);

    /**
     * Creates money transfer transaction in accordance
     * with command and tries to execute it synchronously.
     * In case it's not executed synchronously it will be
     * postponed and executed asynchronously by scheduled job.
     *
     * @param command dto with attributes for transaction creation
     * @return the transaction with actual status of execution
     * @throws IllegalArgumentException if transfer to the account with same identifier as source,
     *                                  amount for transaction is {@code null} or less than zero
     */
    Transaction transferBy(CommandPerformTransfer command);
}
