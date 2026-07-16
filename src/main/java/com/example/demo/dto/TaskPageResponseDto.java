package com.example.demo.dto;

import java.util.List;

public record TaskPageResponseDto(int page, int size, long totalElements, int totalPages,
        List<TaskResponseDto> content) {

    public static TaskPageResponseDto from(TaskJobPageDto taskJobPage) {
        List<TaskResponseDto> tasks = taskJobPage.content().stream()
                .map(taskJob -> TaskResponseDto.from(taskJob, "Task listed from MySQL.")).toList();

        return new TaskPageResponseDto(taskJobPage.page(), taskJobPage.size(),
                taskJobPage.totalElements(), taskJobPage.totalPages(), tasks);
    }
}
