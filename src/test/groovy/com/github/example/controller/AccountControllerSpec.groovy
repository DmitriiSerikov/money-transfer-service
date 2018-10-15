package com.github.example.controller

import com.github.example.dto.request.CommandCreateAccount
import com.github.example.dto.response.AccountData
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import org.junit.Test
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

/**
 * Integration test for {@link AccountController}
 */
class AccountControllerSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
    @Shared
    @AutoCleanup
    HttpClient client = embeddedServer.applicationContext.createBean(HttpClient, embeddedServer.getURL())

    @Test
    def "should return empty collection with 200 status code when accounts not exists yet"() {
        given:
        def request = HttpRequest.GET "/accounts"

        when:
        def response = client.toBlocking().exchange request, Collection.class

        then:
        response.status == HttpStatus.OK
        response.body().empty
    }

    @Test
    def "should return error response with 404 status code when account with specified id doesn't exist"() {
        given:
        def request = HttpRequest.GET "/accounts/1"

        when:
        client.toBlocking().exchange request

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.NOT_FOUND
        ex.message == "Account not exists for id:1"
    }

    @Test
    def "should return error with 400 status code when trying to create account with null balance"() {
        when:
        createAccount null

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == "Initial balance cannot be null"
    }

    @Test
    def "should return error with 400 status code when trying to create account with negative balance"() {
        when:
        createAccount(-100)

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == "Account balance should be positive or zero"
    }

    @Test
    def "should return account data with 201 status code and location header when create account by valid command"() {
        given:
        def initialBalance = 500
        def command = new CommandCreateAccount(initialBalance: 500)
        def request = HttpRequest.POST "/accounts", command

        when:
        def response = client.toBlocking().exchange request, AccountData.class

        then:
        response.status == HttpStatus.CREATED
        response.body().balance == initialBalance
        response.header("Location") == "/accounts/" + response.body().id
    }

    @Test
    def "should return account data with 200 status code when account with specified id exist"() {
        given:
        def initialBalance = 100
        def accountId = createAccount initialBalance
        and:
        def request = HttpRequest.GET "/accounts/" + accountId

        when:
        def response = client.toBlocking().exchange request, AccountData.class

        then:
        response.status == HttpStatus.OK
        response.body().id == accountId
        response.body().balance == initialBalance
    }

    @Test
    def "should return collection of accounts data with 200 status code when accounts exists"() {
        given:
        def request = HttpRequest.GET "/accounts"
        and:
        createAccount 100

        when:
        def response = client.toBlocking().exchange request, Collection.class

        then:
        response.status == HttpStatus.OK
        !response.body().empty
    }

    def createAccount(def initialBalance) {
        def command = new CommandCreateAccount(initialBalance: initialBalance)
        def request = HttpRequest.POST "/accounts", command

        def response = client.toBlocking().exchange request, AccountData.class

        response.body().id
    }
}
