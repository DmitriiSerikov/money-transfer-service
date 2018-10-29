package com.github.example.service.impl

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.TestSupport
import com.github.example.UnitTest
import com.github.example.dao.AccountDao
import com.github.example.dto.request.CommandCreateAccount
import com.github.example.exception.EntityNotFoundException
import com.github.example.model.Account
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.Specification

import static java.math.BigDecimal.ONE

@Category(UnitTest)
class AccountServiceSpec extends Specification implements TestSupport {

    @Subject
    AccountServiceImpl accountService

    @Collaborator
    AccountDao accountDao = Mock()

    def account = accountStub()

    @Test
    def 'should return empty collection of accounts when accounts storage returns empty collection'() {
        given:
        accountDao.findAll() >> []

        when:
        def result = accountService.getAll()

        then:
        result.empty
    }

    @Test
    def 'should return not empty collection of accounts when accounts storage returns not empty collection'() {
        given:
        def accounts = [account]
        accountDao.findAll() >> accounts

        when:
        def result = accountService.getAll()

        then:
        !result.empty
    }

    @Test
    def 'should throw exception when accounts storage does not contains entity for given id and throws exception'() {
        given:
        accountDao.getBy(notExistResourceId) >> { throw new EntityNotFoundException('Not found') }

        when:
        accountService.getById notExistResourceId

        then:
        thrown EntityNotFoundException
    }

    @Test
    def 'should return account by given id when accounts storage contains entity for given id'() {
        given:
        def accountId = account.id
        accountDao.getBy(accountId) >> account

        when:
        def result = accountService.getById accountId

        then:
        result == account
    }

    @Test
    def 'should throw exception when command for account creation is null'() {
        when:
        accountService.createBy null

        then:
        thrown IllegalArgumentException
    }

    @Test
    def 'should insert account into accounts storage when account successfully created by command'() {
        given:
        def initialBalance = ONE
        def command = new CommandCreateAccount(initialBalance: initialBalance)

        when:
        accountService.createBy command

        then:
        1 * accountDao.insert({ it.balance == initialBalance } as Account)
    }

    @Test
    def 'should return created account with initial balance when account successfully inserted into accounts storage'() {
        given:
        def command = new CommandCreateAccount(initialBalance: amount)

        when:
        def result = accountService.createBy command

        then:
        result.balance == amount
    }
}
