package com.github.example.dao.impl;

import com.github.example.dao.AccountDao;
import com.github.example.model.Account;

import java.util.Collection;


public class InMemoryAccountDaoImpl implements AccountDao {

    @Override
    public Collection<Account> findAll() {
        return null;
    }

    @Override
    public Account insert(Account account) {
        return null;
    }

    @Override
    public Account getBy(long accountId) {
        return null;
    }

    @Override
    public void update(Account account) {

    }

    @Override
    public void lockBy(long accountId) {

    }

    @Override
    public void unlockBy(long accountId) {

    }
}
