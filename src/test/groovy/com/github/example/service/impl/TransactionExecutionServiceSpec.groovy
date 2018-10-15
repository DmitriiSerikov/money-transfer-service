package com.github.example.service.impl

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.dao.AccountDao
import com.github.example.dao.TransactionDao
import com.github.example.exception.CouldNotAcquireLockException
import com.github.example.exception.EntityNotFoundException
import com.github.example.model.Account
import com.github.example.model.Transaction
import org.junit.Test
import spock.lang.Shared
import spock.lang.Specification

import static com.github.example.model.Transaction.TransactionStatus.FAILED
import static com.github.example.model.Transaction.TransactionStatus.SUCCESS
import static java.math.BigDecimal.ONE

/**
 * Unit test for {@link TransactionExecutionServiceImpl}
 */
class TransactionExecutionServiceSpec extends Specification {

    @Subject
    TransactionExecutionServiceImpl executionService

    @Collaborator
    AccountDao accountDao = Mock()
    @Collaborator
    TransactionDao transactionDao = Mock()

    @Shared
    def firstAccount = new Account(ONE)
    @Shared
    def secondAccount = new Account(ONE)
    def sourceAccountId = firstAccount.id
    def targetAccountId = secondAccount.id
    def transaction = new Transaction(sourceAccountId, targetAccountId, ONE)
    def transactionId = transaction.id
    def limit = 10

    def setup() {
        accountDao.getBy(sourceAccountId) >> firstAccount
        accountDao.getBy(targetAccountId) >> secondAccount
    }

    @Test
    def "should not execute any transaction when transaction storage doesn't contains pending transactions"() {
        given:
        transactionDao.findPending(limit) >> []

        when:
        executionService.executePending limit

        then:
        0 * transactionDao.lockBy(_)
    }

    @Test
    def "should execute transactions with same source account sequentially when transaction storage contains pending transactions only with one source account"() {
        given:
        transactionDao.findPending(limit) >> [transaction, transaction]

        when:
        executionService.executePending limit

        then:
        2 * transactionDao.lockBy(transactionId)
    }

    @Test
    def "should not throw exception when couldn't lock one of transaction during execution of pending transactions"() {
        given:
        transactionDao.findPending(limit) >> [transaction]
        and:
        transactionDao.lockBy(transactionId) >> { throw new CouldNotAcquireLockException("Failed") }

        when:
        executionService.executePending limit

        then:
        notThrown CouldNotAcquireLockException
    }

    @Test
    def "should not throw exception when one of pending transactions already executed during execution of pending transactions"() {
        given:
        def executedTransaction = transaction.executed()
        transactionDao.findPending(limit) >> [executedTransaction]

        when:
        executionService.executePending limit

        then:
        notThrown IllegalStateException
    }

    @Test
    def "should throw exception when try executes not pending transaction"() {
        given:
        def executedTransaction = transaction.executed()

        when:
        executionService.execute executedTransaction

        then:
        thrown IllegalStateException
    }

    @Test
    def "should lock transaction for id by transactions storage when executes transaction"() {
        when:
        executionService.execute transaction

        then:
        1 * transactionDao.lockBy(transactionId)
        interaction { ensureResourcesUnlockedBy sourceAccountId, targetAccountId, transactionId }
    }

    @Test
    def "should throw exception when transactions storage failed to lock transaction and throws exception"() {
        given:
        def transactionId = transaction.id
        and:
        transactionDao.lockBy(transactionId) >> { throw new CouldNotAcquireLockException("Failed") }

        when:
        executionService.execute transaction

        then:
        thrown CouldNotAcquireLockException
    }

    @Test
    def "should primarily lock account with lower id and then with greater id by accounts storage when executes transaction"() {
        given:
        def transaction = new Transaction(sourceId, targetId, ONE)

        when:
        executionService.execute transaction

        then:
        1 * accountDao.lockBy(firstLock)
        1 * accountDao.lockBy(secondLock)
        interaction { ensureResourcesUnlockedBy sourceAccountId, targetAccountId, transaction.id }

        where:
        sourceId         | targetId         || firstLock       | secondLock
        firstAccount.id  | secondAccount.id || firstAccount.id | secondAccount.id
        secondAccount.id | firstAccount.id  || firstAccount.id | secondAccount.id
    }

