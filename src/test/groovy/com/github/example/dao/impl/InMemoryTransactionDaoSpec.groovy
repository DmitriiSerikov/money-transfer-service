package com.github.example.dao.impl

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.TestSupport
import com.github.example.UnitTest
import com.github.example.exception.EntityAlreadyExistsException
import com.github.example.exception.EntityNotFoundException
import com.github.example.holder.LockHolder
import com.github.example.model.Transaction
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.Specification

import static com.github.example.model.Transaction.TransactionStatus.PENDING
import static java.math.BigDecimal.ONE

@Category(UnitTest)
class InMemoryTransactionDaoSpec extends Specification implements TestSupport {

    @Subject
    InMemoryTransactionDaoImpl inMemoryTransactionDao

    @Collaborator
    LockHolder lockHolder = Mock()

    def transaction = TransactionStub()

    @Test
    def 'should throw exception when transaction for insertion is null'() {
        when:
        inMemoryTransactionDao.insert null

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Transaction cannot be null'
    }

    @Test
    def 'should throw exception when storage already contains transaction with same reference id'() {
        given:
        inMemoryTransactionDao.insert transaction

        when:
        inMemoryTransactionDao.insert transaction

        then:
        def ex = thrown EntityAlreadyExistsException
        ex.message == 'Transaction already exists for referenceId:' + referenceId
    }

    @Test
    def 'should return same transaction when it successfully inserted in storage'() {
        when:
        def result = inMemoryTransactionDao.insert transaction

        then:
        result == transaction
    }

    @Test
    def 'should return empty collection when transactions storage is empty'() {
        when:
        def result = inMemoryTransactionDao.findAll()

        then:
        result.empty
    }

    @Test
    def 'should return collection of all stored transactions when transactions storage is not empty'() {
        given:
        inMemoryTransactionDao.insert transaction

        when:
        def result = inMemoryTransactionDao.findAll()

        then:
        !result.empty
    }

    @Test
    def 'should return collection of all stored transactions sorted by creation date-time when storage contains more than one transaction'() {
        given:
        def createdLaterTransaction = new Transaction('ref_1', firstAccountId, secondAccountId, ONE)
        and:
        inMemoryTransactionDao.insert transaction
        inMemoryTransactionDao.insert createdLaterTransaction

        when:
        def result = inMemoryTransactionDao.findAll()

        then:
        result[0].createdAt < result[1].createdAt
    }

    @Test
    def 'should return empty collection of pending transactions when transactions storage is empty'() {
        when:
        def result = inMemoryTransactionDao.findPending 5

        then:
        result.empty
    }

    @Test
    def 'should return collection of pending transactions when storage contains transactions with different statuses'() {
        given:
        def failedTransaction = new Transaction('ref_1', firstAccountId, secondAccountId, ONE).failed('Failed')
        def executedTransaction = new Transaction('ref_2', firstAccountId, secondAccountId, ONE).executed()
        and:
        inMemoryTransactionDao.insert failedTransaction
        inMemoryTransactionDao.insert executedTransaction
        inMemoryTransactionDao.insert transaction

        when:
        def result = inMemoryTransactionDao.findPending 3

        then:
        result.size() == 1
        result[0].status == PENDING
    }

    @Test
    def 'should return collection of pending transactions sorted by creation date-time when storage contains more than one pending transaction'() {
        given:
        def createdLaterPendingTransaction = new Transaction('ref_1', firstAccountId, secondAccountId, ONE)
        and:
        inMemoryTransactionDao.insert transaction
        inMemoryTransactionDao.insert createdLaterPendingTransaction

        when:
        def result = inMemoryTransactionDao.findPending 3

        then:
        result[0].createdAt < result[1].createdAt
    }

    @Test
    def 'should return limited collection of pending transactions when storage contains more pending transactions then specified by limit'() {
        given:
        def anotherPendingTransaction = new Transaction('ref_1', firstAccountId, secondAccountId, ONE)
        and:
        inMemoryTransactionDao.insert anotherPendingTransaction
        inMemoryTransactionDao.insert transaction

        when:
        def result = inMemoryTransactionDao.findPending 1

        then:
        result.size() == 1
    }

    @Test
    def 'should throw exception when try get transaction by null instead of id'() {
        when:
        inMemoryTransactionDao.getBy null

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Transaction identifier cannot be null'
    }

    @Test
    def 'should throw exception when storage does not contains transaction for specified id'() {
        when:
        inMemoryTransactionDao.getBy notExistResourceId

        then:
        def ex = thrown EntityNotFoundException
        ex.message == 'Transaction not exists for id: ' + notExistResourceId
    }

    @Test
    def 'should return stored transaction when storage contains transaction for specified id'() {
        given:
        inMemoryTransactionDao.insert transaction

        when:
        def result = inMemoryTransactionDao.getBy transaction.id

        then:
        result == transaction
    }

    @Test
    def 'should throw exception when transaction for update is null'() {
        when:
        inMemoryTransactionDao.update null

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Transaction cannot be null'
    }

    @Test
    def 'should acquire lock for transaction id using holder before updating transaction'() {
        given:
        inMemoryTransactionDao.insert transaction

        when:
        inMemoryTransactionDao.update transaction

        then:
        1 * lockHolder.acquire({ it.contains(transaction.id as String) } as String)
    }

    @Test
    def 'should throw exception when try to update transaction that does not exist in storage'() {
        when:
        inMemoryTransactionDao.update transaction

        then:
        def ex = thrown EntityNotFoundException
        ex.message == 'Transaction not exists for id: ' + transaction.id
    }

    @Test
    def 'should update transaction when transaction for update already exists in storage'() {
        given:
        def updatedTransaction = transaction.executed()
        and:
        inMemoryTransactionDao.insert transaction

        when:
        inMemoryTransactionDao.update updatedTransaction

        then:
        inMemoryTransactionDao.getBy(transaction.id) == updatedTransaction
    }

    @Test
    def 'should release lock for transaction id using holder when finished update of transaction'() {
        given:
        inMemoryTransactionDao.insert transaction

        when:
        inMemoryTransactionDao.update transaction

        then:
        1 * lockHolder.release({ it.contains(transaction.id as String) } as String)
    }

    @Test
    def 'should throw exception when try lock account by null instead of id'() {
        when:
        inMemoryTransactionDao.lockBy null

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Transaction identifier cannot be null'
    }

    @Test
    def 'should acquire lock using holder when try to lock transaction for specified id'() {
        when:
        inMemoryTransactionDao.lockBy notExistResourceId

        then:
        1 * lockHolder.acquire({ it.contains(notExistResourceId as String) } as String)
    }

    @Test
    def 'should throw exception when try unlock account by null instead of id'() {
        when:
        inMemoryTransactionDao.unlockBy null

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Transaction identifier cannot be null'
    }

    @Test
    def 'should release lock using holder when try to unlock transaction for specified id'() {
        when:
        inMemoryTransactionDao.unlockBy notExistResourceId

        then:
        1 * lockHolder.release({ it.contains(notExistResourceId as String) } as String)
    }
}
