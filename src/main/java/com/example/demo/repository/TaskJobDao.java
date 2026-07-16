package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import com.example.demo.constant.TaskStatus;
import com.example.demo.dto.TaskJobDto;
import com.example.demo.model.TaskJob;

@Component
public class TaskJobDao {

    private final JdbcTemplate jdbcTemplate;

    public TaskJobDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(TaskJob taskJob) {
        TaskJobDto taskJobDto = TaskJobDto.fromModel(taskJob);
        jdbcTemplate.update("""
                    INSERT INTO task_job (id, status, payload, execute_time)
                    VALUES (?, ?, ?, ?)
                """, taskJobDto.id(), taskJobDto.status(), taskJobDto.payload(),
                taskJobDto.executeTime());
    }

    public Optional<TaskJob> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    SELECT id, status, payload, execute_time
                    FROM task_job
                    WHERE id = ?
                    """, taskJobDtoRowMapper(), id).toModel());
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public long countByStatus(TaskStatus status) {
        return Optional.ofNullable(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM task_job
                WHERE status = ?
                """, Long.class, status.value())).orElse(0L);
    }

    public List<TaskJob> findByStatus(TaskStatus status, int limit, int offset) {
        return jdbcTemplate.query("""
                SELECT id, status, payload, execute_time
                FROM task_job
                WHERE status = ?
                ORDER BY execute_time ASC, id ASC
                LIMIT ? OFFSET ?
                """, taskJobDtoRowMapper(), status.value(), limit, offset).stream()
                .map(TaskJobDto::toModel).toList();
    }

    public List<TaskJob> findDuePendingTasks(LocalDateTime now, int limit) {
        return jdbcTemplate.query("""
                SELECT id, status, payload, execute_time
                FROM task_job
                WHERE status = ?
                AND execute_time <= ?
                ORDER BY execute_time ASC, id ASC
                LIMIT ?
                """, taskJobDtoRowMapper(), TaskStatus.PENDING.value(), now, limit).stream()
                .map(TaskJobDto::toModel).toList();
    }

    public boolean updateStatus(String id, TaskStatus currentStatus, TaskStatus nextStatus) {
        int updatedRows = jdbcTemplate.update("""
                UPDATE task_job
                SET status = ?
                WHERE id = ?
                AND status = ?
                """, nextStatus.value(), id, currentStatus.value());

        return updatedRows > 0;
    }

    private RowMapper<TaskJobDto> taskJobDtoRowMapper() {
        return (resultSet, rowNumber) -> new TaskJobDto(resultSet.getString("id"),
                resultSet.getString("status"), resultSet.getString("payload"),
                resultSet.getObject("execute_time", LocalDateTime.class));
    }
}
