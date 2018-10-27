package com.github.example

import com.github.example.model.Account
import com.github.example.model.Transaction
import groovy.transform.CompileStatic

import static java.util.UUID.fromString

@CompileStatic
trait TestSupport {

    Account AccountStub() {
        new Account(10 as BigDecimal)
    }

    Transaction TransactionStub() {
        new Transaction("ref", fromString("a-b-c-d-e"), fromString("a-b-c-d-f"), 10 as BigDecimal)
    }
}
