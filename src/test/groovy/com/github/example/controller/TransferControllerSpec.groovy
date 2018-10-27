package com.github.example.controller

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.github.example.TestSupport
import com.github.example.UnitTest
import com.github.example.dto.request.CommandPerformTransfer
import com.github.example.dto.response.ExecutionResultData
import com.github.example.service.TransactionService
import io.micronaut.http.HttpStatus
import org.junit.Test
import org.junit.experimental.categories.Category
import org.modelmapper.ModelMapper
import spock.lang.Specification

@Category(UnitTest)
class TransferControllerSpec extends Specification implements TestSupport {

    @Subject
    TransferController controller

    @Collaborator
    TransactionService transactionService = Mock()
    @Collaborator
    ModelMapper modelMapper = Mock()

    def transaction = TransactionStub()
    def command = new CommandPerformTransfer()

    @Test
    def 'should throw exception when service could not perform transfer by command and throws exception'() {
        given:
        transactionService.transferBy(command) >> { throw new IllegalArgumentException() }

        when:
        controller.performTransfer command

        then:
        thrown IllegalArgumentException
    }

    @Test
    def 'should respond with OK code when service successfully initiated transfer by command'() {
        given:
        transactionService.transferBy(command) >> transaction

        when:
        def result = controller.performTransfer command

        then:
        result.status == HttpStatus.OK
    }

    @Test
    def 'should use mapper for conversion when service successfully initiated transfer by command and returned transaction'() {
        given:
        transactionService.transferBy(command) >> transaction

        when:
        controller.performTransfer command

        then:
        1 * modelMapper.map(transaction, ExecutionResultData)
    }

    @Test
    def 'should use execution result data as dto when convert responses from internal model'() {
        when:
        def result = controller.getDtoClass()

        then:
        result == ExecutionResultData
    }
}
