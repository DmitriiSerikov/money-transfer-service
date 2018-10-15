package com.github.example.handler;

import com.github.example.exception.EntityAlreadyExistsException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpStatus;

@Requires(classes = EntityAlreadyExistsException.class)
public class EntityAlreadyExistsExceptionHandler extends AbstractExceptionHandler<EntityAlreadyExistsException> {

    @Override
    protected HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
