package com.github.example.controller

import com.github.example.ITestSupport
import com.github.example.IntegrationTest
import com.github.example.dto.request.CommandCreateAccount
import com.github.example.dto.request.CommandPerformTransfer
import com.github.example.dto.response.AccountData
import com.github.example.dto.response.ExecutionResultData
import com.github.example.dto.response.TransactionData
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
class TransactionControllerISpec extends Specification implements ITestSupport {

    @Shared
    @AutoCleanup
    def server = ApplicationContext.run EmbeddedServer
    @Shared
    @AutoCleanup
    def client = server.applicationContext.createBean HttpClient, server.getURL()

    def firstAccountId
    def secondAccountId

    def setup() {
        def request = HttpRequest.POST "$apiV1Root/accounts", [initialBalance: 100] as CommandCreateAccount

        firstAccountId = client.toBlocking().exchange(request, AccountData).body().id
        secondAccountId = client.toBlocking().exchange(request, AccountData).body().id
    }

    @Test
    def 'should return response with empty collection and OK code when transactions are not exists yet'() {
        given:
        def request = HttpRequest.GET "$apiV1Root/transactions"

        when:
        def response = client.toBlocking().exchange request, Collection

        then:
        response.status == HttpStatus.OK
        response.body().empty
    }

    @Test
    def 'should return response with error message and Not Found code when transaction with specified id does not exist'() {
        given:
        def request = HttpRequest.GET "$apiV1Root/transactions/$notExistResourceId"

        when:
        client.toBlocking().exchange request

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.NOT_FOUND
        ex.message == "Transaction not exists for id: $notExistResourceId"
    }

    @Test
    def 'should return response with error message and Bad Request code when trying to get transaction with incorrect id format'() {
        given:
        def request = HttpRequest.GET "$apiV1Root/transactions/$wrongFormatResourceId"

        when:
        client.toBlocking().exchange request

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message.contains "Invalid UUID string: $wrongFormatResourceId"
    }

    @Test
    def 'should return response with transaction data and OK code when transaction with specified id exist'() {
        given:
        def amount = 100 as BigDecimal
        def transactionId = createTransaction(firstAccountId, secondAccountId, amount).body().id
        and:
        def request = HttpRequest.GET "$apiV1Root/transactions/$transactionId"

        when:
        def response = client.toBlocking().exchange request, TransactionData
        def responseBody = response.body()

        then:
        response.status == HttpStatus.OK
        responseBody.id == transactionId
        responseBody.entries*.accountId.contains firstAccountId
        responseBody.entries*.accountId.contains secondAccountId
        responseBody.entries*.amount.contains amount
        responseBody.entries*.amount.contains amount.negate()
        responseBody.entries*.amount.sum() == 0 as BigDecimal
    }

    @Test
    def 'should return response with not empty collection and OK code when transactions exists'() {
        given:
        def request = HttpRequest.GET "$apiV1Root/transactions"
        and:
        createTransaction()

        when:
        def response = client.toBlocking().exchange request, Collection

        then:
        response.status == HttpStatus.OK
        !response.body().empty
    }

    def createTransaction(sourceAccountId = firstAccountId, targetAccountId = secondAccountId, amount = 10) {
        def command = [sourceAccountId: sourceAccountId, referenceId: referenceId,
                       targetAccountId: targetAccountId, amount: amount] as CommandPerformTransfer
        def request = HttpRequest.POST "$apiV1Root/transfers", command

        client.toBlocking().exchange request, ExecutionResultData
    }
}
