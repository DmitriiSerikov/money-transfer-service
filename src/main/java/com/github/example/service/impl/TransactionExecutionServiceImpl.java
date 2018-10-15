package com.github.example.service.impl;

import com.github.example.model.Transaction;
import com.github.example.service.TransactionExecutionService;


public class TransactionExecutionServiceImpl implements TransactionExecutionService {

    @Override
    public void executePending(int limit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(Transaction transaction) {
        throw new UnsupportedOperationException();
    }
}
