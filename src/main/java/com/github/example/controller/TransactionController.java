package com.github.example.controller;

import com.github.example.dto.response.TransactionData;
import com.github.example.model.Transaction;
import com.github.example.service.TransactionService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.modelmapper.ModelMapper;

import javax.inject.Inject;
import java.util.Collection;
import java.util.UUID;

@Controller("/api/${money.transfer.api.version}/transactions")
public class TransactionController extends AbstractController<Transaction, TransactionData> {

    private final TransactionService transactionService;

    @Inject
    public TransactionController(final ModelMapper modelMapper, final TransactionService transactionService) {
        super(modelMapper);
        this.transactionService = transactionService;
    }

    @Get
    @Produces
    @Operation(
            summary = "Get Transactions",
            description = "This endpoint retrieves all transactions",
            parameters = @Parameter(description = "ID of transaction")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All transactions",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(ref = "#/components/schemas/TransactionData"))
                    )
            )
    })
    public Collection<TransactionData> getAllTransactions() {
        return convertToDto(transactionService.getAll());
    }

    @Get(uri = "/{transactionId}")
    @Produces
    @Operation(summary = "Get Transaction", description = "This endpoint retrieves one of transactions by ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction by ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/TransactionData")
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
    public HttpResponse<TransactionData> getTransactionById(final UUID transactionId) {
        final Transaction transaction = transactionService.getById(transactionId);
        return HttpResponse.ok(convertToDto(transaction));
    }

    @Override
    protected Class<TransactionData> getDtoClass() {
        return TransactionData.class;
    }
}