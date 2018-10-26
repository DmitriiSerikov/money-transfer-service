package com.github.example.service.impl

import com.github.example.IntegrationTest
import com.github.example.dto.request.CommandCreateAccount
import com.github.example.dto.request.CommandCreateTransaction
import com.github.example.service.AccountService
import com.github.example.service.TransactionExecutionService
import com.github.example.service.TransactionService
import io.micronaut.context.ApplicationContext
import net.bytebuddy.utility.RandomString
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors

@Category(IntegrationTest)
class TransactionExecutionServiceITSpec extends Specification {

    @Shared
    @AutoCleanup
    def applicationContext = ApplicationContext.run()

    @Shared
    def executionService = applicationContext.getBean TransactionExecutionService
    @Shared
    def transactionService = applicationContext.getBean TransactionService
    @Shared
    def accountService = applicationContext.getBean AccountService

    @Test
    @Timeout(value = 10)
    def "should preserve consistency of summary balance and avoid deadlocks when executes concurrent transactions between two accounts"() {
        given:
        def firstAccountId = accountService.createBy(new CommandCreateAccount(initialBalance: firstAccountBalance)).id
        def secondAccountId = accountService.createBy(new CommandCreateAccount(initialBalance: secondAccountBalance)).id
        def txFromFirstToSecond = { transaction firstAccountId, secondAccountId, txAmount }
        def txFromSecondToFirst = { transaction secondAccountId, firstAccountId, txAmount }

        when:
        executeConcurrentTransactions txCount, txFromFirstToSecond, txFromSecondToFirst

        then:
        def firstBalance = accountService.getById(firstAccountId).balance
        def secondBalance = accountService.getById(secondAccountId).balance
        expectedSummaryBalance == firstBalance + secondBalance

        where:
        firstAccountBalance | secondAccountBalance | txAmount | txCount || expectedSummaryBalance
        1000                | 1000                 | 10       | 2000    || 2000
        1000                | 2000                 | 1000     | 1000    || 3000
        0                   | 1000                 | 1000     | 500     || 1000
        0                   | 0                    | 500      | 100     || 0
    }

    def transaction(def firstAccountId, def secondAccountId, def amount) {
        def command = new CommandCreateTransaction(referenceId: new RandomString(40), sourceAccountId: firstAccountId, targetAccountId: secondAccountId, amount: amount)
        def transaction = transactionService.createBy command
        executionService.execute transaction.id
    }

    def executeConcurrentTransactions(int count, Closure txFromFirstToSecond, Closure txFromSecondToFirst) {
        def executor = Executors.newFixedThreadPool(count)
        def completionService = new ExecutorCompletionService<>(executor)
        def barrier = new CyclicBarrier(count)

        count.times {
            def closure = it % 2 == 0 ? txFromFirstToSecond : txFromSecondToFirst
            completionService.submit({
                barrier.await()
                closure.call()
            })
        }

        int executed = 0
        while (executed < count) {
            def future = completionService.take()
            if (future.done) executed++
        }
        executor.shutdown()
    }
}
