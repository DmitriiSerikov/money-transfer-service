package com.github.example.controller

import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.runtime.server.EmbeddedServer
import org.junit.Test
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class TransactionControllerSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
    @Shared
    @AutoCleanup
    RxHttpClient client = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.getURL())

    @Test
    def "should return ok when get all transactions"() {
        given:
        HttpResponse response = client.toBlocking().exchange("/transactions")

        expect:
        response.status == HttpStatus.OK
    }
}
