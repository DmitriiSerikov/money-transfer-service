package com.github.example.job;

import com.github.example.service.TransactionExecutionService;
import io.micronaut.context.annotation.Value;
import io.micronaut.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;

@Singleton
public class TransactionsProcessingJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsProcessingJob.class);

    private final TransactionExecutionService transactionExecutionService;

    @Value("${processing.transactions.limit:200}")
    private int processingLimit;

    @Value("${processing.transactions.enabled:true}")
    private boolean enabled;

    @Inject
    public TransactionsProcessingJob(final TransactionExecutionService transactionExecutionService) {
        this.transactionExecutionService = transactionExecutionService;
    }

    @Scheduled(cron = "${processing.transactions.cron:0 0 * ? * *}")
    public void process() {
        if (enabled) {
            LOGGER.info("Start processing pending transactions at {}", Instant.now());
            transactionExecutionService.executePending(processingLimit);
            LOGGER.info("Finished processing pending transactions at {}", Instant.now());
        }
    }
}