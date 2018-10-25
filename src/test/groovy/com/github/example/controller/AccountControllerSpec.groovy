package com.github.example.controller

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.UnitTest
import com.github.example.dto.request.CommandCreateAccount
import com.github.example.dto.response.AccountData
import com.github.example.exception.EntityNotFoundException
import com.github.example.model.Account
import com.github.example.service.AccountService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import org.junit.Test
import org.junit.experimental.categories.Category
import org.modelmapper.ModelMapper
import spock.lang.Specification

import static java.math.BigDecimal.ONE

@Category(UnitTest)
class AccountControllerSpec extends Specification {

    @Subject
    AccountController controller

    @Collaborator
    AccountService accountService = Mock()
    @Collaborator
    ModelMapper modelMapper = Mock()

    def request = Mock(HttpRequest)
    def command = new CommandCreateAccount()
    def account = new Account(ONE)
    def accountId = account.id

    @Test
    def "should respond with empty list when service return null or empty collection while getting all accounts"() {
        given:
        accountService.getAll() >> accounts

        when:
        def result = controller.getAllAccounts()

        then:
        result.empty

        where:
        accounts << [null, []]
    }

    @Test
    def "should use mapper for conversion when service returns not empty collection while getting all accounts"() {
        given:
        def accounts = [account]
        accountService.getAll() >> accounts

        when:
        controller.getAllAccounts()

        then:
        accounts.size() * modelMapper.map(_ as Account, AccountData)
    }

    @Test
    def "should throw exception when service not found account and throws exception"() {
        given:
        accountService.getById(accountId) >> { throw new EntityNotFoundException("Not found") }

        when:
        controller.getAccountById accountId

        then:
        thrown EntityNotFoundException
    }

    @Test
    def "should respond with 'ok' status code when account for specified id is returned by service"() {
        given:
        accountService.getById(accountId) >> account

        when:
        def result = controller.getAccountById accountId

        then:
        result.status == HttpStatus.OK
    }

    @Test
    def "should use mapper for conversion when account for specified id is returned by service"() {
        given:
        accountService.getById(accountId) >> account

        when:
        controller.getAccountById accountId

        then:
        1 * modelMapper.map(account, AccountData)
    }

    @Test
    def "should throw exception when service can't create account and throws exception"() {
        given:
        accountService.createBy(command) >> { throw new IllegalArgumentException() }

        when:
        controller.createAccount command, request

        then:
        thrown IllegalArgumentException
    }

    @Test
    def "should respond with 'created' status code when service successfully created account by command"() {
        given:
        accountService.createBy(command) >> account

        when:
        def result = controller.createAccount command, request

        then:
        result.status == HttpStatus.CREATED
    }

    @Test
    def "should respond with resource location header when service successfully created account by command"() {
        given:
        accountService.createBy(command) >> account
        and:
        request.getPath() >> "/resource"

        when:
        def result = controller.createAccount command, request

        then:
        result.header("Location") == "/resource/" + accountId
    }

    @Test
    def "should use mapper for conversion when service successfully created and returned account by command"() {
        given:
        accountService.createBy(command) >> account

        when:
        controller.createAccount command, request

        then:
        1 * modelMapper.map(account, AccountData)
    }

    @Test
    def "should use account data as dto when convert responses from internal model"() {
        when:
        def result = controller.getDtoClass()

        then:
        result == AccountData
    }
}
