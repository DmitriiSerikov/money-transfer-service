package com.github.example.job

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.UnitTest
import com.github.example.service.TransactionExecutionService
import org.junit.Test
import org.junit.experimental.categories.Category
import spock.lang.Specification

@Category(UnitTest)
class TransactionsProcessingJobSpec extends Specification {

    @Subject
    TransactionsProcessingJob transactionsProcessingJob

    @Collaborator
    TransactionExecutionService transactionExecutionService = Mock()

    def limit = 100

    def setup() {
        transactionsProcessingJob.setProcessingLimit limit
    }

    @Test
    def 'should start processing of pending transaction with specified limit when job is enabled and triggered by scheduler'() {
        given:
        transactionsProcessingJob.setEnabled true

        when:
        transactionsProcessingJob.process()

        then:
        1 * transactionExecutionService.executePending(limit)
    }

    @Test
    def 'should not start processing of pending transaction when job is disabled'() {
        given:
        transactionsProcessingJob.setEnabled false

        when:
        transactionsProcessingJob.process()

        then:
        0 * transactionExecutionService.executePending(limit)
    }
}
