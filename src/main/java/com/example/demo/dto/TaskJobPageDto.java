package com.example.demo.dto;

import java.util.List;
import com.example.demo.model.TaskJob;

public record TaskJobPageDto(int page, int size, long totalElements, int totalPages,
        List<TaskJob> content) {
}
