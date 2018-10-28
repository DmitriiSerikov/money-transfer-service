package com.github.example.service.impl;

import com.github.example.dao.TransactionDao;
import com.github.example.dto.request.CommandPerformTransfer;
import com.github.example.exception.CouldNotAcquireLockException;
import com.github.example.exception.EntityNotFoundException;
import com.github.example.model.Transaction;
import com.github.example.service.TransactionExecutionService;
import com.github.example.service.TransactionService;
import io.micronaut.http.server.exceptions.InternalServerException;
import org.modelmapper.internal.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.UUID;

@Singleton
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);

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

        try {
            final Transaction transaction = new Transaction(command.getReferenceId(),
                    command.getSourceAccountId(),
                    command.getTargetAccountId(),
                    command.getAmount());

            transactionDao.insert(transaction);
            return tryPerformSynchronouslyBy(transaction.getId());
        } catch (EntityNotFoundException ex) {
            throw new InternalServerException("Just stored transaction is not found in storage during execution", ex);
        }
    }

    private Transaction tryPerformSynchronouslyBy(final UUID transactionId) {
        try {
            return executionService.executeBy(transactionId);
        } catch (CouldNotAcquireLockException | IllegalStateException ex) {
            LOGGER.debug("Transaction with id: {} were performed asynchronously, try to obtain it from storage with actual status", transactionId, ex);
            return getById(transactionId);
        }
    }
}