    @Test
    def "should mark transaction as failed when accounts storage not found source account and throws exception"() {
        given:
        def sourceAccountId = 10
        def transaction = new Transaction(sourceAccountId, targetAccountId, ONE)
        and:
        accountDao.getBy(sourceAccountId) >> { throw new EntityNotFoundException("Not found") }

        when:
        executionService.execute transaction

        then:
        1 * transactionDao.update({ it.status == FAILED } as Transaction)
        interaction { ensureResourcesUnlockedBy sourceAccountId, targetAccountId, transaction.id }
    }

    @Test
    def "should mark transaction as failed when accounts storage not found target account and throws exception"() {
        given:
        def targetAccountId = 10
        def transaction = new Transaction(sourceAccountId, targetAccountId, ONE)
        and:
        accountDao.getBy(targetAccountId) >> { throw new EntityNotFoundException("Not found") }

        when:
        executionService.execute transaction

        then:
        1 * transactionDao.update({ it.status == FAILED } as Transaction)
        interaction { ensureResourcesUnlockedBy sourceAccountId, targetAccountId, transaction.id }
    }

    @Test
    def "should mark transaction as failed when source account balance after withdrawal less than zero"() {
        given:
        def sourceAccount = new Account(sourceAccountBalance as BigDecimal)
        def sourceAccountId = sourceAccount.id
        def transaction = new Transaction(sourceAccountId, targetAccountId, amount as BigDecimal)
        and:
        accountDao.getBy(sourceAccountId) >> sourceAccount

        when:
        executionService.execute transaction

        then:
        1 * transactionDao.update({ it.status == FAILED } as Transaction)
        interaction { ensureResourcesUnlockedBy sourceAccountId, targetAccountId, transaction.id }

        where:
        sourceAccountBalance | amount
        1                    | 2
        5                    | 10
        10                   | 20
    }

    @Test
    def "should withdraw from source account and update it in storage when transaction executed successfully"() {
        given:
        def sourceAccount = new Account(sourceAccountBalance as BigDecimal)
        def sourceAccountId = sourceAccount.id
        def transaction = new Transaction(sourceAccountId, targetAccountId, amount as BigDecimal)
        and:
        accountDao.getBy(sourceAccountId) >> sourceAccount

        when:
        executionService.execute transaction

        then:
        1 * accountDao.update({
            it == sourceAccount
            it.balance == expectedBalance
        } as Account)
        interaction { ensureResourcesUnlockedBy sourceAccountId, targetAccountId, transaction.id }

        where:
        sourceAccountBalance | amount || expectedBalance
        1                    | 1      || 0
        10.5                 | 0.5    || 10
        20.995               | 5.395  || 15.6
    }

    @Test
    def "should deposit into target account and update it in storage when transaction executed successfully"() {
        given:
        def targetAccount = new Account(targetAccountBalance as BigDecimal)
        def targetAccountId = targetAccount.id
        def transaction = new Transaction(sourceAccountId, targetAccountId, amount as BigDecimal)
        and:
        accountDao.getBy(targetAccountId) >> targetAccount

        when:
        executionService.execute transaction

        then:
        1 * accountDao.update({
            it == targetAccount
            it.balance == expectedBalance
        } as Account)
        interaction { ensureResourcesUnlockedBy sourceAccountId, targetAccountId, transaction.id }

        where:
        targetAccountBalance | amount || expectedBalance
        0                    | 1      || 1
        10.6                 | 0.5    || 11.1
        20.995               | 0.105  || 21.1
    }

    @Test
    def "should mark transaction as succeed when transaction executed successfully"() {
        when:
        executionService.execute transaction

        then:
        1 * transactionDao.update({ it.status == SUCCESS } as Transaction)
        interaction { ensureResourcesUnlockedBy sourceAccountId, targetAccountId, transactionId }
    }

    def ensureResourcesUnlockedBy(long sourceAccountId, long targetAccountId, long transactionId) {
        1 * accountDao.unlockBy(sourceAccountId)
        1 * accountDao.unlockBy(targetAccountId)
        1 * transactionDao.unlockBy(transactionId)
    }
}
