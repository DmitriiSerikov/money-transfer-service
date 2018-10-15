package com.github.example.job;

import com.github.example.service.TransactionExecutionService;
import io.micronaut.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;

@Singleton
public class TransactionsProcessingJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsProcessingJob.class);

    private static final int DEFAULT_PROCESSING_LIMIT = 200;

    private final TransactionExecutionService transactionExecutionService;

    @Inject
    public TransactionsProcessingJob(final TransactionExecutionService transactionExecutionService) {
        this.transactionExecutionService = transactionExecutionService;
    }

    @Scheduled(fixedRate = "15s")
    public void process() {
        LOGGER.info("Start processing pending transactions at {}", Instant.now());
        transactionExecutionService.executePending(DEFAULT_PROCESSING_LIMIT);
        LOGGER.info("Finished processing pending transactions at {}", Instant.now());
    }
}