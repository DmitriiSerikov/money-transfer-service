package com.github.example.service.impl;

import com.github.example.dao.AccountDao;
import com.github.example.dao.TransactionDao;
import com.github.example.exception.CouldNotAcquireLockException;
import com.github.example.exception.EntityNotFoundException;
import com.github.example.model.Account;
import com.github.example.model.Transaction;
import com.github.example.model.TransactionEntry;
import com.github.example.service.TransactionExecutionService;
import io.micronaut.core.util.CollectionUtils;
import org.modelmapper.internal.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.example.model.Transaction.TransactionStatus.PENDING;

@Singleton
public class TransactionExecutionServiceImpl implements TransactionExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionExecutionServiceImpl.class);

    private final TransactionDao transactionDao;
    private final AccountDao accountDao;

    @Inject
    public TransactionExecutionServiceImpl(final TransactionDao transactionDao, final AccountDao accountDao) {
        this.transactionDao = transactionDao;
        this.accountDao = accountDao;
    }

    @Override
    public void executePending(final int limit) {
        final Collection<Transaction> pendingTransactions = transactionDao.findPending(limit);
        if (CollectionUtils.isNotEmpty(pendingTransactions)) {
            LOGGER.info("Found {} pending transactions for execution, limit for bulk operation is {}", pendingTransactions.size(), limit);
            pendingTransactions.stream()
                    .map(Transaction::getId)
                    .forEach(this::executeBy);
        }
    }

    @Override
    public boolean executeBy(final UUID transactionId) {
        Assert.notNull(transactionId, "Transaction identifier");

        LOGGER.debug("Start execution of transaction with id: {} at {}", transactionId, Instant.now());
        try {
            transactionDao.lockBy(transactionId);

            final Transaction transaction = transactionDao.getBy(transactionId);
            checkStatusOf(transaction);

            return executeLocked(transaction);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("Transaction with id: {} is not executed cause transaction is not found", transactionId, ex);
            return false;
        } catch (CouldNotAcquireLockException | IllegalStateException ex) {
            LOGGER.debug("Transaction with id: {} is locked by another thread or already executed", transactionId, ex);
            return false;
        } finally {
            transactionDao.unlockBy(transactionId);
            LOGGER.debug("Finished execution of transaction with id: {} at {}", transactionId, Instant.now());
        }
    }

    private boolean executeLocked(final Transaction transaction) {
        final Set<TransactionEntry> entries = transaction.getEntries();
        final UUID transactionId = transaction.getId();

        try {
            executeWithOrdering(accountDao::lockBy, entries);

            final List<Account> updatedAccounts = entries.stream()
                    .map(this::updateAccountByEntry)
                    .collect(Collectors.toList());

            updatedAccounts.forEach(accountDao::update);
            transactionDao.update(transaction.executed());

            return true;
        } catch (CouldNotAcquireLockException ex) {
            LOGGER.debug("Execution of the transaction with id: {} will be delayed cause failed to lock on one of the accounts", transactionId, ex);
            return false;
        } catch (Exception ex) {
            LOGGER.debug("Transaction with id: {} executed with failure", transactionId, ex);
            transactionDao.update(transaction.failed(ex.getMessage()));
            return true;
        } finally {
            executeWithOrdering(accountDao::unlockBy, entries);
        }
    }

    private void checkStatusOf(final Transaction transaction) {
        if (!PENDING.equals(transaction.getStatus())) {
            throw new IllegalStateException("Transaction is already performed with id: " + transaction.getId());
        }
    }

    private Account updateAccountByEntry(final TransactionEntry entry) {
        final Account account = accountDao.getBy(entry.getAccountId());
        return account.addBy(entry);
    }

    private void executeWithOrdering(final Consumer<UUID> operation, final Set<TransactionEntry> entries) {
        entries.stream()
                .map(TransactionEntry::getAccountId)
                .sorted()
                .forEach(operation);
    }
}
