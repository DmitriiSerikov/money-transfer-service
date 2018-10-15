package com.github.example.service;


import com.github.example.dto.request.CommandCreateAccount;
import com.github.example.exception.EntityNotFoundException;
import com.github.example.model.Account;

import java.util.Collection;


public interface AccountService {

    /**
     * Returns all accounts.
     *
     * @return the collection of all accounts
     */
    Collection<Account> getAll();

    /**
     * Returns account by the unique identificator of account.
     *
     * @param accountId the unique identificator of account
     * @return the account found by unique identificator
     * @throws EntityNotFoundException if account is not found by unique identificator
     */
    Account getById(long accountId);

    /**
     * Creates account with attributes specified by command.
     *
     * @param command dto with attributes for account creation
     * @return the successfully created account
     * @throws IllegalArgumentException if initial balance for account creation
     *                                  is {@code null} or less than zero
     */
    Account createBy(CommandCreateAccount command);
}
