package com.github.example.controller;

import com.github.example.dto.request.CommandPerformTransfer;
import com.github.example.dto.response.ExecutionResultData;
import com.github.example.model.Transaction;
import com.github.example.service.TransactionService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.modelmapper.ModelMapper;

import javax.inject.Inject;

@Controller("/api/${money.transfer.api.version}/transfers")
public class TransferController extends AbstractController<Transaction, ExecutionResultData> {

    private final TransactionService transactionService;

    @Inject
    public TransferController(final ModelMapper modelMapper, final TransactionService transactionService) {
        super(modelMapper);
        this.transactionService = transactionService;
    }

    @Post
    @Consumes
    @Produces
    @Operation(
            summary = "Transfer", tags = "Transactions",
            description = "This endpoint processes transfers between accounts"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer processing result",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ExecutionResultData")
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
    public HttpResponse<ExecutionResultData> performTransfer(@Body final CommandPerformTransfer command) {
        final Transaction transaction = transactionService.transferBy(command);
        return HttpResponse.ok(convertToDto(transaction));
    }

    @Override
    protected Class<ExecutionResultData> getDtoClass() {
        return ExecutionResultData.class;
    }
}
