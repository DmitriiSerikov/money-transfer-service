package com.github.example

import com.github.example.model.Account
import com.github.example.model.Transaction
import com.github.example.model.TransactionBuilder
import com.github.example.model.TransactionEntry
import groovy.transform.CompileStatic

import static java.util.UUID.fromString

@CompileStatic
trait TestSupport {

    final UUID notExistResourceId = fromString 'f-f-f-f-f'
    final UUID firstAccountId = fromString 'a-b-c-d-0'
    final UUID secondAccountId = fromString 'a-b-c-d-1'
    final String referenceId = 'reference'
    final BigDecimal amount = 10

    Account accountStub(BigDecimal initialBalance = 10) {
        new Account(initialBalance)
    }

    Transaction transactionStub(String refId = referenceId, UUID sourceAccountId = firstAccountId, UUID targetAccountId = secondAccountId, BigDecimal txAmount = amount) {
        TransactionBuilder.builder(refId)
                .withdraw(sourceAccountId, txAmount)
                .deposit(targetAccountId, txAmount)
                .build()
    }

    TransactionEntry getTransactionEntry(BigDecimal txAmount = amount, UUID accountId = firstAccountId) {
        new TransactionEntry(accountId, txAmount)
    }
}
