package com.example.demo.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import com.example.demo.model.TaskJob;

public record TaskResponseDto(String taskId, Instant executeAt, Object payload, String status,
        String message) {

    public static TaskResponseDto from(TaskJob taskJob) {
        return from(taskJob, "Task detail loaded from MySQL.");
    }

    public static TaskResponseDto from(TaskJob taskJob, String message) {
        return new TaskResponseDto(taskJob.id(), toInstant(taskJob.executeTime()),
                taskJob.payload(), taskJob.status(), message);
    }

    private static Instant toInstant(LocalDateTime executeTime) {
        return executeTime == null ? null : executeTime.toInstant(ZoneOffset.UTC);
    }
}
