package com.github.example.model

import com.github.example.TestSupport
import com.github.example.UnitTest
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.Specification

@Category(UnitTest)
class TransactionEntrySpec extends Specification implements TestSupport {

    @Test
    def 'should throw exception when trying to initialize entry with null account id'() {
        when:
        new TransactionEntry(null, amount)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Entry account cannot be null'
    }

    @Test
    def 'should throw exception when trying to initialize entry with null amount'() {
        when:
        new TransactionEntry(firstAccountId, null)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Entry amount cannot be null'
    }

    @Test
    def 'should throw exception when trying to initialize entry with zero amount'() {
        when:
        new TransactionEntry(firstAccountId, 0 as BigDecimal)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Entry amount should not be zero'
    }

    @Test
    def 'should initialize entry properties when account id specified and amount is not null or zero'() {
        when:
        def result = new TransactionEntry(firstAccountId, amount)

        then:
        result.id
        result.accountId == firstAccountId
        result.amount == amount
    }

    @Test
    def 'should return true when check equality to the reference points to the same instance'() {
        given:
        def entry = transactionEntry

        expect:
        entry.equals entry
    }

    @Test
    def 'should return false when check equality to the object that is not an instance of transaction entry'() {
        when:
        def result = transactionEntry.equals Object

        then:
        !result
    }

    @Test
    def 'should return false when check equality to the null'() {
        when:
        def result = transactionEntry.equals null

        then:
        !result
    }

    @Test
    def 'should return false when check equality to the transaction entry with different id'() {
        given:
        def entry = transactionEntry

        when:
        def result = transactionEntry.equals entry

        then:
        !result
    }
}
