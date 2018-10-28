package com.github.example.controller

import com.github.example.ITestSupport
import com.github.example.IntegrationTest
import com.github.example.dao.AccountDao
import com.github.example.dto.request.CommandCreateAccount
import com.github.example.dto.request.CommandPerformTransfer
import com.github.example.dto.response.AccountData
import com.github.example.dto.response.ExecutionResultData
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static com.github.example.model.Transaction.TransactionStatus.*

@Category(IntegrationTest)
class TransferControllerISpec extends Specification implements ITestSupport {

    @Shared
    @AutoCleanup
    def server = ApplicationContext.run EmbeddedServer
    @Shared
    @AutoCleanup
    def client = server.applicationContext.createBean HttpClient, server.getURL()
    @Shared
    def accountDao = server.applicationContext.getBean AccountDao

    def initialBalance = 100
    def firstAccountId
    def secondAccountId

    def setup() {
        def request = HttpRequest.POST "$apiV1Root/accounts", [initialBalance: initialBalance] as CommandCreateAccount

        firstAccountId = client.toBlocking().exchange request, AccountData body().id
        secondAccountId = client.toBlocking().exchange request, AccountData body().id
    }

    @Test
    def 'should return response with Bad Request code when trying to perform transfer with wrong request format'() {
        given:
        def request = HttpRequest.POST "$apiV1Root/transfers", []

        when:
        client.toBlocking().exchange request

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
    }

    @Test
    def 'should return response with error message and Bad Request code when trying to perform transfer with null reference id'() {
        when:
        performTransfer referenceId: null

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == 'Reference identifier should be not blank string'
    }

    @Test
    def 'should return response with error message and Bad Request code when trying to perform transfer with empty reference id'() {
        when:
        performTransfer referenceId: ''

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == 'Reference identifier should be not blank string'
    }

    @Test
    def 'should return response with error message and Conflict code when trying to perform transfer with not unique reference id'() {
        given:
        def referenceId = referenceId
        performTransfer referenceId: referenceId

        when:
        performTransfer referenceId: referenceId

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.CONFLICT
        ex.message == "Transaction already exists for referenceId:$referenceId"
    }

    @Test
    def 'should return response with error message and Bad Request code when trying to perform transfer for null amount'() {
        when:
        performTransfer amount: null

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == 'Transaction amount cannot be null'
    }

    @Test
    def 'should return response with error message and Bad Request code when trying to perform transfer for negative amount'() {
        when:
        performTransfer amount: -10

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == 'Transaction amount should be positive'
    }

    @Test
    def 'should return response with error message and Bad Request code when trying to perform transfer to the same account'() {
        when:
        performTransfer sourceAccountId: firstAccountId, targetAccountId: firstAccountId

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == 'Transactions to the same account is not allowed'
    }

    @Test
    def 'should return response with FAILED execution status and OK code when trying to perform transfer from account that does not exist'() {
        when:
        def response = performTransfer sourceAccountId: notExistResourceId

        then:
        response.status == HttpStatus.OK
        response.body().status == FAILED as String
    }

    @Test
    def 'should return response with FAILED execution status and OK code when trying to perform transfer to account that does not exist'() {
        when:
        def response = performTransfer targetAccountId: notExistResourceId

        then:
        response.status == HttpStatus.OK
        response.body().status == FAILED as String
    }

    @Test
    def 'should return response with FAILED execution status and OK code when source account balance after withdrawal less than zero'() {
        when:
        def response = performTransfer amount: initialBalance * 2

        then:
        response.status == HttpStatus.OK
        response.body().status == FAILED as String
    }

    @Test
    def 'should return response with PENDING execution status and OK code when failed to lock source account during synchronous execution'() {
        given:
        accountDao.lockBy firstAccountId as UUID

        when:
        def response = performTransfer()

        then:
        response.status == HttpStatus.OK
        response.body().status == PENDING as String

        cleanup:
        accountDao.unlockBy firstAccountId as UUID
    }

    @Test
    def 'should return response with PENDING execution status and OK code when failed to lock target account during synchronous execution'() {
        given:
        accountDao.lockBy secondAccountId as UUID

        when:
        def response = performTransfer()

        then:
        response.status == HttpStatus.OK
        response.body().status == PENDING as String

        cleanup:
        accountDao.unlockBy secondAccountId as UUID
    }

    @Test
    def 'should return response with SUCCESS execution status and OK code when transfer are successfully initiated by command'() {
        when:
        def response = performTransfer()

        then:
        response.status == HttpStatus.OK
        response.body().status == SUCCESS as String
    }

    def performTransfer(Map opts = [:]) {
        def command = [sourceAccountId: opts.sourceAccountId ?: firstAccountId,
                       targetAccountId: opts.targetAccountId ?: secondAccountId,
                       referenceId    : opts.containsKey('referenceId') ? opts.referenceId : referenceId,
                       amount         : opts.containsKey('amount') ? opts.amount : 10] as CommandPerformTransfer
        def request = HttpRequest.POST "$apiV1Root/transfers", command

        client.toBlocking().exchange request, ExecutionResultData
    }
}
