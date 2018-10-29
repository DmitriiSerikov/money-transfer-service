package com.github.example.model

import com.github.example.TestSupport
import com.github.example.UnitTest
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.Specification

@Category(UnitTest)
class AccountSpec extends Specification implements TestSupport {

    @Test
    def 'should throw exception when trying to initialize account with null balance'() {
        when:
        new Account(null)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Account balance cannot be null'
    }

    @Test
    def 'should throw exception when trying to initialize account with negative balance'() {
        given:
        def negativeBalance = -10 as BigDecimal

        when:
        new Account(negativeBalance)

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Account balance should be positive or zero'
    }

    @Test
    def 'should throw exception when try to add amount by null instead of transaction entry'() {
        given:
        def account = new Account(0 as BigDecimal)

        when:
        account.addBy null

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Transaction entry cannot be null'
    }

    @Test
    def 'should throw exception when after adding amount by transaction entry balance is negative'() {
        given:
        def account = new Account(0 as BigDecimal)
        def entry = getTransactionEntry(-10 as BigDecimal)

        when:
        account.addBy entry

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Account balance should be positive or zero'
    }

    @Test
    def 'should initialize account properties when initial balance is positive'() {
        given:
        def initialBalance = 0 as BigDecimal

        when:
        def result = new Account(initialBalance)

        then:
        result.id
        result.balance == initialBalance
        result.createdAt == result.updatedAt
    }

    @Test
    def 'should return new instance with copied and updated properties when add amount by transaction entry'() {
        given:
        def initialAccount = new Account(0 as BigDecimal)
        def entry = getTransactionEntry(10 as BigDecimal)

        when:
        def result = initialAccount.addBy entry

        then:
        result != initialAccount
        result.id == initialAccount.id
        result.createdAt == initialAccount.createdAt
        result.balance == 10
    }
}
