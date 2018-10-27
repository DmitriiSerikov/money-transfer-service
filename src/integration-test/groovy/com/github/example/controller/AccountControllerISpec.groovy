package com.github.example.controller

import com.github.example.ITestSupport
import com.github.example.IntegrationTest
import com.github.example.dto.request.CommandCreateAccount
import com.github.example.dto.response.AccountData
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
class AccountControllerISpec extends Specification implements ITestSupport {

    @Shared
    @AutoCleanup
    def server = ApplicationContext.run EmbeddedServer
    @Shared
    @AutoCleanup
    def client = server.applicationContext.createBean HttpClient, server.getURL()

    @Test
    def 'should return response with empty collection and OK code when accounts are not exists yet'() {
        given:
        def request = HttpRequest.GET "$API_V1_ROOT/accounts"

        when:
        def response = client.toBlocking().exchange request, Collection

        then:
        response.status == HttpStatus.OK
        response.body().empty
    }

    @Test
    def 'should return response with error message and Not Found code when account with specified id does not exist'() {
        given:
        def request = HttpRequest.GET "$API_V1_ROOT/accounts/$NOT_EXIST_RESOURCE_ID"

        when:
        client.toBlocking().exchange request

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.NOT_FOUND
        ex.message == "Account not exists for id: $NOT_EXIST_RESOURCE_ID"
    }

    @Test
    def 'should return response with error message and Bad Request code when trying to get account with incorrect id format'() {
        given:
        def request = HttpRequest.GET "$API_V1_ROOT/accounts/$WRONG_FORMAT_RESOURCE_ID"

        when:
        client.toBlocking().exchange request

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message.contains "Invalid UUID string: $WRONG_FORMAT_RESOURCE_ID"
    }

    @Test
    def 'should return response with error message and Bad Request code when trying to create account with null balance'() {
        given:
        def initialBalance = null

        when:
        createAccount initialBalance

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == 'Initial balance cannot be null'
    }

    @Test
    def 'should return response with error message and Bad Request code when trying to create account with negative balance'() {
        given:
        def initialBalance = -100

        when:
        createAccount initialBalance

        then:
        def ex = thrown HttpClientResponseException
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == 'Account balance should be positive or zero'
    }

    @Test
    def 'should return response with account data, Created code and location of resource when account successfully created by command'() {
        given:
        def initialBalance = 500

        when:
        def response = createAccount initialBalance
        def responseBody = response.body()

        then:
        response.status == HttpStatus.CREATED
        response.header('Location') == "$API_V1_ROOT/accounts/$responseBody.id"
        responseBody.balance == initialBalance
    }

    @Test
    def 'should return response with account data and OK code when account with specified id exist'() {
        given:
        def initialBalance = 100
        def created = createAccount initialBalance
        def location = created.header 'Location'
        def accountId = created.body().id
        and:
        def request = HttpRequest.GET location

        when:
        def response = client.toBlocking().exchange request, AccountData
        def responseBody = response.body()

        then:
        response.status == HttpStatus.OK
        responseBody.id == accountId
        responseBody.balance == initialBalance
        responseBody.createdAt == responseBody.updatedAt
    }

    @Test
    def 'should return response with not empty collection and OK code when accounts exists'() {
        given:
        def request = HttpRequest.GET "$API_V1_ROOT/accounts"
        and:
        createAccount 100

        when:
        def response = client.toBlocking().exchange request, Collection

        then:
        response.status == HttpStatus.OK
        !response.body().empty
    }

    def createAccount(initialBalance) {
        def request = HttpRequest.POST "$API_V1_ROOT/accounts", [initialBalance: initialBalance] as CommandCreateAccount

        client.toBlocking().exchange request, AccountData
    }
}
