package com.github.example;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(
        info = @Info(
                title = "Money transfer service",
                version = "1.0",
                description = "Standalone service that exposes simple RESTful API for money transfers between accounts",
                license = @License(name = "MIT License", url = "https://github.com/DmitriiSerikov/money-transfer-service/blob/master/LICENSE"),
                contact = @Contact(name = "Dmitrii Serikov", url = "https://www.linkedin.com/in/dmitry-serikov/")
        ),
        tags = {
                @Tag(name = "Transactions"),
                @Tag(name = "Accounts")
        },
        servers = {
                @Server(url = "http://localhost:8080/api/1.0", description = "Development environment"),
                @Server(url = "https://money-transfer-service.herokuapp.com/api/1.0", description = "Staging environment")
        },
        externalDocs = @ExternalDocumentation(url = "https://www.getpostman.com/collections/57052f61858db233b359", description = "Postman collection")
)
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class);
    }
}