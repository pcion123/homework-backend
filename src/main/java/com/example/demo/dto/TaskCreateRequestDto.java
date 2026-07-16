package com.example.demo.dto;

import java.time.Instant;
import java.util.Map;

public record TaskCreateRequestDto(String taskId, Instant executeAt, Map<String, Object> payload) {

    private static final String DEFAULT_TASK_ID = "abc-123";
    private static final Instant DEFAULT_EXECUTE_AT = Instant.parse("2025-07-21T15:00:00Z");
    private static final Map<String, Object> DEFAULT_PAYLOAD = Map.of("type", "email", "target",
            "hello@example.com", "message", "This is a scheduled task!");

    public String normalizedTaskId() {
        return taskId == null || taskId.isBlank() ? DEFAULT_TASK_ID : taskId;
    }

    public Instant normalizedExecuteAt() {
        return executeAt == null ? DEFAULT_EXECUTE_AT : executeAt;
    }

    public Map<String, Object> normalizedPayload() {
        return payload == null ? DEFAULT_PAYLOAD : payload;
    }
}
