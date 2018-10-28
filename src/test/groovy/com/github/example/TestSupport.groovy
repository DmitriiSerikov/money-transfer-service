package com.github.example

import com.github.example.model.Account
import com.github.example.model.Transaction
import groovy.transform.CompileStatic

import static java.util.UUID.fromString

@CompileStatic
trait TestSupport {

    final UUID notExistResourceId = fromString 'f-f-f-f-f'
    final UUID firstAccountId = fromString 'a-b-c-d-0'
    final UUID secondAccountId = fromString 'a-b-c-d-1'
    final String referenceId = 'reference'
    final BigDecimal amount = 10

    Account AccountStub(BigDecimal initialBalance = 10) {
        new Account(initialBalance)
    }

    Transaction TransactionStub(UUID sourceAccountId = firstAccountId, String refId = referenceId,
                                UUID targetAccountId = secondAccountId, BigDecimal txAmount = amount) {
        new Transaction(refId, sourceAccountId, targetAccountId, txAmount)
    }
}
