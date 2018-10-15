package com.github.example.handler;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateos.JsonError;
import io.micronaut.http.hateos.Link;
import io.micronaut.http.server.exceptions.ExceptionHandler;

import javax.inject.Singleton;

@Produces
@Singleton
@Requires(classes = ExceptionHandler.class)
public abstract class AbstractExceptionHandler<T extends Throwable> implements ExceptionHandler<T, HttpResponse> {

    protected abstract HttpStatus getStatus();

    @Override
    public HttpResponse handle(HttpRequest request, T t) {
        JsonError error = new JsonError(t.getMessage())
                .link(Link.SELF, Link.of(request.getUri()));
        return HttpResponse.status(getStatus()).body(error);
    }
}
