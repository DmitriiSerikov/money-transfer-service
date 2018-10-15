package com.github.example.dao;

import com.github.example.exception.CouldNotAcquireLockException;
import com.github.example.exception.EntityAlreadyExistsException;
import com.github.example.exception.EntityNotFoundException;
import com.github.example.model.Account;

import java.util.Collection;


public interface AccountDao {

    /**
     * Returns a collection of all accounts from the storage.
     *
     * @return all accounts in the storage
     */
    Collection<Account> findAll();

    /**
     * Stores newly created account into the storage.
     *
     * @param account the account entity for storing into the storage
     * @return the account entity successfully persisted into the storage
     * @throws EntityAlreadyExistsException if account with the unique identificator is already present
     */
    Account insert(Account account);

    /**
     * Returns account by the unique identificator of account
     * in case it's already present in the storage.
     *
     * @param accountId the unique identificator of account
     * @return returns account found by unique identificator
     * @throws EntityNotFoundException if storage doesn't contain account with specified unique identificator
     */
    Account getBy(long accountId);

    /**
     * Update account in the storage in case it already present
     * otherwise puts it into the storage.
     *
     * @param account the account entity for storing into the storage
     */
    void update(Account account);

    /**
     * Acquires the lock for account entity by the unique identificator.
     *
     * @param accountId the unique identificator of account
     * @throws CouldNotAcquireLockException if lock is already acquired or thread is interrupted
     */
    void lockBy(long accountId);

    /**
     * Releases lock for account entity by the unique identificator.
     *
     * @param accountId the unique identificator of account
     */
    void unlockBy(long accountId);
}
