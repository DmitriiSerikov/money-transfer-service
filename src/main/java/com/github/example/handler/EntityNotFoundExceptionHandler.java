package com.github.example.handler;

import com.github.example.exception.EntityNotFoundException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpStatus;

@Requires(classes = EntityNotFoundException.class)
public class EntityNotFoundExceptionHandler extends AbstractExceptionHandler<EntityNotFoundException> {

    @Override
    protected HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
