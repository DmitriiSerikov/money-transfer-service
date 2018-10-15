package com.github.example.service.impl;

import com.github.example.dto.request.CommandCreateAccount;
import com.github.example.model.Account;
import com.github.example.service.AccountService;

import java.util.Collection;
import java.util.Collections;


public class AccountServiceImpl implements AccountService {

    @Override
    public Collection<Account> getAll() {
        return Collections.emptyList();
    }

    @Override
    public Account getById(long accountId) {
        return null;
    }

    @Override
    public Account createBy(CommandCreateAccount command) {
        return null;
    }
}
