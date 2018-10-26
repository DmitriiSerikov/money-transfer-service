package com.github.example.model

import com.github.example.UnitTest
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.Specification

import static com.github.example.model.Transaction.TransactionStatus.*

@Category(UnitTest)
class TransactionSpec extends Specification {

    def referenceId = "external_reference_id"
    def firstAccountId = UUID.fromString "0-0-0-0-1"
    def secondAccountId = UUID.fromString "0-0-0-0-2"

    @Test
    def "should throw exception when trying initialize transaction with null reference id"() {
        when:
        new Transaction(null, firstAccountId, secondAccountId, BigDecimal.ONE)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == "Reference identifier cannot be null"
    }

    @Test
    def "should throw exception when trying initialize transaction with null amount"() {
        when:
        new Transaction(referenceId, firstAccountId, secondAccountId, null)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == "Transaction amount cannot be null"
    }

    @Test
    def "should throw exception when trying initialize transaction with negative amount"() {
        given:
        def negativeAmount = new BigDecimal(-10)

        when:
        new Transaction(referenceId, firstAccountId, secondAccountId, negativeAmount)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == "Transaction amount should be positive"
    }

    @Test
    def "should throw exception when trying initialize transaction with zero amount"() {
        when:
        new Transaction(referenceId, firstAccountId, secondAccountId, BigDecimal.ZERO)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == "Transaction amount should be positive"
    }

    @Test
    def "should throw exception when trying initialize transaction between same account id's"() {
        when:
        new Transaction(referenceId, firstAccountId, firstAccountId, BigDecimal.ONE)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == "Transactions not allowed between same account id's"
    }

    @Test
    def "should initialize transaction properties when reference id, amount and account id's are correct"() {
        given:
        def amount = BigDecimal.ONE

        when:
        def result = new Transaction(referenceId, firstAccountId, secondAccountId, amount)

        then:
        result.id
        !result.reasonCode
        result.amount == amount
        result.status == PENDING
        result.referenceId == referenceId
        result.sourceAccountId == firstAccountId
        result.targetAccountId == secondAccountId
        result.createdAt == result.updatedAt
    }

    @Test
    def "should keep transaction immutable by returning new instance with copied and updated properties when transaction is executed"() {
        given:
        def initialTransaction = new Transaction(referenceId, firstAccountId, secondAccountId, BigDecimal.ONE)

        when:
        def result = initialTransaction.executed()

        then:
        result != initialTransaction
        result.id == initialTransaction.id
        result.amount == initialTransaction.amount
        result.referenceId == initialTransaction.referenceId
        result.sourceAccountId == initialTransaction.sourceAccountId
        result.targetAccountId == initialTransaction.targetAccountId
        result.createdAt == initialTransaction.createdAt
        result.reasonCode == initialTransaction.reasonCode
        result.status == SUCCESS
    }

    @Test
    def "should keep transaction immutable by returning new instance with copied and updated properties when transaction is failed"() {
        given:
        def reasonOfFail = "Some reason"
        def initialTransaction = new Transaction(referenceId, firstAccountId, secondAccountId, BigDecimal.ONE)

        when:
        def result = initialTransaction.failed reasonOfFail

        then:
        result != initialTransaction
        result.id == initialTransaction.id
        result.amount == initialTransaction.amount
        result.referenceId == initialTransaction.referenceId
        result.sourceAccountId == initialTransaction.sourceAccountId
        result.targetAccountId == initialTransaction.targetAccountId
        result.createdAt == initialTransaction.createdAt
        result.reasonCode == reasonOfFail
        result.status == FAILED
    }
}
