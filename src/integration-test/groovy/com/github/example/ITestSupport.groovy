package com.github.example

import groovy.transform.CompileStatic
import net.bytebuddy.utility.RandomString

@CompileStatic
trait ITestSupport {

    final String apiV1Root = '/api/1.0'
    final String wrongFormatResourceId = '1'
    final UUID notExistResourceId = UUID.fromString 'f-f-f-f-f'

    String getReferenceId() {
        RandomString.make 40
    }
}
