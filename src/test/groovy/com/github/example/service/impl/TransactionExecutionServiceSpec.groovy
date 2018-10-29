package com.github.example.service.impl

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.TestSupport
import com.github.example.UnitTest
import com.github.example.dao.AccountDao
import com.github.example.dao.TransactionDao
import com.github.example.exception.CouldNotAcquireLockException
import com.github.example.exception.EntityNotFoundException
import com.github.example.model.Account
import com.github.example.model.Transaction
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.Specification

import static com.github.example.model.Transaction.TransactionStatus.FAILED
import static com.github.example.model.Transaction.TransactionStatus.SUCCESS

@Category(UnitTest)
class TransactionExecutionServiceSpec extends Specification implements TestSupport {

    @Subject
    TransactionExecutionServiceImpl executionService

    @Collaborator
    AccountDao accountDao = Mock()
    @Collaborator
    TransactionDao transactionDao = Mock()

    def limit = 10
    def transaction = transactionStub()
    def transactionId = transaction.id

    def setup() {
        transactionDao.getBy(transactionId) >> transaction
        accountDao.getBy(firstAccountId) >> accountStub()
        accountDao.getBy(secondAccountId) >> accountStub()
    }

    @Test
    def 'should not execute any transactions when transaction storage does not contains pending transactions'() {
        given:
        transactionDao.findPending(limit) >> []

        when:
        executionService.executePending limit

        then:
        0 * transactionDao.lockBy(_)
    }

    @Test
    def 'should execute transactions sequentially when transaction storage contains pending transactions'() {
        given:
        def pendingTransaction = transactionStub()
        def pendingTransactionId = pendingTransaction.id
        transactionDao.getBy(pendingTransactionId) >> pendingTransaction
        and:
        transactionDao.findPending(limit) >> [transaction, pendingTransaction]

        when:
        executionService.executePending limit

        then:
        1 * transactionDao.lockBy(transactionId)
        1 * transactionDao.lockBy(pendingTransactionId)
    }

    @Test
    def 'should not throw exception and stop execution of pending transactions when could not lock one of transactions'() {
        given:
        def lockedTransaction = transactionStub()
        def lockedTransactionId = lockedTransaction.id
        transactionDao.findPending(limit) >> [lockedTransaction, transaction]
        transactionDao.getBy(lockedTransactionId) >> lockedTransaction
        and:
        transactionDao.lockBy(lockedTransactionId) >> { throw new CouldNotAcquireLockException('Failed') }

        when:
        executionService.executePending limit

        then:
        notThrown CouldNotAcquireLockException
        1 * transactionDao.lockBy(transactionId)
        1 * transactionDao.unlockBy(transactionId)
    }

    @Test
    def 'should not throw exception and stop execution of pending transactions when one of transactions is not found during execution of pending transactions'() {
        given:
        def phantomTransaction = transactionStub()
        def phantomTransactionId = phantomTransaction.id
        transactionDao.findPending(limit) >> [phantomTransaction, transaction]
        and:
        transactionDao.getBy(phantomTransactionId) >> { throw new EntityNotFoundException('Not found') }

        when:
        executionService.executePending limit

        then:
        notThrown EntityNotFoundException
        1 * transactionDao.lockBy(transactionId)
        1 * transactionDao.unlockBy(transactionId)
    }

    @Test
    def 'should not throw exception and stop execution of pending transactions when one of transactions already executed during execution of pending transactions'() {
        given:
        def executedTransaction = transactionStub().executed()
        def executedTransactionId = executedTransaction.id
        transactionDao.findPending(limit) >> [executedTransaction, transaction]
        and:
        transactionDao.getBy(executedTransactionId) >> executedTransaction

        when:
        executionService.executePending limit

        then:
        notThrown IllegalStateException
        1 * transactionDao.lockBy(transactionId)
        1 * transactionDao.unlockBy(transactionId)
    }

    @Test
    def 'should throw exception when try execute transaction by null instead of id'() {
        when:
        executionService.executeBy null

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Transaction identifier cannot be null'
    }

    @Test
    def 'should return false when transactions storage failed to lock transaction by id and throws exception'() {
        given:
        transactionDao.lockBy(transactionId) >> { throw new CouldNotAcquireLockException('Failed') }

        when:
        def result = executionService.executeBy transactionId

        then:
        !result
    }

    @Test
    def 'should lock transaction by id when executes transaction by id'() {
        when:
        executionService.executeBy transactionId

        then:
        1 * transactionDao.lockBy(transactionId)
    }

    @Test
    def 'should return false when transaction is not found in transactions storage by specified id'() {
        given:
        transactionDao.getBy(notExistResourceId) >> { throw new EntityNotFoundException('Not found') }

        when:
        def result = executionService.executeBy notExistResourceId

        then:
        !result
        1 * transactionDao.unlockBy(notExistResourceId)
    }

    @Test
    def 'should return false when transaction with specified id is already executed'() {
        given:
        def executedTransaction = transactionStub().executed()
        def executedTransactionId = executedTransaction.id
        and:
        transactionDao.getBy(executedTransactionId) >> executedTransaction

        when:
        def result = executionService.executeBy executedTransactionId

        then:
        !result
        1 * transactionDao.unlockBy(executedTransactionId)
    }

