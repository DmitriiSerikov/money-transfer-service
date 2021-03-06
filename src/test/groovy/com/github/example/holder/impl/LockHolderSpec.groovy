package com.github.example.holder.impl

import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.UnitTest
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.Specification

@Category(UnitTest)
class LockHolderSpec extends Specification {

    @Subject
    LockHolderImpl lockHolder

    def lockId = 'some_lock_id'

    @Test
    def 'should throw exception when try acquire lock by null instead of id'() {
        when:
        lockHolder.acquire null

        then:
        thrown IllegalArgumentException
    }

    @Test
    def 'should not throw exception when acquire lock that does not acquired before by any thread'() {
        when:
        lockHolder.acquire lockId

        then:
        notThrown Exception
    }

    @Test
    def 'should not throw exception when try acquire already acquired lock second time'() {
        given:
        lockHolder.acquire lockId

        when:
        lockHolder.acquire lockId

        then:
        notThrown Exception
    }

    @Test
    def 'should throw exception when try release lock by null instead of id'() {
        when:
        lockHolder.release null

        then:
        thrown IllegalArgumentException
    }

    @Test
    def 'should not throw exception when try release lock that does not exist'() {
        when:
        lockHolder.release lockId

        then:
        notThrown Exception
    }

    @Test
    def 'should not throw exception when release lock that does not hold by any thread and neither one thread are waiting to acquire this lock'() {
        given:
        lockHolder.acquire lockId

        when:
        lockHolder.release lockId

        then:
        notThrown Exception
    }
}
