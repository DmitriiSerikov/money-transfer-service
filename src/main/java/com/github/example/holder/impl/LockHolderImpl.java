package com.github.example.holder.impl;

import com.github.example.exception.CouldNotAcquireLockException;
import com.github.example.holder.LockHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Optional.ofNullable;

@Singleton
public class LockHolderImpl implements LockHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockHolderImpl.class);
    private static final long DEFAULT_TIMEOUT = 5L;

    private final Map<String, Lock> locks = new ConcurrentHashMap<>();

    @Override
    public void acquire(final String lockId) {
        locks.compute(lockId, this::getAcquiredLock);
    }

    @Override
    public void release(final String lockId) {
        locks.computeIfPresent(lockId, this::releaseLock);
    }

    private Lock getAcquiredLock(final String lockId, final Lock previousValue) {
        try {
            final Lock lock = ofNullable(previousValue).orElseGet(ReentrantLock::new);
            if (!lock.tryLock(DEFAULT_TIMEOUT, TimeUnit.SECONDS)) {
                throw new CouldNotAcquireLockException("Could not acquire lock for key:" + lockId);
            }
            LOGGER.info("Lock acquired for id:{}", lockId);
            return lock;
        } catch (InterruptedException ex) {
            final Thread currentThread = Thread.currentThread();
            LOGGER.error("Thread {} where interrupted when acquire lock for id:{}", currentThread.getName(), lockId);
            currentThread.interrupt();
            throw new CouldNotAcquireLockException("Lock not acquired due to interruption of thread, id:" + lockId, ex);
        }
    }

    private Lock releaseLock(final String lockId, final Lock lock) {
        try {
            lock.unlock();
            LOGGER.info("Lock released for id:{}", lockId);
        } catch (Exception ex) {
            LOGGER.error("Couldn't release lock for entity with id:{}", lockId, ex);
        }
        return lock;
    }
}
