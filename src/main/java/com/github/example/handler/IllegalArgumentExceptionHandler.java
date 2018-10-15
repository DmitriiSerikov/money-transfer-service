package com.github.example.handler;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpStatus;

@Requires(classes = IllegalArgumentException.class)
public class IllegalArgumentExceptionHandler extends AbstractExceptionHandler<IllegalArgumentException> {

    @Override
    protected HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
