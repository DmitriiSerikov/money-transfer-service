package com.github.example.dao.impl;

import com.github.example.dao.TransactionDao;
import com.github.example.model.Transaction;

import java.util.Collection;


public class InMemoryTransactionDaoImpl implements TransactionDao {

    @Override
    public Collection<Transaction> findAll() {
        return null;
    }

    @Override
    public Collection<Transaction> findPending(int limit) {
        return null;
    }

    @Override
    public Transaction insert(Transaction transaction) {
        return null;
    }

    @Override
    public Transaction getBy(long transactionId) {
        return null;
    }

    @Override
    public void update(Transaction transaction) {

    }

    @Override
    public void lockBy(long transactionId) {

    }

    @Override
    public void unlockBy(long transactionId) {

    }
}
