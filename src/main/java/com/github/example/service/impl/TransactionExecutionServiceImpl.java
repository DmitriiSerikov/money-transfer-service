package com.github.example.service.impl;

import com.github.example.dao.AccountDao;
import com.github.example.dao.TransactionDao;
import com.github.example.exception.CouldNotAcquireLockException;
import com.github.example.model.Account;
import com.github.example.model.Transaction;
import com.github.example.service.TransactionExecutionService;
import io.micronaut.core.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.github.example.model.Transaction.TransactionStatus.PENDING;
import static java.util.stream.Collectors.groupingBy;

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
    public void executePending(int limit) {
        final Collection<Transaction> pendingTransactions = transactionDao.findPending(limit);
        if (CollectionUtils.isNotEmpty(pendingTransactions)) {
            final Map<Long, List<Transaction>> txGroupedBySourceAccountId = pendingTransactions.stream()
                    .collect(groupingBy(Transaction::getSourceAccountId));
            txGroupedBySourceAccountId.values().parallelStream()
                    .forEach(this::processWithOrdering);
        }
    }

    @Override
    public void execute(final Transaction transaction) {
        checkTransactionStatus(transaction);

        final long transactionId = transaction.getId();
        final long sourceAccountId = transaction.getSourceAccountId();
        final long targetAccountId = transaction.getTargetAccountId();
        final BigDecimal amount = transaction.getAmount();

        transactionDao.lockBy(transactionId);
        try {
            acquireLocksWithOrdering(sourceAccountId, targetAccountId);

            final Account sourceAccount = accountDao.getBy(sourceAccountId);
            final Account targetAccount = accountDao.getBy(targetAccountId);

            accountDao.update(sourceAccount.withdraw(amount));
            accountDao.update(targetAccount.deposit(amount));
            transactionDao.update(transaction.executed());
        } catch (CouldNotAcquireLockException ex) {
            LOGGER.info("Execution of the transaction with id:{} will be delayed due to the lock of one of the accounts", transactionId, ex);
        } catch (Exception ex) {
            LOGGER.error("Failed to execute transaction with id:{} between accounts with source id:{} and target id:{}", transactionId, sourceAccountId, targetAccountId, ex);
            transactionDao.update(transaction.failed());
        } finally {
            accountDao.unlockBy(sourceAccountId);
            accountDao.unlockBy(targetAccountId);
            transactionDao.unlockBy(transactionId);
        }
    }

    private void processWithOrdering(final List<Transaction> transactions) {
        transactions.stream()
                .peek(transaction -> LOGGER.info("Performing transaction with id:{} at {}", transaction.getId(), Instant.now()))
                .forEach(this::executePendingTransaction);
    }

    private void executePendingTransaction(final Transaction transaction) {
        try {
            execute(transaction);
        } catch (CouldNotAcquireLockException | IllegalStateException ex) {
            LOGGER.error("Performing of the transaction with id:{} failed", transaction.getId(), ex);
        }
    }

    private void acquireLocksWithOrdering(final long sourceAccountId, final long targetAccountId) {
        final long firstLock;
        final long secondLock;

        if (sourceAccountId < targetAccountId) {
            firstLock = sourceAccountId;
            secondLock = targetAccountId;
        } else {
            firstLock = targetAccountId;
            secondLock = sourceAccountId;
        }

        accountDao.lockBy(firstLock);
        accountDao.lockBy(secondLock);
    }

    private void checkTransactionStatus(final Transaction transaction) {
        if (!PENDING.equals(transaction.getStatus())) {
            throw new IllegalStateException("Transaction is already performed with id:" + transaction.getId());
        }
    }
}
