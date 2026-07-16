package com.example.demo.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "task-job")
public class TaskJobConfig {

    private long checkFixedDelay = 5000;
    private int checkBatchSize = 100;
    private long lockTimeout = 30000;

    public long getCheckFixedDelay() {
        return checkFixedDelay;
    }

    public void setCheckFixedDelay(long checkFixedDelay) {
        this.checkFixedDelay = checkFixedDelay;
    }

    public int getCheckBatchSize() {
        return checkBatchSize;
    }

    public void setCheckBatchSize(int checkBatchSize) {
        this.checkBatchSize = checkBatchSize;
    }

    public long getLockTimeout() {
        return lockTimeout;
    }

    public void setLockTimeout(long lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    public int checkBatchSize() {
        return checkBatchSize;
    }

    public String checkFixedDelayMillis() {
        return String.valueOf(checkFixedDelay);
    }

    public Duration lockTimeout() {
        return Duration.ofMillis(lockTimeout);
    }
}
