package com.github.example.controller

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.TestSupport
import com.github.example.UnitTest
import com.github.example.dto.response.TransactionData
import com.github.example.exception.EntityNotFoundException
import com.github.example.model.Transaction
import com.github.example.service.TransactionService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import org.junit.Test
import org.junit.experimental.categories.Category
import org.modelmapper.ModelMapper
import spock.lang.Specification

@Category(UnitTest)
class TransactionControllerSpec extends Specification implements TestSupport {

    @Subject
    TransactionController controller

    @Collaborator
    TransactionService transactionService = Mock()
    @Collaborator
    ModelMapper modelMapper = Mock()

    def request = Mock(HttpRequest)
    def transaction = TransactionStub()
    def transactionId = transaction.id

    @Test
    def 'should respond with empty list when service return null or empty collection while getting all transactions'() {
        given:
        transactionService.getAll() >> transactions

        when:
        def result = controller.getAllTransactions()

        then:
        result.empty

        where:
        transactions << [null, []]
    }

    @Test
    def 'should use mapper for conversion when service returns not empty collection while getting all transactions'() {
        given:
        def transactions = [transaction]
        transactionService.getAll() >> transactions

        when:
        controller.getAllTransactions()

        then:
        transactions.size() * modelMapper.map(_ as Transaction, TransactionData)
    }

    @Test
    def 'should throw exception when service not found transaction and throws exception'() {
        given:
        transactionService.getById(transactionId) >> { throw new EntityNotFoundException('Not found') }

        when:
        controller.getTransactionById transactionId

        then:
        thrown EntityNotFoundException
    }

    @Test
    def 'should respond with OK code when transaction for specified id is returned by service'() {
        given:
        transactionService.getById(transactionId) >> transaction

        when:
        def result = controller.getTransactionById transactionId

        then:
        result.status == HttpStatus.OK
    }

    @Test
    def 'should use mapper for conversion when transaction for specified id is returned by service'() {
        given:
        transactionService.getById(transactionId) >> transaction

        when:
        controller.getTransactionById transactionId

        then:
        1 * modelMapper.map(transaction, TransactionData)
    }

    @Test
    def 'should use transaction data as dto when convert responses from internal model'() {
        when:
        def result = controller.getDtoClass()

        then:
        result == TransactionData
    }
}
