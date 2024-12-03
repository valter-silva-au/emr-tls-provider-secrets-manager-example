package com.amazonaws.awssamples.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.function.Function;

public class RetryHandler {
    private static final Logger logger = LoggerFactory.getLogger(RetryHandler.class);
    
    private static final int DEFAULT_MAX_RETRIES = 15;
    private static final long MIN_SLEEP = 1000L;
    private static final long SLEEP_RANGE = 2000L;
    private static final Random RANDOM = new Random();

    private final int maxRetries;

    public RetryHandler() {
        this(DEFAULT_MAX_RETRIES);
    }

    public RetryHandler(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public <T, R> R retry(Function<T, R> operation, T input) {
        R result = null;
        
        for (int i = 0; i < maxRetries; ++i) {
            if (i > 0) {
                logger.debug("{}: retry count: {}", Thread.currentThread().getName(), i);
            }

            result = operation.apply(input);
            if (result != null) {
                return result;
            }

            randomSleep(i);
        }

        throw new RetryExhaustedException("Retry attempts exhausted after " + maxRetries + " attempts");
    }

    private void randomSleep(int retryCount) {
        try {
            long sleepTime = MIN_SLEEP + Math.abs(RANDOM.nextLong()) % ((retryCount + 1) * SLEEP_RANGE);
            logger.trace("Sleeping for {} ms", sleepTime);
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Sleep interrupted", e);
        }
    }
}
