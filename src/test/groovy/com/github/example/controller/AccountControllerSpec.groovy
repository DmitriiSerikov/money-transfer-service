package com.github.example.controller

import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import org.junit.Test
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class AccountControllerSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
    @Shared
    @AutoCleanup
    HttpClient client = embeddedServer.applicationContext.createBean(HttpClient, embeddedServer.getURL())

    @Test
    def "should return ok when get all accounts"() {
        given:
        HttpResponse response = client.toBlocking().exchange("/accounts")

        expect:
        response.status == HttpStatus.OK
    }
}
