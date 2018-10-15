package com.github.example.dao.impl

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.exception.EntityAlreadyExistsException
import com.github.example.exception.EntityNotFoundException
import com.github.example.holder.LockHolder
import com.github.example.model.Account
import org.junit.Test
import spock.lang.Specification

import static java.math.BigDecimal.ONE

/**
 * Unit test for {@link InMemoryAccountDaoImpl}
 */
class InMemoryAccountDaoSpec extends Specification {

    @Subject
    InMemoryAccountDaoImpl inMemoryAccountDao

    @Collaborator
    LockHolder lockHolder = Mock()

    def account = new Account(ONE)

    @Test
    def "should throw exception when account for insertion is null"() {
        when:
        inMemoryAccountDao.insert null

        then:
        thrown IllegalArgumentException
    }

    @Test
    def "should throw exception when storage already contains account for this id"() {
        given:
        inMemoryAccountDao.insert account

        when:
        inMemoryAccountDao.insert account

        then:
        thrown EntityAlreadyExistsException
    }

    @Test
    def "should return same account when it successfully inserted in storage"() {
        when:
        def result = inMemoryAccountDao.insert account

        then:
        result == account
    }

    @Test
    def "should return empty collection when accounts storage is empty"() {
        when:
        def result = inMemoryAccountDao.findAll()

        then:
        result.empty
    }

    @Test
    def "should return collection of stored accounts when accounts storage is not empty"() {
        given:
        inMemoryAccountDao.insert account

        when:
        def result = inMemoryAccountDao.findAll()

        then:
        !result.empty
        result[0] == account
    }

    @Test
    def "should throw exception when storage doesn't contains account for specified id"() {
        when:
        inMemoryAccountDao.getBy 10

        then:
        thrown EntityNotFoundException
    }

    @Test
    def "should return stored account when storage contains account for specified id"() {
        given:
        inMemoryAccountDao.insert account

        when:
        def result = inMemoryAccountDao.getBy account.id

        then:
        result == account
    }

    @Test
    def "should throw exception when account for update is null"() {
        when:
        inMemoryAccountDao.update null

        then:
        thrown IllegalArgumentException
    }

    @Test
    def "should acquire lock for account id using holder before updating account"() {
        when:
        inMemoryAccountDao.update account

        then:
        1 * lockHolder.acquire({ it.contains(account.id as String) } as String)
    }

    @Test
    def "should store account when storage doesn't contains account with specified id"() {
        when:
        inMemoryAccountDao.update account

        then:
        inMemoryAccountDao.getBy(account.id) == account
    }

    @Test
    def "should update account when storage already contains account with specified id"() {
        given:
        def updatedAccount = account.withdraw ONE
        inMemoryAccountDao.insert account

        when:
        inMemoryAccountDao.update updatedAccount

        then:
        inMemoryAccountDao.getBy(account.id) == updatedAccount
    }

    @Test
    def "should release lock for account id using holder when finishes update of account"() {
        when:
        inMemoryAccountDao.update account

        then:
        1 * lockHolder.release({ it.contains(account.id as String) } as String)
    }

    @Test
    def "should acquire lock using holder when try to lock account for specified id"() {
        when:
        inMemoryAccountDao.lockBy 10

        then:
        1 * lockHolder.acquire("Account_10")
    }

    @Test
    def "should release lock using holder when try to unlock account for specified id"() {
        when:
        inMemoryAccountDao.unlockBy 10

        then:
        1 * lockHolder.release("Account_10")
    }
}
