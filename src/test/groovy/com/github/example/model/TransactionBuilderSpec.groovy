package com.github.example.model

import com.github.example.TestSupport
import com.github.example.UnitTest
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Category(UnitTest)
class TransactionBuilderSpec extends Specification implements TestSupport {

    @Subject
    TransactionBuilder builder = TransactionBuilder.builder referenceId

    @Test
    def 'should return new instance of transaction builder when factory method is used'() {
        when:
        def result = TransactionBuilder.builder referenceId

        then:
        result != builder
        result instanceof TransactionBuilder
    }

    @Test
    def 'should throw exception when try to build transaction with null withdrawal amount from account'() {
        when:
        builder.withdraw firstAccountId, null

        then:
        def ex = thrown IllegalArgumentException
        ex.message == "Operation amount cannot be null"
    }

    @Test
    def 'should throw exception when try to build transaction with zero or negative withdrawal amount from account'() {
        when:
        builder.withdraw firstAccountId, withdrawAmount as BigDecimal

        then:
        def ex = thrown IllegalArgumentException
        ex.message == "Operation amount should be positive"

        where:
        withdrawAmount << [0, -1, -2.5]
    }

    @Test
    def 'should throw exception when try to build transaction with null deposit amount from account'() {
        when:
        builder.deposit firstAccountId, null

        then:
        def ex = thrown IllegalArgumentException
        ex.message == "Operation amount cannot be null"
    }

    @Test
    def 'should throw exception when try to build transaction with zero or negative deposit amount from account'() {
        when:
        builder.deposit firstAccountId, depositAmount as BigDecimal

        then:
        def ex = thrown IllegalArgumentException
        ex.message == "Operation amount should be positive"

        where:
        depositAmount << [0, -1, -2.5]
    }

    @Test
    def 'should throw exception when try to build unbalanced transaction with only deposit entry'() {
        given:
        builder.deposit firstAccountId, amount

        when:
        builder.build()

        then:
        def ex = thrown IllegalArgumentException
        ex.message == "Summary of transaction entries should be zero"
    }

    @Test
    def 'should throw exception when try to build unbalanced transaction with only withdrawal entry'() {
        given:
        builder.withdraw firstAccountId, amount

        when:
        builder.build()

        then:
        def ex = thrown IllegalArgumentException
        ex.message == "Summary of transaction entries should be zero"
    }

    @Unroll
    def 'should throw exception when try to build unbalanced transaction with deposit amount:#depositAmount and withdraw amount:#withdrawAmount'() {
        given:
        builder.withdraw firstAccountId, withdrawAmount as BigDecimal
        builder.deposit firstAccountId, depositAmount as BigDecimal

        when:
        builder.build()

        then:
        def ex = thrown IllegalArgumentException
        ex.message == "Summary of transaction entries should be zero"

        where:
        depositAmount | withdrawAmount
        1             | 10
        1.5           | 2
        10.112        | 10.111
    }

    @Test
    def 'should successfully build new transaction when transaction entries are balanced'() {
        given:
        builder.withdraw firstAccountId, amount
        builder.deposit firstAccountId, amount

        when:
        def result = builder.build()

        then:
        result.referenceId == referenceId
        result.entries*.amount.contains amount
        result.entries*.amount.contains amount.negate()
    }
}
