package com.github.example.factory

import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import org.junit.Test
import org.modelmapper.ModelMapper
import spock.lang.Specification

/**
 * Unit test for {@link ModelMapperFactory}
 */
class ModelMapperFactorySpec extends Specification {

    @Subject
    ModelMapperFactory factory

    @Test
    def "should produce implementation of model mapper when bean creation is required"() {
        when:
        def result = factory.create()

        then:
        result as ModelMapper
    }
}
