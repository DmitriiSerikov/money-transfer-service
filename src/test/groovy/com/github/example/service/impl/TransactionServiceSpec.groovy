package com.github.example.service.impl

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.TestSupport
import com.github.example.UnitTest
import com.github.example.dao.TransactionDao
import com.github.example.dto.request.CommandPerformTransfer
import com.github.example.exception.CouldNotAcquireLockException
import com.github.example.exception.EntityNotFoundException
import com.github.example.model.Transaction
import com.github.example.service.TransactionExecutionService
import io.micronaut.http.server.exceptions.InternalServerException
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
    def transaction = TransactionStub()
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
    def 'should insert transaction into transactions storage when transaction successfully created by transfer command'() {
        when:
        transactionService.transferBy command

        then:
        1 * transactionDao.insert({
            it.referenceId == referenceId
            it.sourceAccountId == firstAccountId
            it.targetAccountId == secondAccountId
            it.amount == amount
        } as Transaction)
    }

    @Test
    def 'should throw exception when transaction inserted into storage by transfer command is not found in storage during execution'() {
        given:
        executionService.executeBy(_ as UUID) >> { throw new EntityNotFoundException('Not found') }

        when:
        transactionService.transferBy command

        then:
        thrown InternalServerException
    }

    @Test
    def 'should throw exception when failed to lock transaction during synchronous execution and failed to obtain transaction with actual status from storage'() {
        given:
        executionService.executeBy(_ as UUID) >> { throw new CouldNotAcquireLockException('Failed') }
        and:
        transactionDao.getBy(_ as UUID) >> { throw new EntityNotFoundException('Not found') }

        when:
        transactionService.transferBy command

        then:
        thrown InternalServerException
    }

    @Unroll
    def 'should return transaction with status:#expectedStatus when failed to lock transaction during synchronous execution and storage returns transaction with #txFromStorage.status status'() {
        given:
        executionService.executeBy(_ as UUID) >> { throw new CouldNotAcquireLockException('Failed') }
        and:
        transactionDao.getBy(_ as UUID) >> txFromStorage

        when:
        def result = transactionService.transferBy command

        then:
        result.status == expectedStatus

        where:
        txFromStorage          || expectedStatus
        transaction            || PENDING
        transaction.executed() || SUCCESS
        transaction.failed()   || FAILED
    }

    @Test
    def 'should throw exception when transaction already executed during synchronous execution and failed to obtain transaction with actual status from storage'() {
        given:
        executionService.executeBy(_ as UUID) >> { throw new IllegalStateException('Executed') }
        and:
        transactionDao.getBy(_ as UUID) >> { throw new EntityNotFoundException('Not found') }

        when:
        transactionService.transferBy command

        then:
        thrown InternalServerException
    }

    @Unroll
    def 'should return transaction with status:#expectedStatus when transaction already executed during synchronous execution and storage returns transaction with #txFromStorage.status status'() {
        given:
        executionService.executeBy(_ as UUID) >> { throw new IllegalStateException('Executed') }
        and:
        transactionDao.getBy(_ as UUID) >> txFromStorage

        when:
        def result = transactionService.transferBy command

        then:
        result.status == expectedStatus

        where:
        txFromStorage          || expectedStatus
        transaction            || PENDING
        transaction.executed() || SUCCESS
        transaction.failed()   || FAILED
    }

    @Unroll
    def 'should return transaction with status:#expectedStatus when transaction with status #txFromStorage.status returned by service after successful synchronous execution'() {
        given:
        executionService.executeBy(_ as UUID) >> txFromStorage

        when:
        def result = transactionService.transferBy command

        then:
        result.status == expectedStatus

        where:
        txFromStorage          || expectedStatus
        transaction            || PENDING
        transaction.executed() || SUCCESS
        transaction.failed()   || FAILED
    }
}
