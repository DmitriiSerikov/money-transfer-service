package com.github.example.dao.impl

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.exception.EntityAlreadyExistsException
import com.github.example.exception.EntityNotFoundException
import com.github.example.holder.LockHolder
import com.github.example.model.Transaction
import org.junit.Test
import spock.lang.Specification

import static com.github.example.model.Transaction.TransactionStatus.PENDING
import static java.math.BigDecimal.ONE

/**
 * Unit test for {@link InMemoryTransactionDaoImpl}
 */
class InMemoryTransactionDaoSpec extends Specification {

    @Subject
    InMemoryTransactionDaoImpl inMemoryTransactionDao

    @Collaborator
    LockHolder lockHolder = Mock()

    def transaction = new Transaction(10, 20, ONE)

    @Test
    def "should throw exception when transaction for insertion is null"() {
        when:
        inMemoryTransactionDao.insert null

        then:
        thrown IllegalArgumentException
    }

    @Test
    def "should throw exception when storage already contains transaction for this id"() {
        given:
        inMemoryTransactionDao.insert transaction

        when:
        inMemoryTransactionDao.insert transaction

        then:
        thrown EntityAlreadyExistsException
    }

    @Test
    def "should return same transaction when it successfully inserted in storage"() {
        when:
        def result = inMemoryTransactionDao.insert transaction

        then:
        result == transaction
    }

    @Test
    def "should return empty collection when transactions storage is empty"() {
        when:
        def result = inMemoryTransactionDao.findAll()

        then:
        result.empty
    }

    @Test
    def "should return collection of all stored transactions when transactions storage is not empty"() {
        given:
        inMemoryTransactionDao.insert transaction

        when:
        def result = inMemoryTransactionDao.findAll()

        then:
        !result.empty
        result[0] == transaction
    }

    @Test
    def "should return empty collection of pending transactions when transactions storage is empty"() {
        when:
        def result = inMemoryTransactionDao.findPending 5

        then:
        result.empty
    }

    @Test
    def "should return collection of pending transactions when storage contains transactions with different statuses"() {
        given:
        def failedTransaction = new Transaction(10, 20, ONE).failed()
        def executedTransaction = new Transaction(10, 20, ONE).executed()
        and:
        inMemoryTransactionDao.insert failedTransaction
        inMemoryTransactionDao.insert executedTransaction
        inMemoryTransactionDao.insert transaction

        when:
        def result = inMemoryTransactionDao.findPending 3

        then:
        !result.empty
        result[0].status == PENDING
    }

    @Test
    def "should return limited collection of pending transactions when storage has more then specified limit of pending transactions"() {
        given:
        def anotherPendingTransaction = new Transaction(10, 20, ONE)
        and:
        inMemoryTransactionDao.insert anotherPendingTransaction
        inMemoryTransactionDao.insert transaction

        when:
        def result = inMemoryTransactionDao.findPending 1

        then:
        result.size() == 1
    }

    @Test
    def "should throw exception when storage doesn't contains transaction for specified id"() {
        when:
        inMemoryTransactionDao.getBy 10

        then:
        thrown EntityNotFoundException
    }

    @Test
    def "should return stored transaction when storage contains transaction for specified id"() {
        given:
        inMemoryTransactionDao.insert transaction

        when:
        def result = inMemoryTransactionDao.getBy transaction.id

        then:
        result == transaction
    }

    @Test
    def "should throw exception when transaction for update is null"() {
        when:
        inMemoryTransactionDao.update null

        then:
        thrown IllegalArgumentException
    }

    @Test
    def "should acquire lock for transaction id using holder before updating transaction"() {
        when:
        inMemoryTransactionDao.update transaction

        then:
        1 * lockHolder.acquire({ it.contains(transaction.id as String) } as String)
    }

    @Test
    def "should store transaction when storage doesn't contains transaction with specified id"() {
        when:
        inMemoryTransactionDao.update transaction

        then:
        inMemoryTransactionDao.getBy(transaction.id) == transaction
    }

    @Test
    def "should update transaction when storage already contains transaction with specified id"() {
        given:
        def updatedTransaction = transaction.executed()
        inMemoryTransactionDao.insert transaction

        when:
        inMemoryTransactionDao.update updatedTransaction

        then:
        inMemoryTransactionDao.getBy(transaction.id) == updatedTransaction
    }

    @Test
    def "should release lock for transaction id using holder when finishes update of transaction"() {
        when:
        inMemoryTransactionDao.update transaction

        then:
        1 * lockHolder.release({ it.contains(transaction.id as String) } as String)
    }

    @Test
    def "should acquire lock using holder when try to lock transaction for specified id"() {
        when:
        inMemoryTransactionDao.lockBy 10

        then:
        1 * lockHolder.acquire("Transaction_10")
    }

    @Test
    def "should release lock using holder when try to unlock transaction for specified id"() {
        when:
        inMemoryTransactionDao.unlockBy 10

        then:
        1 * lockHolder.release("Transaction_10")
    }
}
