package com.github.example.controller

import com.github.example.IntegrationTest
import com.github.example.dto.request.CommandCreateAccount
import com.github.example.dto.request.CommandCreateTransaction
import com.github.example.dto.response.AccountData
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
class TransactionControllerSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
    @Shared
    @AutoCleanup
    HttpClient client = embeddedServer.applicationContext.createBean(HttpClient, embeddedServer.getURL())

    def notExistAccountId = UUID.fromString "550e8400-e29b-41d4-a716-446655440000"
    def sourceAccountId
    def targetAccountId

    def setup() {
        sourceAccountId = createAccount 100
        targetAccountId = createAccount 0
    }

    @Test
    def "should return empty collection with 200 status code when transactions not exists yet"() {
        given:
        def request = HttpRequest.GET "/transactions"

        when:
        def response = client.toBlocking().exchange request, Collection.class

        then:
        response.status == HttpStatus.OK
        response.body().empty
    }

    @Test
    def "should return error response with 404 status code when transaction with specified id doesn't exist"() {
        given:
        def request = HttpRequest.GET "/transactions/550e8400-e29b-41d4-a716-446655440000"

        when:
        client.toBlocking().exchange request

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.NOT_FOUND
        ex.message == "Transaction not exists for id:550e8400-e29b-41d4-a716-446655440000"
    }

    @Test
    def "should return error response with 400 status code when trying to get transaction with incorrect id format"() {
        given:
        def request = HttpRequest.GET "/transactions/1"

        when:
        client.toBlocking().exchange request

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == "Failed to convert argument [transactionId] for value [1] due to: Invalid UUID string: 1"
    }

    @Test
    def "should return error with 400 status code when trying to create transaction for source account that not exists"() {
        when:
        createTransaction notExistAccountId, targetAccountId, 10

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == "Account not exists for id:550e8400-e29b-41d4-a716-446655440000"
    }

    @Test
    def "should return error with 400 status code when trying to create transaction for target account that not exists"() {
        when:
        createTransaction sourceAccountId, notExistAccountId, 10

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == "Account not exists for id:550e8400-e29b-41d4-a716-446655440000"
    }

    @Test
    def "should return error with 400 status code when trying to create transaction with null amount"() {
        when:
        createTransaction sourceAccountId, targetAccountId, null

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == "Transaction amount cannot be null"
    }

    @Test
    def "should return error with 400 status code when trying to create transaction with negative amount"() {
        when:
        createTransaction sourceAccountId, targetAccountId, -10

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == "Transaction amount should be positive"
    }

    @Test
    def "should return error with 400 status code when trying to create transaction between same accounts"() {
        when:
        createTransaction sourceAccountId, sourceAccountId, 10

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == "Transactions not allowed between same account id's"
    }

    @Test
    def "should return 202 status code and location header when create transaction by valid command"() {
        given:
        def command = new CommandCreateTransaction(sourceAccountId: sourceAccountId, targetAccountId: targetAccountId, amount: 10)
        def request = HttpRequest.POST "/transactions", command

        when:
        def response = client.toBlocking().exchange request, TransactionData.class

        then:
        response.status == HttpStatus.ACCEPTED
        response.header("Location").contains "/transactions/"
    }

    @Test
    def "should return transaction data with 200 status code when transaction with specified id exist"() {
        given:
        def amount = 100
        def transactionId = createTransaction sourceAccountId, targetAccountId, amount
        and:
        def request = HttpRequest.GET "/transactions/" + transactionId

        when:
        def response = client.toBlocking().exchange request, TransactionData.class

        then:
        response.status == HttpStatus.OK
        response.body().id == transactionId
        response.body().sourceAccountId == sourceAccountId
        response.body().targetAccountId == targetAccountId
        response.body().amount == amount
    }

    @Test
    def "should return collection of transactions data with 200 status code when transactions exists"() {
        given:
        def request = HttpRequest.GET "/transactions"
        and:
        createTransaction sourceAccountId, targetAccountId, 10

        when:
        def response = client.toBlocking().exchange request, Collection.class

        then:
        response.status == HttpStatus.OK
        !response.body().empty
    }

    def createTransaction(def sourceAccountId, def targetAccountId, def amount) {
        def command = new CommandCreateTransaction(sourceAccountId: sourceAccountId, targetAccountId: targetAccountId, amount: amount)
        def request = HttpRequest.POST "/transactions", command

        def response = client.toBlocking().exchange request, TransactionData.class

        UUID.fromString response.header("Location") - "/transactions/"
    }

    def createAccount(def initialBalance) {
        def command = new CommandCreateAccount(initialBalance: initialBalance)
        def request = HttpRequest.POST "/accounts", command

        def response = client.toBlocking().exchange request, AccountData.class

        response.body().id
    }
}
