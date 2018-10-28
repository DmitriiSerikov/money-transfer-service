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
import static java.math.BigDecimal.ONE

@Category(UnitTest)
class TransactionExecutionServiceSpec extends Specification implements TestSupport {

    @Subject
    TransactionExecutionServiceImpl executionService

    @Collaborator
    AccountDao accountDao = Mock()
    @Collaborator
    TransactionDao transactionDao = Mock()

    def limit = 10
    def transaction = TransactionStub()
    def transactionId = transaction.id

    def setup() {
        transactionDao.getBy(transactionId) >> transaction
        accountDao.getBy(firstAccountId) >> AccountStub()
        accountDao.getBy(secondAccountId) >> AccountStub()
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
    def 'should execute transactions for the same source account sequentially when transaction storage contains pending transactions only for one source account'() {
        given:
        def transactionWithSameSourceAccount = new Transaction(referenceId, firstAccountId, secondAccountId, ONE)
        transactionDao.getBy(transactionWithSameSourceAccount.id) >> transactionWithSameSourceAccount
        and:
        transactionDao.findPending(limit) >> [transaction, transactionWithSameSourceAccount]

        when:
        executionService.executePending limit

        then:
        1 * transactionDao.lockBy(transactionId)
        1 * transactionDao.lockBy(transactionWithSameSourceAccount.id)
    }

    @Test
    def 'should execute transactions for the different accounts in parallel when transaction storage contains pending transactions between different accounts'() {
        given:
        def transactionWithAnotherSourceAccount = new Transaction(referenceId, secondAccountId, firstAccountId, ONE)
        transactionDao.getBy(transactionWithAnotherSourceAccount.id) >> transactionWithAnotherSourceAccount
        and:
        transactionDao.findPending(limit) >> [transaction, transactionWithAnotherSourceAccount]

        when:
        executionService.executePending limit

        then:
        2 * transactionDao.lockBy(*_)
    }

    @Test
    def 'should not throw exception and stop execution of pending transactions when could not lock one of transactions'() {
        given:
        def lockedTransaction = new Transaction(referenceId, firstAccountId, secondAccountId, ONE)
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
    def 'should not throw exception and stop execution when one of transactions is not found during execution of pending transactions'() {
        given:
        def phantomTransaction = new Transaction(referenceId, firstAccountId, secondAccountId, ONE)
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
    def 'should not throw exception and stop execution when one of transactions already executed during execution of pending transactions'() {
        given:
        def executedTransaction = new Transaction(referenceId, firstAccountId, secondAccountId, ONE).executed()
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
    def 'should throw exception when transactions storage failed to lock transaction by id and throws exception'() {
        given:
        transactionDao.lockBy(transactionId) >> { throw new CouldNotAcquireLockException('Failed') }

        when:
        executionService.executeBy transactionId

        then:
        thrown CouldNotAcquireLockException
    }

    @Test
    def 'should lock transaction by id when executes transaction by id'() {
        when:
        executionService.executeBy transactionId

        then:
        1 * transactionDao.lockBy(transactionId)
        interaction { ensureResourcesUnlockedBy firstAccountId, secondAccountId, transactionId }
    }

    @Test
    def 'should throw exception when transaction is not found in transactions storage by specified id'() {
        given:
        transactionDao.getBy(notExistResourceId) >> { throw new EntityNotFoundException('Not found') }

        when:
        executionService.executeBy notExistResourceId

        then:
        thrown EntityNotFoundException
    }

    @Test
    def 'should throw exception when transaction with specified id is already executed'() {
        given:
        def executedTransaction = new Transaction(referenceId, firstAccountId, secondAccountId, ONE).executed()
        def executedTransactionId = executedTransaction.id
        and:
        transactionDao.getBy(executedTransactionId) >> executedTransaction

        when:
        executionService.executeBy executedTransactionId

        then:
        thrown IllegalStateException
    }

    @Test
    def 'should not throw exception when accounts storage failed to lock source account by id and throws exception'() {
        given:
        def sourceAccountId = transaction.sourceAccountId
        and:
        accountDao.lockBy(sourceAccountId) >> { throw new CouldNotAcquireLockException('Failed') }

        when:
        executionService.executeBy transactionId

        then:
        notThrown CouldNotAcquireLockException
        interaction { ensureResourcesUnlockedBy firstAccountId, secondAccountId, transactionId }
    }

    @Test
    def 'should return transaction in current status when accounts storage failed to lock source account by id and throws exception'() {
        given:
        def sourceAccountId = transaction.sourceAccountId
        and:
        accountDao.lockBy(sourceAccountId) >> { throw new CouldNotAcquireLockException('Failed') }

        when:
        def result = executionService.executeBy transactionId

        then:
        result == transaction
        interaction { ensureResourcesUnlockedBy firstAccountId, secondAccountId, transactionId }
    }

    @Test
    def 'should not throw exception when accounts storage failed to lock target account by id and throws exception'() {
        given:
        def targetAccountId = transaction.targetAccountId
        and:
        accountDao.lockBy(targetAccountId) >> { throw new CouldNotAcquireLockException('Failed') }

        when:
        executionService.executeBy transactionId

        then:
        notThrown CouldNotAcquireLockException
        interaction { ensureResourcesUnlockedBy firstAccountId, secondAccountId, transactionId }
    }

    @Test
    def 'should return transaction in current status when accounts storage failed to lock target account by id and throws exception'() {
        given:
        def targetAccountId = transaction.targetAccountId
        and:
        accountDao.lockBy(targetAccountId) >> { throw new CouldNotAcquireLockException('Failed') }

        when:
        def result = executionService.executeBy transactionId

        then:
        result == transaction
        interaction { ensureResourcesUnlockedBy firstAccountId, secondAccountId, transactionId }
    }

    @Test
    def 'should primarily lock account with lower id and then with greater id when executes transaction'() {
        given:
        def transaction = new Transaction(referenceId, sourceId, targetId, ONE)
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
    def 'should mark transaction as failed when accounts storage does not contains source account for specified id and throws exception'() {
        given:
        def transaction = new Transaction(referenceId, notExistResourceId, secondAccountId, amount)
        def transactionId = transaction.id
        transactionDao.getBy(transactionId) >> transaction
        and:
        accountDao.getBy(notExistResourceId) >> { throw new EntityNotFoundException('Not found') }

        when:
        executionService.executeBy transactionId

        then:
        1 * transactionDao.update({ it.status == FAILED } as Transaction)
        interaction { ensureResourcesUnlockedBy notExistResourceId, secondAccountId, transactionId }
    }

    @Test
    def 'should return transaction after update it in storage when accounts storage does not contains source account for specified id and throws exception'() {
        given:
        def transaction = new Transaction(referenceId, notExistResourceId, secondAccountId, amount)
        def transactionId = transaction.id
        transactionDao.getBy(transactionId) >> transaction
        and:
        accountDao.getBy(notExistResourceId) >> { throw new EntityNotFoundException('Not found') }
        transactionDao.update(_ as Transaction) >> transaction

        when:
        def result = executionService.executeBy transactionId

        then:
        result == transaction
        interaction { ensureResourcesUnlockedBy notExistResourceId, secondAccountId, transactionId }
    }

    @Test
    def 'should mark transaction as failed when accounts storage does not contains target account for specified id and throws exception'() {
        given:
        def transaction = new Transaction(referenceId, firstAccountId, notExistResourceId, amount)
        def transactionId = transaction.id
        transactionDao.getBy(transactionId) >> transaction
        and:
        accountDao.getBy(notExistResourceId) >> { throw new EntityNotFoundException('Not found') }

        when:
        executionService.executeBy transactionId

        then:
        1 * transactionDao.update({ it.status == FAILED } as Transaction)
        interaction { ensureResourcesUnlockedBy firstAccountId, notExistResourceId, transactionId }
    }

    @Test
    def 'should return transaction after update it in storage when accounts storage does not contains target account for specified id and throws exception'() {
        given:
        def transaction = new Transaction(referenceId, firstAccountId, notExistResourceId, amount)
        def transactionId = transaction.id
        transactionDao.getBy(transactionId) >> transaction
        and:
        accountDao.getBy(notExistResourceId) >> { throw new EntityNotFoundException('Not found') }
        transactionDao.update(_ as Transaction) >> transaction

        when:
        def result = executionService.executeBy transactionId

        then:
        result == transaction
        interaction { ensureResourcesUnlockedBy firstAccountId, notExistResourceId, transactionId }
    }

    @Test
    def 'should mark transaction as failed when source account balance after withdrawal less than zero'() {
        given:
        def sourceAccount = new Account(sourceAccountBalance as BigDecimal)
        def sourceAccountId = sourceAccount.id
        def transaction = new Transaction(referenceId, sourceAccountId, secondAccountId, amount as BigDecimal)
        def transactionId = transaction.id
        transactionDao.getBy(transactionId) >> transaction
        and:
        accountDao.getBy(sourceAccountId) >> sourceAccount

        when:
        executionService.executeBy transactionId

        then:
        1 * transactionDao.update({ it.status == FAILED } as Transaction)
        interaction { ensureResourcesUnlockedBy sourceAccountId, secondAccountId, transactionId }

        where:
        sourceAccountBalance | amount
        1                    | 1.0001
        5                    | 50
        10                   | 10.5
    }

    @Test
    def 'should return transaction after update it in storage when source account balance after withdrawal less than zero'() {
        given:
        def sourceAccount = new Account(sourceAccountBalance as BigDecimal)
        def sourceAccountId = sourceAccount.id
        def transaction = new Transaction(referenceId, sourceAccountId, secondAccountId, amount as BigDecimal)
        def transactionId = transaction.id
        transactionDao.getBy(transactionId) >> transaction
        and:
        accountDao.getBy(sourceAccountId) >> sourceAccount
        transactionDao.update(_ as Transaction) >> transaction

        when:
        def result = executionService.executeBy transactionId

        then:
        result == transaction
        interaction { ensureResourcesUnlockedBy sourceAccountId, secondAccountId, transactionId }

        where:
        sourceAccountBalance | amount
        1                    | 1.0001
        5                    | 50
        10                   | 10.5
    }

    @Test
    def 'should withdraw from source account and update it in storage when transaction executed successfully'() {
        given:
        def sourceAccount = new Account(sourceAccountBalance as BigDecimal)
        def sourceAccountId = sourceAccount.id
        def transaction = new Transaction(referenceId, sourceAccountId, secondAccountId, amount as BigDecimal)
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
    def 'should deposit into target account and update it in storage when transaction executed successfully'() {
        given:
        def targetAccount = new Account(targetAccountBalance as BigDecimal)
        def targetAccountId = targetAccount.id
        def transaction = new Transaction(referenceId, firstAccountId, targetAccountId, amount as BigDecimal)
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
    def 'should mark transaction as succeed when transaction executed successfully'() {
        when:
        executionService.executeBy transactionId

        then:
        1 * transactionDao.update({ it.status == SUCCESS } as Transaction)
        interaction { ensureResourcesUnlockedBy firstAccountId, secondAccountId, transactionId }
    }

    @Test
    def 'should return transaction after update it in storage with success status when transaction executed successfully'() {
        given:
        transactionDao.update(_ as Transaction) >> transaction

        when:
        def result = executionService.executeBy transactionId

        then:
        result == transaction
        interaction { ensureResourcesUnlockedBy firstAccountId, secondAccountId, transactionId }
    }

    def ensureResourcesUnlockedBy(UUID sourceAccountId, UUID targetAccountId, UUID transactionId) {
        1 * accountDao.unlockBy(sourceAccountId)
        1 * accountDao.unlockBy(targetAccountId)
        1 * transactionDao.unlockBy(transactionId)
    }
}