    @Test
    def 'should return false when accounts storage failed to lock one of the accounts by id and throws exception'() {
        given:
        def accountId = transaction.entries[0].accountId
        and:
        accountDao.lockBy(accountId) >> { throw new CouldNotAcquireLockException('Failed') }

        when:
        def result = executionService.executeBy transactionId

        then:
        !result
        interaction { ensureResourcesUnlockedBy firstAccountId, secondAccountId, transactionId }
    }

    @Test
    def 'should primarily lock account with lower id and then with greater id when executes transaction'() {
        given:
        def transaction = transactionStub referenceId, sourceId, targetId
        def transactionId = transaction.id
        transactionDao.getBy(transactionId) >> transaction

        when:
        executionService.executeBy transactionId

        then:
        1 * accountDao.lockBy(firstLock)
        1 * accountDao.lockBy(secondLock)
        interaction { ensureResourcesUnlockedBy firstAccountId, secondAccountId, transactionId }

        where:
        sourceId        | targetId        || firstLock      | secondLock
        firstAccountId  | secondAccountId || firstAccountId | secondAccountId
        secondAccountId | firstAccountId  || firstAccountId | secondAccountId
    }

    @Test
    def 'should return true and mark transaction as failed when accounts storage does not contains one of the accounts and throws exception'() {
        given:
        def transaction = transactionStub referenceId, notExistResourceId
        def transactionId = transaction.id
        transactionDao.getBy(transactionId) >> transaction
        and:
        accountDao.getBy(notExistResourceId) >> { throw new EntityNotFoundException('Not found') }

        when:
        def result = executionService.executeBy transactionId

        then:
        result
        1 * transactionDao.update({ it.status == FAILED } as Transaction)
        interaction { ensureResourcesUnlockedBy notExistResourceId, secondAccountId, transactionId }
    }

    @Test
    def 'should return true and mark transaction as failed when after updating by transaction entry one of the account balances less than zero'() {
        given:
        def sourceAccount = accountStub(sourceAccountBalance as BigDecimal)
        def sourceAccountId = sourceAccount.id
        def transaction = transactionStub(referenceId, sourceAccountId, secondAccountId, amount as BigDecimal)
        def transactionId = transaction.id
        transactionDao.getBy(transactionId) >> transaction
        and:
        accountDao.getBy(sourceAccountId) >> sourceAccount

        when:
        def result = executionService.executeBy transactionId

        then:
        result
        1 * transactionDao.update({ it.status == FAILED } as Transaction)
        interaction { ensureResourcesUnlockedBy sourceAccountId, secondAccountId, transactionId }

        where:
        sourceAccountBalance | amount
        1                    | 1.0001
        5                    | 50
        10                   | 10.5
    }

    @Test
    def 'should update account in storage after withdrawal from it by transaction entry when transaction executed successfully'() {
        given:
        def sourceAccount = accountStub(sourceAccountBalance as BigDecimal)
        def sourceAccountId = sourceAccount.id
        def transaction = transactionStub(referenceId, sourceAccountId, secondAccountId, amount as BigDecimal)
        def transactionId = transaction.id
        transactionDao.getBy(transactionId) >> transaction
        and:
        accountDao.getBy(sourceAccountId) >> sourceAccount

        when:
        executionService.executeBy transactionId

        then:
        1 * accountDao.update({
            it == sourceAccount
            it.balance == expectedBalance
        } as Account)
        interaction { ensureResourcesUnlockedBy sourceAccountId, secondAccountId, transactionId }

        where:
        sourceAccountBalance | amount || expectedBalance
        1                    | 1      || 0
        10.5                 | 0.5    || 10
        20.995               | 5.395  || 15.6
    }

    @Test
    def 'should update account in storage after deposit to it by transaction entry when transaction executed successfully'() {
        given:
        def targetAccount = accountStub(targetAccountBalance as BigDecimal)
        def targetAccountId = targetAccount.id
        def transaction = transactionStub(referenceId, firstAccountId, targetAccountId, amount as BigDecimal)
        def transactionId = transaction.id
        transactionDao.getBy(transactionId) >> transaction
        and:
        accountDao.getBy(targetAccountId) >> targetAccount

        when:
        executionService.executeBy transactionId

        then:
        1 * accountDao.update({
            it == targetAccount
            it.balance == expectedBalance
        } as Account)
        interaction { ensureResourcesUnlockedBy firstAccountId, targetAccountId, transactionId }

        where:
        targetAccountBalance | amount || expectedBalance
        0                    | 1      || 1
        10.6                 | 0.5    || 11.1
        20.995               | 0.105  || 21.1
    }

    @Test
    def 'should return true and mark transaction as succeed when transaction executed successfully'() {
        when:
        def result = executionService.executeBy transactionId

        then:
        result
        1 * transactionDao.update({ it.status == SUCCESS } as Transaction)
        interaction { ensureResourcesUnlockedBy firstAccountId, secondAccountId, transactionId }
    }

    def ensureResourcesUnlockedBy(UUID sourceAccountId, UUID targetAccountId, UUID transactionId) {
        1 * accountDao.unlockBy(sourceAccountId)
        1 * accountDao.unlockBy(targetAccountId)
        1 * transactionDao.unlockBy(transactionId)
    }
}
