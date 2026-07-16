package com.example.demo.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import com.example.demo.constant.TaskStatus;
import com.example.demo.model.TaskJob;

public record TaskJobDto(String id, String status, String payload, LocalDateTime executeTime) {

    public static TaskJobDto fromCreate(String id, Instant executeAt, String payload) {
        return new TaskJobDto(id, TaskStatus.PENDING.value(), payload,
                LocalDateTime.ofInstant(executeAt, ZoneOffset.UTC));
    }

    public static TaskJobDto fromModel(TaskJob taskJob) {
        return new TaskJobDto(taskJob.id(), taskJob.status(), taskJob.payload(),
                taskJob.executeTime());
    }

    public TaskJob toModel() {
        return new TaskJob(id, status, payload, executeTime);
    }
}
