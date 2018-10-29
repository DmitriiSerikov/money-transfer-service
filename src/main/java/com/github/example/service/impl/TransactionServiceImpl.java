package com.github.example.service.impl;

import com.github.example.dao.TransactionDao;
import com.github.example.dto.request.CommandPerformTransfer;
import com.github.example.model.Transaction;
import com.github.example.model.TransactionBuilder;
import com.github.example.service.TransactionExecutionService;
import com.github.example.service.TransactionService;
import org.modelmapper.internal.util.Assert;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

@Singleton
public class TransactionServiceImpl implements TransactionService {

    private final TransactionDao transactionDao;
    private final TransactionExecutionService executionService;

    @Inject
    public TransactionServiceImpl(final TransactionDao transactionDao, final TransactionExecutionService executionService) {
        this.transactionDao = transactionDao;
        this.executionService = executionService;
    }

    @Override
    public Collection<Transaction> getAll() {
        return transactionDao.findAll();
    }

    @Override
    public Transaction getById(final UUID transactionId) {
        return transactionDao.getBy(transactionId);
    }

    @Override
    public Transaction transferBy(final CommandPerformTransfer command) {
        Assert.notNull(command);

        final UUID sourceAccountId = command.getSourceAccountId();
        final UUID targetAccountId = command.getTargetAccountId();
        final BigDecimal amount = command.getAmount();

        if (sourceAccountId.equals(targetAccountId)) {
            throw new IllegalArgumentException("Money transfer to the same account is not allowed");
        }

        final Transaction transaction = TransactionBuilder.builder(command.getReferenceId())
                .withdraw(sourceAccountId, amount)
                .deposit(targetAccountId, amount)
                .build();

        transactionDao.insert(transaction);

        final UUID transactionId = transaction.getId();
        final boolean isExecuted = executionService.executeBy(transactionId);
        return isExecuted ? getById(transactionId) : transaction;
    }
}