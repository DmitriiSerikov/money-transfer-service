package com.github.example.controller

import com.github.example.ITestSupport
import com.github.example.IntegrationTest
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

@Category(IntegrationTest)
class TransferControllerISpec extends Specification implements ITestSupport {

    @Shared
    @AutoCleanup
    def server = ApplicationContext.run EmbeddedServer
    @Shared
    @AutoCleanup
    def client = server.applicationContext.createBean HttpClient, server.getURL()

    def firstAccountId
    def secondAccountId

    def setup() {
        def request = HttpRequest.POST "$API_V1_ROOT/accounts", [initialBalance: 100] as CommandCreateAccount

        firstAccountId = client.toBlocking().exchange request, AccountData body().id
        secondAccountId = client.toBlocking().exchange request, AccountData body().id
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
    def 'should return response with error message and Bad Request code when trying to perform transfer from account that does not exist'() {
        when:
        performTransfer sourceAccountId: NOT_EXIST_RESOURCE_ID

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == "Account not exists for id: $NOT_EXIST_RESOURCE_ID"
    }

    @Test
    def 'should return response with error message and Bad Request code when trying to perform transfer to account that does not exist'() {
        when:
        performTransfer targetAccountId: NOT_EXIST_RESOURCE_ID

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == "Account not exists for id: $NOT_EXIST_RESOURCE_ID"
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
    def "should return response with error message and Bad Request code when trying to perform transfer to the same account"() {
        when:
        performTransfer sourceAccountId: firstAccountId, targetAccountId: firstAccountId

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == 'Transactions to the same account is not allowed'
    }

    @Test
    def 'should return response with transfer execution result and OK code when transfer are successfully initiated by command'() {
        when:
        def response = performTransfer()
        def responseBody = response.body()

        then:
        response.status == HttpStatus.OK
        responseBody.id
        responseBody.createdAt
        responseBody.status
    }

    def performTransfer(Map opts = [:]) {
        def command = [sourceAccountId: opts.sourceAccountId ?: firstAccountId,
                       targetAccountId: opts.targetAccountId ?: secondAccountId,
                       referenceId    : opts.containsKey('referenceId') ? opts.referenceId : referenceId,
                       amount         : opts.containsKey('amount') ? opts.amount : 10] as CommandPerformTransfer
        def request = HttpRequest.POST "$API_V1_ROOT/transfers", command

        client.toBlocking().exchange request, ExecutionResultData
    }
}
