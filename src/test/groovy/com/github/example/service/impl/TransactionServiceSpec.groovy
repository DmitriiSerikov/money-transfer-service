package com.github.example.service.impl

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.TestSupport
import com.github.example.UnitTest
import com.github.example.dao.TransactionDao
import com.github.example.dto.request.CommandPerformTransfer
import com.github.example.exception.EntityNotFoundException
import com.github.example.model.Transaction
import com.github.example.service.TransactionExecutionService
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.example.model.Transaction.TransactionStatus.*

@Category(UnitTest)
class TransactionServiceSpec extends Specification implements TestSupport {

    @Subject
    TransactionServiceImpl transactionService

    @Collaborator
    TransactionDao transactionDao = Mock()
    @Collaborator
    TransactionExecutionService executionService = Mock()

    @Shared
    def transaction = transactionStub()
    @Shared
    def transactionId = transaction.id

    def command = [sourceAccountId: firstAccountId, referenceId: referenceId,
                   targetAccountId: secondAccountId, amount: amount] as CommandPerformTransfer

    @Test
    def 'should return empty collection of transactions when transactions storage returns empty collection'() {
        given:
        transactionDao.findAll() >> []

        when:
        def result = transactionService.getAll()

        then:
        result.empty
    }

    @Test
    def 'should return not empty collection of transactions when transactions storage returns not empty collection'() {
        given:
        transactionDao.findAll() >> [transaction]

        when:
        def result = transactionService.getAll()

        then:
        !result.empty
    }

    @Test
    def 'should throw exception when transactions storage does not contains entity for given id and throws exception'() {
        given:
        transactionDao.getBy(notExistResourceId) >> { throw new EntityNotFoundException('Not found') }

        when:
        transactionService.getById notExistResourceId

        then:
        thrown EntityNotFoundException
    }

    @Test
    def 'should return transaction by given id when transactions storage contains entity for given id'() {
        given:
        transactionDao.getBy(transactionId) >> transaction

        when:
        def result = transactionService.getById transactionId

        then:
        result == transaction
    }

    @Test
    def 'should throw exception when command to perform transfer is null'() {
        when:
        transactionService.transferBy null

        then:
        thrown IllegalArgumentException
    }

    @Test
    def 'should throw exception when source and target accounts in the command are the same'() {
        given:
        def command = [sourceAccountId: firstAccountId, referenceId: referenceId,
                       targetAccountId: firstAccountId, amount: amount] as CommandPerformTransfer

        when:
        transactionService.transferBy command

        then:
        def ex = thrown IllegalArgumentException
        ex.message == 'Money transfer to the same account is not allowed'
    }

    @Test
    def 'should insert transaction into transactions storage when transaction successfully created by command'() {
        when:
        transactionService.transferBy command

        then:
        1 * transactionDao.insert({ it.referenceId == referenceId } as Transaction)
    }

    @Unroll
    def 'should return transaction created by command with PENDING status when transaction is not executed synchronously'() {
        given:
        executionService.executeBy(_ as UUID) >> false

        when:
        def result = transactionService.transferBy command

        then:
        result.status == PENDING
        result.referenceId == command.referenceId
    }

    @Unroll
    def 'should return transaction after execution with #expectedStatus status when transaction successfully executed synchronously'() {
        given:
        executionService.executeBy(_ as UUID) >> true
        and:
        transactionDao.getBy(_ as UUID) >> txFromStorage

        when:
        def result = transactionService.transferBy command

        then:
        result.status == expectedStatus
        result.referenceId == command.referenceId

        where:
        txFromStorage          || expectedStatus
        transaction            || PENDING
        transaction.executed() || SUCCESS
        transaction.failed()   || FAILED
    }
}
