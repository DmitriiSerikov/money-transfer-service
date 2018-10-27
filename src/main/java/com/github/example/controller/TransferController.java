package com.github.example.controller;

import com.github.example.dto.request.CommandPerformTransfer;
import com.github.example.dto.response.ExecutionResultData;
import com.github.example.model.Transaction;
import com.github.example.service.TransactionService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import org.modelmapper.ModelMapper;

import javax.inject.Inject;

@Controller("/api/1.0/transfers")
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
    public HttpResponse<ExecutionResultData> performTransfer(@Body final CommandPerformTransfer command) {
        final Transaction transaction = transactionService.transferBy(command);
        return HttpResponse.ok(convertToDto(transaction));
    }

    @Override
    protected Class<ExecutionResultData> getDtoClass() {
        return ExecutionResultData.class;
    }
}
