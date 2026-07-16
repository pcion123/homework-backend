package com.example.demo.constant;

import java.util.Locale;
import java.util.Optional;

public enum TaskStatus {
    PENDING, TRIGGERED, CANCELLED, COMPLETED;

    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<TaskStatus> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        for (TaskStatus taskStatus : values()) {
            if (taskStatus.name().equalsIgnoreCase(value)
                    || taskStatus.value().equalsIgnoreCase(value)) {
                return Optional.of(taskStatus);
            }
        }

        return Optional.empty();
    }
}
