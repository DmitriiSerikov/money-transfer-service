package com.github.example.service.impl

import com.github.example.ITestSupport
import com.github.example.IntegrationTest
import com.github.example.dto.request.CommandCreateAccount
import com.github.example.dto.request.CommandPerformTransfer
import com.github.example.service.AccountService
import com.github.example.service.TransactionExecutionService
import com.github.example.service.TransactionService
import io.micronaut.context.ApplicationContext
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

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
    def transactionService = applicationContext.getBean TransactionService
    @Shared
    def accountService = applicationContext.getBean AccountService

    @Test
    def 'should preserve consistency of summary balance and avoid deadlocks when execute concurrent transactions between two accounts'() {
        given:
        def firstAccountId = accountService.createBy(new CommandCreateAccount(initialBalance: firstStartBalance)).id
        def secondAccountId = accountService.createBy(new CommandCreateAccount(initialBalance: secondStartBalance)).id
        def txFromFirstToSecond = { transfer firstAccountId, secondAccountId, txAmount }
        def txFromSecondToFirst = { transfer secondAccountId, firstAccountId, txAmount }

        when:
        def result = executeConcurrentTasks count, txFromFirstToSecond, txFromSecondToFirst
        def firstFinalBalance = accountService.getById(firstAccountId).balance
        def secondFinalBalance = accountService.getById(secondAccountId).balance

        then:
        expectedSummaryBalance == firstFinalBalance + secondFinalBalance
        result.success == count
        result.failed == 0

        where:
        firstStartBalance | secondStartBalance | txAmount | count || expectedSummaryBalance
        1000              | 1000               | 10       | 2000  || 2000
        0                 | 1000               | 1000     | 1000  || 1000
        0                 | 0                  | 500      | 500   || 0
    }

    @Test
    def 'should execute transaction successfully only once and avoid deadlocks when try execute same transaction concurrently'() {
        given:
        def firstAccountId = accountService.createBy(new CommandCreateAccount(initialBalance: startBalance)).id
        def secondAccountId = accountService.createBy(new CommandCreateAccount(initialBalance: startBalance)).id
        def transaction = transactionService.transferBy new CommandPerformTransfer(referenceId: referenceId, sourceAccountId: firstAccountId, targetAccountId: secondAccountId, amount: txAmount)

        when:
        def result = executeConcurrentTasks count, { executionService.execute transaction.id }

        then:
        result.success == expectedSuccessCount
        result.failed == expectedFailedCount

        where:
        startBalance | txAmount | count || expectedSuccessCount | expectedFailedCount
        1000         | 1000     | 500   || 1                    | 499
        2000         | 10       | 1000  || 1                    | 999
    }

    def transfer(def firstAccountId, def secondAccountId, def amount) {
        def command = new CommandPerformTransfer(referenceId: referenceId, sourceAccountId: firstAccountId, targetAccountId: secondAccountId, amount: amount)
        def transaction = transactionService.transferBy command
        executionService.execute transaction.id
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
                try {
                    future.get()
                    success++
                } catch (ignored) {
                    failed++
                }
            }
        }
        executor.shutdown()

        [failed: failed, success: success]
    }
}
