package com.github.example.service.impl;

import com.github.example.dto.request.CommandCreateTransaction;
import com.github.example.model.Transaction;
import com.github.example.service.TransactionService;

import java.util.Collection;
import java.util.Collections;


public class TransactionServiceImpl implements TransactionService {

    @Override
    public Collection<Transaction> getAll() {
        return Collections.emptyList();
    }

    @Override
    public Transaction getById(long transactionId) {
        return null;
    }

    @Override
    public Transaction createBy(CommandCreateTransaction command) {
        return null;
    }
}
