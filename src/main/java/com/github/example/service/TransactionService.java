package com.github.example.service;

import com.github.example.dto.request.CommandCreateTransaction;
import com.github.example.exception.EntityNotFoundException;
import com.github.example.model.Transaction;

import java.util.Collection;


public interface TransactionService {

    /**
     * Returns all transactions.
     *
     * @return the collection of all transactions
     */
    Collection<Transaction> getAll();

    /**
     * Returns transaction by the unique identificator of transaction.
     *
     * @param transactionId the unique identificator of transaction
     * @return the transaction found by unique identificator
     * @throws EntityNotFoundException if transaction is not found by unique identificator
     */
    Transaction getById(long transactionId);

    /**
     * Creates transaction with attributes specified by command.
     *
     * @param command dto with attributes for transaction creation
     * @return the successfully created transaction
     * @throws IllegalArgumentException if one of transaction participants
     *                                  is not found by the unique identificator,
     *                                  transaction between accounts with same identificator,
     *                                  amount for transaction is {@code null} or less than zero
     */
    Transaction createBy(CommandCreateTransaction command);
}
