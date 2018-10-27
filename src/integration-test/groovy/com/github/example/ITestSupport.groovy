package com.github.example

import groovy.transform.CompileStatic
import net.bytebuddy.utility.RandomString

@CompileStatic
trait ITestSupport {

    final String API_V1_ROOT = '/api/1.0'
    final String WRONG_FORMAT_RESOURCE_ID = '1'
    final UUID NOT_EXIST_RESOURCE_ID = UUID.fromString 'f-f-f-f-f'

    def getReferenceId() {
        [length: 40] as RandomString
    }
}
