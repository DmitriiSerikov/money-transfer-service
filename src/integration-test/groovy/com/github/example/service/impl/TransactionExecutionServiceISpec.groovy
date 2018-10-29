package com.github.example.service.impl

import com.github.example.ITestSupport
import com.github.example.IntegrationTest
import com.github.example.dao.AccountDao
import com.github.example.dao.TransactionDao
import com.github.example.model.Account
import com.github.example.model.TransactionBuilder
import com.github.example.service.TransactionExecutionService
import io.micronaut.context.ApplicationContext
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors

import static io.micronaut.context.env.PropertySource.of

@Category(IntegrationTest)
class TransactionExecutionServiceISpec extends Specification implements ITestSupport {

    @Shared
    @AutoCleanup
    def applicationContext = ApplicationContext.run of(['processing.transactions.enabled': false])

    @Shared
    def executionService = applicationContext.getBean TransactionExecutionService
    @Shared
    def transactionDao = applicationContext.getBean TransactionDao
    @Shared
    def accountDao = applicationContext.getBean AccountDao

    @Test
    @Timeout(10)
    def 'should preserve consistency of summary balance and avoid deadlocks when execute concurrent transactions between two accounts'() {
        given:
        def firstAccountId = createAccount firstStartBalance as BigDecimal
        def secondAccountId = createAccount secondStartBalance as BigDecimal
        def txFromFirstToSecond = { executeTransaction firstAccountId, secondAccountId, txAmount as BigDecimal }
        def txFromSecondToFirst = { executeTransaction secondAccountId, firstAccountId, txAmount as BigDecimal }

        when:
        executeConcurrentTasks count, txFromFirstToSecond, txFromSecondToFirst
        def firstFinalBalance = accountDao.getBy(firstAccountId).balance
        def secondFinalBalance = accountDao.getBy(secondAccountId).balance

        then:
        expectedSummaryBalance == firstFinalBalance + secondFinalBalance

        where:
        firstStartBalance | secondStartBalance | txAmount | count || expectedSummaryBalance
        1000              | 1000               | 10       | 2000  || 2000
        0                 | 1000               | 1000     | 1000  || 1000
        0                 | 0                  | 500      | 500   || 0
    }

    @Test
    @Timeout(10)
    def 'should execute transaction successfully only once and avoid deadlocks when try execute same transaction concurrently'() {
        given:
        def firstAccountId = createAccount startBalance as BigDecimal
        def secondAccountId = createAccount startBalance as BigDecimal
        def transactionId = createTransaction firstAccountId, secondAccountId, txAmount as BigDecimal

        when:
        def result = executeConcurrentTasks count, { executionService.executeBy transactionId }

        then:
        result.success == expectedSuccessCount
        result.failed == expectedFailedCount

        where:
        startBalance | txAmount | count || expectedSuccessCount | expectedFailedCount
        1000         | 1000     | 500   || 1                    | 499
        2000         | 10       | 1000  || 1                    | 999
    }

    def executeConcurrentTasks(int count, Closure... tasks) {
        def executor = Executors.newFixedThreadPool(count)
        def completionService = new ExecutorCompletionService<>(executor)
        def barrier = new CyclicBarrier(count)

        count.times {
            def task = it % 2 == 0 ? tasks.first() : tasks.last()
            completionService.submit({
                barrier.await()
                task.call()
            })
        }

        int executed = 0
        int failed = 0
        int success = 0
        while (executed < count) {
            def future = completionService.take()
            if (future.done) {
                executed++
                future.get() ? success++ : failed++
            }
        }
        executor.shutdown()

        [failed: failed, success: success]
    }

    def createAccount(BigDecimal initialBalance) {
        def account = new Account(initialBalance)

        accountDao.insert(account)

        account.id
    }

    def createTransaction(UUID firstAccountId, UUID secondAccountId, BigDecimal amount) {
        def transaction = TransactionBuilder.builder(referenceId)
                .withdraw(firstAccountId, amount)
                .deposit(secondAccountId, amount)
                .build()

        transactionDao.insert(transaction)

        transaction.id
    }

    def executeTransaction(UUID firstAccountId, UUID secondAccountId, BigDecimal amount) {
        def transactionId = createTransaction firstAccountId, secondAccountId, amount
        executionService.executeBy transactionId
    }
}
