package com.example.demo.controller;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.constant.TaskStatus;
import com.example.demo.dto.TaskCreateRequestDto;
import com.example.demo.dto.TaskPageResponseDto;
import com.example.demo.dto.TaskResponseDto;
import com.example.demo.exception.GeneralRuntimeException;
import com.example.demo.model.TaskJob;
import com.example.demo.service.TaskJobService;

@RestController
@RequestMapping("/tasks")
public class TaskSecheduleController {

    private final TaskJobService taskJobService;

    public TaskSecheduleController(TaskJobService taskJobService) {
        this.taskJobService = taskJobService;
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskCreateRequestDto request) {
        String taskId = request.normalizedTaskId();
        Instant executeAt = request.normalizedExecuteAt();
        Map<String, Object> payload = request.normalizedPayload();

        try {
            TaskJob taskJob = taskJobService.create(taskId, executeAt, payload);

            TaskResponseDto response = TaskResponseDto.from(taskJob, "Task has been scheduled.");

            return ResponseEntity.created(URI.create("/tasks/" + taskId)).body(response);
        } catch (GeneralRuntimeException e1) {
            return ResponseEntity.status(e1.getCode()).body(e1.getMessage());
        } catch (Exception e2) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> getTask(@PathVariable String taskId) {
        return taskJobService.findById(taskId)
                .map(taskJob -> ResponseEntity.ok(TaskResponseDto.from(taskJob)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> cancelTask(@PathVariable String taskId) {
        try {
            return taskJobService.cancelById(taskId)
                    .map(taskJob -> ResponseEntity
                            .ok(TaskResponseDto.from(taskJob, "Task has been cancelled.")))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (GeneralRuntimeException e1) {
            return ResponseEntity.status(e1.getCode()).body(e1.getMessage());
        } catch (Exception e2) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<TaskPageResponseDto> listTasks(
            @RequestParam(defaultValue = "pending") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 1 || size <= 0) {
            return ResponseEntity.badRequest().build();
        }

        return TaskStatus.fromValue(status)
                .map(taskStatus -> ResponseEntity.ok(TaskPageResponseDto
                        .from(taskJobService.findByStatus(taskStatus, page, size))))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
