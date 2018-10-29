package com.github.example.model

import com.github.example.TestSupport
import com.github.example.UnitTest
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.Specification

import static com.github.example.model.Transaction.TransactionStatus.*

@Category(UnitTest)
class TransactionSpec extends Specification implements TestSupport {

    def entries = transactionStub().entries

    @Test
    def 'should throw exception when trying to initialize transaction with null or blank reference id'() {
        when:
        new Transaction(refId, entries)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Reference identifier should be not blank string'

        where:
        refId << [null, '', ' ']
    }

    @Test
    def 'should throw exception when trying to initialize transaction with null or empty entries set'() {
        when:
        new Transaction(referenceId, transactionEntries)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Transactions entries should not be empty'

        where:
        transactionEntries << [null, [] as Set]
    }

    @Test
    def 'should throw exception when trying to initialize transaction with only one entry'() {
        given:
        def entries = [transactionEntry] as Set

        when:
        new Transaction(referenceId, entries)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Transactions should have at least two entries'
    }

    @Test
    def 'should initialize transaction properties when reference id is not blank and two or more entries present'() {
        given:
        def entries = transactionStub().entries

        when:
        def result = new Transaction(referenceId, entries)

        then:
        result.id
        !result.reasonCode
        !result.completedAt
        result.referenceId == referenceId
        result.status == PENDING
        result.entries == entries
        result.createdAt == result.updatedAt
    }

    @Test
    def 'should return new instance with copied and updated properties when transaction is executed'() {
        given:
        def initialTransaction = transactionStub()

        when:
        def result = initialTransaction.executed()

        then:
        result != initialTransaction
        result.id == initialTransaction.id
        result.referenceId == initialTransaction.referenceId
        result.entries == initialTransaction.entries
        result.createdAt == initialTransaction.createdAt
        result.updatedAt == result.completedAt
        result.reasonCode == initialTransaction.reasonCode
        result.status == SUCCESS
    }

    @Test
    def 'should return new instance with copied and updated properties when transaction is failed'() {
        given:
        def reasonOfFail = 'Some reason'
        def initialTransaction = transactionStub()

        when:
        def result = initialTransaction.failed reasonOfFail

        then:
        result != initialTransaction
        result.id == initialTransaction.id
        result.referenceId == initialTransaction.referenceId
        result.entries == initialTransaction.entries
        result.createdAt == initialTransaction.createdAt
        result.updatedAt == result.completedAt
        result.reasonCode == reasonOfFail
        result.status == FAILED
    }
}
