package com.example.demo.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import com.example.demo.model.TaskJob;

public record TaskDetailResponseDto(String taskId, Instant executeAt, Object payload,
        String status) {

    public static TaskDetailResponseDto from(TaskJob taskJob) {
        return new TaskDetailResponseDto(taskJob.id(), toInstant(taskJob.executeTime()),
                taskJob.payload(), taskJob.status());
    }

    private static Instant toInstant(LocalDateTime executeTime) {
        return executeTime == null ? null : executeTime.toInstant(ZoneOffset.UTC);
    }
}