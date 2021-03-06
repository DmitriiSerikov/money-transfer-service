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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Collections.unmodifiableCollection;

@Singleton
public class InMemoryAccountDaoImpl implements AccountDao {

    private static final String ACCOUNT_ID_PARAM = "Account identifier";
    private static final String ACCOUNT_PARAM = "Account";

    private final ConcurrentMap<UUID, Account> storage = new ConcurrentHashMap<>();
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
    public void insert(final Account account) {
        Assert.notNull(account, ACCOUNT_PARAM);

        final UUID accountId = account.getId();

        if (storage.putIfAbsent(accountId, account) != null) {
            throw new EntityAlreadyExistsException("Account already exists for id:" + accountId);
        }
    }

    @Override
    public Account getBy(final UUID accountId) {
        Assert.notNull(accountId, ACCOUNT_ID_PARAM);

        return Optional.ofNullable(storage.get(accountId))
                .orElseThrow(() -> new EntityNotFoundException("Account not exists for id: " + accountId));
    }

    @Override
    public void update(final Account account) {
        Assert.notNull(account, ACCOUNT_PARAM);

        final UUID accountId = account.getId();

        lockBy(accountId);
        try {
            if (storage.replace(accountId, account) == null) {
                throw new EntityNotFoundException("Account not exists for id: " + accountId);
            }
        } finally {
            unlockBy(accountId);
        }
    }

    @Override
    public void lockBy(final UUID accountId) {
        Assert.notNull(accountId, ACCOUNT_ID_PARAM);

        lockHolder.acquire(getLockId(accountId));
    }

    @Override
    public void unlockBy(final UUID accountId) {
        Assert.notNull(accountId, ACCOUNT_ID_PARAM);

        lockHolder.release(getLockId(accountId));
    }

    private String getLockId(final UUID accountId) {
        return "Account_" + accountId;
    }
}
