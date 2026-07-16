package com.example.demo.dto;

import java.util.List;

public record TaskPageResponseDto(int page, int size, long totalElements, int totalPages,
                List<TaskDetailResponseDto> content) {

        public static TaskPageResponseDto from(TaskJobPageDto taskJobPage) {
                List<TaskDetailResponseDto> tasks = taskJobPage.content().stream()
                                .map(TaskDetailResponseDto::from).toList();

                return new TaskPageResponseDto(taskJobPage.page(), taskJobPage.size(),
                                taskJobPage.totalElements(), taskJobPage.totalPages(), tasks);
        }
}
