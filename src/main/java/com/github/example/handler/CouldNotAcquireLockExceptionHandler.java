package com.github.example.handler;

import com.github.example.exception.CouldNotAcquireLockException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpStatus;

@Requires(classes = CouldNotAcquireLockException.class)
public class CouldNotAcquireLockExceptionHandler extends AbstractExceptionHandler<CouldNotAcquireLockException> {

    @Override
    protected HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}