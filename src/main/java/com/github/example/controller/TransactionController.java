package com.github.example.controller;

import com.github.example.dto.response.TransactionData;
import com.github.example.model.Transaction;
import com.github.example.service.TransactionService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import org.modelmapper.ModelMapper;

import javax.inject.Inject;
import java.util.Collection;
import java.util.UUID;

@Controller("/api/1.0/transactions")
public class TransactionController extends AbstractController<Transaction, TransactionData> {

    private final TransactionService transactionService;

    @Inject
    public TransactionController(final ModelMapper modelMapper, final TransactionService transactionService) {
        super(modelMapper);
        this.transactionService = transactionService;
    }

    @Get
    @Produces
    public Collection<TransactionData> getAllTransactions() {
        return convertToDto(transactionService.getAll());
    }

    @Get(value = "/{transactionId}")
    @Produces
    public HttpResponse<TransactionData> getTransactionById(final UUID transactionId) {
        final Transaction transaction = transactionService.getById(transactionId);
        return HttpResponse.ok(convertToDto(transaction));
    }

    @Override
    protected Class<TransactionData> getDtoClass() {
        return TransactionData.class;
    }
}