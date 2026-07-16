package com.example.demo.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.example.demo.config.TaskJobConfig;
import com.example.demo.constant.TaskStatus;
import com.example.demo.dto.TaskJobDto;
import com.example.demo.dto.TaskJobPageDto;
import com.example.demo.exception.GeneralRuntimeException;
import com.example.demo.model.TaskJob;
import com.example.demo.repository.JobLockerDao;
import com.example.demo.repository.TaskJobDao;
import com.example.demo.util.JsonUtil;

@Service
public class TaskJobService {

    private final TaskJobDao taskJobDao;
    private final JsonUtil jsonUtil;
    private final JobLockerDao jobLockerDao;
    private final Duration lockTimeout;

    public TaskJobService(TaskJobDao taskJobDao, JsonUtil jsonUtil, JobLockerDao jobLockerDao,
            TaskJobConfig taskJobConfig) {
        this.taskJobDao = taskJobDao;
        this.jsonUtil = jsonUtil;
        this.jobLockerDao = jobLockerDao;
        this.lockTimeout = taskJobConfig.lockTimeout();
    }

    public TaskJob create(String id, Instant executeAt, Map<String, Object> payload) {
        String payloadJson = jsonUtil.toJson(payload);

        TaskJob taskJob = TaskJobDto.fromCreate(id, executeAt, payloadJson).toModel();

        try {
            taskJobDao.create(taskJob);
        } catch (DuplicateKeyException e1) {
            throw new GeneralRuntimeException(HttpStatus.CONFLICT.value(),
                    "Task job with id " + id + " already exists.");
        }

        return taskJob;
    }

    public Optional<TaskJob> findById(String id) {
        return taskJobDao.findById(id);
    }

    public TaskJobPageDto findByStatus(TaskStatus status, int page, int size) {
        long totalElements = taskJobDao.countByStatus(status);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        int offset = (page - 1) * size;

        return new TaskJobPageDto(page, size, totalElements, totalPages,
                taskJobDao.findByStatus(status, size, offset));
    }

    public List<TaskJob> findDuePendingTasks(Instant now, int limit) {
        LocalDateTime nowDateTime = LocalDateTime.ofInstant(now, ZoneOffset.UTC);

        return taskJobDao.findDuePendingTasks(nowDateTime, limit);
    }

    public boolean markTriggered(String id) {
        return taskJobDao.updateStatus(id, TaskStatus.PENDING, TaskStatus.TRIGGERED);
    }

    public Optional<TaskJob> cancelById(String id) {
        Optional<TaskJob> taskJob = findById(id);
        if (taskJob.isEmpty()) {
            throw new GeneralRuntimeException(HttpStatus.NOT_FOUND.value(),
                    "Task job with id " + id + " not found.");
        }

        Optional<String> lockValue = jobLockerDao.tryLock(id, lockTimeout);
        if (lockValue.isEmpty()) {
            throw new GeneralRuntimeException(HttpStatus.CONFLICT.value(),
                    "Task job with id " + id + " is locked.");
        }

        try {
            boolean cancelled =
                    taskJobDao.updateStatus(id, TaskStatus.PENDING, TaskStatus.CANCELLED);
            if (!cancelled) {
                throw new GeneralRuntimeException(HttpStatus.CONFLICT.value(),
                        "Task job with id " + id + " is not in pending status.");
            }

            TaskJob cancelledTaskJob = new TaskJob(taskJob.get().id(), TaskStatus.CANCELLED.value(),
                    taskJob.get().payload(), taskJob.get().executeTime());

            return Optional.of(cancelledTaskJob);
        } finally {
            jobLockerDao.unlock(id, lockValue.get());
        }
    }
}
