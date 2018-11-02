package com.github.example.controller;

import com.github.example.dto.request.CommandCreateAccount;
import com.github.example.dto.response.AccountData;
import com.github.example.model.Account;
import com.github.example.service.AccountService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.modelmapper.ModelMapper;

import javax.inject.Inject;
import java.net.URI;
import java.util.Collection;
import java.util.UUID;

@Controller("/api/${money.transfer.api.version}/accounts")
public class AccountController extends AbstractController<Account, AccountData> {

    private final AccountService accountService;

    @Inject
    public AccountController(final ModelMapper modelMapper, final AccountService accountService) {
        super(modelMapper);
        this.accountService = accountService;
    }

    @Get
    @Produces
    @Operation(
            summary = "Get Accounts", tags = "Accounts",
            description = "This endpoint retrieves all accounts"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All accounts",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(ref = "#/components/schemas/AccountData"))
                    )
            )
    })
    public Collection<AccountData> getAllAccounts() {
        return convertToDto(accountService.getAll());
    }

    @Get(uri = "/{accountId}")
    @Produces
    @Operation(
            summary = "Get Account", tags = "Accounts",
            description = "This endpoint retrieves one of accounts by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account by ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/AccountData")
                    )
            ),
            @ApiResponse(
                    description = "Unexpected error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/JsonError")
                    )
            )
    })
    public HttpResponse<AccountData> getAccountById(final UUID accountId) {
        final Account account = accountService.getById(accountId);
        return HttpResponse.ok(convertToDto(account));
    }

    @Post
    @Consumes
    @Produces
    @Operation(
            summary = "Create Account", tags = "Accounts",
            description = "This endpoint creates new account by initial parameters"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Created account",
                    headers = @Header(
                            name = "location",
                            description = "Created account location",
                            schema = @Schema(type = "string", format = "uri")
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/AccountData")
                    )
            ),
            @ApiResponse(
                    description = "Unexpected error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/JsonError")
                    )
            )
    })
    public HttpResponse<AccountData> createAccount(@Body final CommandCreateAccount command, final HttpRequest request) {
        final Account account = accountService.createBy(command);
        final URI location = HttpResponse.uri(request.getPath() + "/" + account.getId());
        return HttpResponse.created(convertToDto(account), location);
    }

    @Override
    protected Class<AccountData> getDtoClass() {
        return AccountData.class;
    }
}