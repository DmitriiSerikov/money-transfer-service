package com.github.example.job

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.service.TransactionExecutionService
import org.junit.Test
import spock.lang.Specification

/**
 * Unit test for {@link TransactionsProcessingJob}
 */
class TransactionsProcessingJobSpec extends Specification {

    @Subject
    TransactionsProcessingJob transactionsProcessingJob

    @Collaborator
    TransactionExecutionService transactionExecutionService = Mock()

    @Test
    def "should start processing of pending transaction with specified limit when job is triggered by scheduler"() {
        when:
        transactionsProcessingJob.process()

        then:
        1 * transactionExecutionService.executePending(_)
    }
}
