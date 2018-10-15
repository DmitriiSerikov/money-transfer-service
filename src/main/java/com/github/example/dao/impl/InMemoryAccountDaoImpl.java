package com.github.example.dao.impl;

import com.github.example.dao.AccountDao;
import com.github.example.exception.EntityAlreadyExistsException;
import com.github.example.exception.EntityNotFoundException;
import com.github.example.holder.LockHolder;
import com.github.example.model.Account;
import org.modelmapper.internal.util.Assert;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableCollection;

@Singleton
public class InMemoryAccountDaoImpl implements AccountDao {

    private final Map<Long, Account> storage = new ConcurrentHashMap<>();
    private final LockHolder lockHolder;

    @Inject
    public InMemoryAccountDaoImpl(final LockHolder lockHolder) {
        this.lockHolder = lockHolder;
    }

    @Override
    public Collection<Account> findAll() {
        return unmodifiableCollection(storage.values());
    }

    @Override
    public Account insert(final Account account) {
        Assert.notNull(account);

        return storage.compute(account.getId(), (id, previousValue) -> {
            if (previousValue != null) {
                throw new EntityAlreadyExistsException("Account already exists for id:" + id);
            }
            return account;
        });
    }

    @Override
    public Account getBy(final long accountId) {
        return Optional.ofNullable(storage.get(accountId))
                .orElseThrow(() -> new EntityNotFoundException("Account not exists for id:" + accountId));
    }

    @Override
    public void update(final Account account) {
        Assert.notNull(account);

        final long accountId = account.getId();

        lockBy(accountId);
        try {
            storage.put(accountId, account);
        } finally {
            unlockBy(accountId);
        }
    }

    @Override
    public void lockBy(final long accountId) {
        lockHolder.acquire(getLockId(accountId));
    }

    @Override
    public void unlockBy(final long accountId) {
        lockHolder.release(getLockId(accountId));
    }

    private String getLockId(final long accountId) {
        return "Account_" + accountId;
    }
}
