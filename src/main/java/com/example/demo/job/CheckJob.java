package com.example.demo.job;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.demo.config.TaskJobConfig;
import com.example.demo.model.TaskJob;
import com.example.demo.mq.RocketMessage;
import com.example.demo.mq.TaskMessagePublisher;
import com.example.demo.repository.JobLockerDao;
import com.example.demo.service.TaskJobService;

@Component
public class CheckJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckJob.class);

    private final TaskJobConfig taskJobConfig;
    private final JobLockerDao jobLockerDao;
    private final TaskJobService taskJobService;
    private final TaskMessagePublisher taskMessagePublisher;

    public CheckJob(TaskJobConfig taskJobConfig, JobLockerDao jobLockerDao,
            TaskJobService taskJobService, TaskMessagePublisher taskMessagePublisher) {
        this.taskJobConfig = taskJobConfig;
        this.jobLockerDao = jobLockerDao;
        this.taskJobService = taskJobService;
        this.taskMessagePublisher = taskMessagePublisher;
    }

    @Scheduled(fixedDelayString = "#{@taskJobConfig.checkFixedDelayMillis()}")
    public void checkDueTasks() {
        List<TaskJob> dueTasks =
                taskJobService.findDuePendingTasks(Instant.now(), taskJobConfig.checkBatchSize());
        if (dueTasks.isEmpty()) {
            return;
        }

        LOGGER.info("Found {} due task job(s).", dueTasks.size());

        for (TaskJob taskJob : dueTasks) {
            Optional<String> lockValue =
                    jobLockerDao.tryLock(taskJob.id(), taskJobConfig.lockTimeout());
            if (lockValue.isEmpty()) {
                continue;
            }

            try {
                RocketMessage<String> message = RocketMessage.of(taskJob.payload(), taskJob.id());

                taskMessagePublisher.publish(message);

                boolean markedTriggered = taskJobService.markTriggered(taskJob.id());
                if (markedTriggered) {
                    LOGGER.info("Task job {} marked as triggered.", taskJob.id());
                } else {
                    LOGGER.warn(
                            "Task job {} was published but not marked triggered because it is no longer pending.",
                            taskJob.id());
                }
            } catch (Exception exception) {
                LOGGER.warn("Failed to trigger task job {}", taskJob.id(), exception);
            } finally {
                boolean unlocked = jobLockerDao.unlock(taskJob.id(), lockValue.get());
                if (!unlocked) {
                    LOGGER.warn("Failed to unlock task job {} because lock value did not match.",
                            taskJob.id());
                }
            }
        }
    }
}
