package com.kiran.taskmaster.service;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.kiran.taskmaster.model.Task;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TaskScheduler {

    private final TaskService taskService;
    private final ExecutorService executor;
    private final ReentrantLock resourceLock = new ReentrantLock();
    private int availableResources;
    private final Queue<Task> waitingQueue = new LinkedBlockingQueue<>();

    public TaskScheduler(TaskService taskService) {
        this.taskService = taskService;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.availableResources = 100;
    }

    public void start() {
        taskService.enqueueReadyQueues();
        int totalTasks = taskService.getAllTasks().size();
        while(taskService.getCompletedTasks().size() < totalTasks){
            boolean submitted = false;

            Task readyTask = taskService.getReadyQueue().poll();
            if(readyTask != null){
                executor.submit(() -> executeTask(readyTask));
                submitted = true;
            }

            Task waitingTask = waitingQueue.poll();
            if (waitingTask != null) {
                executor.submit(() -> executeTask(waitingTask));
                submitted = true;
            }
            if (!submitted) {
                try {
                    Thread.sleep(50); // Wait for tasks to complete and free resources
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Scheduler interrupted", e);
                    break;
                }
            }
        }
        // executor.shutdown();
        log.info("Scheduler completed all {} tasks", totalTasks);
    }

    private void executeTask(Task task) {
        resourceLock.lock();
        try {
            if (availableResources >= task.resourceUnits()) {
                availableResources -= task.resourceUnits();
                log.info("Starting Task " + task.id() + " with " + task.resourceUnits() + " resources");
            } else {
                log.info("Task " + task.id() + " waiting for resources");
                waitingQueue.offer(task);
                return;
            }
        } finally {
            resourceLock.unlock();
        }

        long startTime = System.nanoTime();
        try {
            Thread.sleep(task.executionTime());
            log.info("Task {} completed", task.id());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Task {} interrupted, retries left: {}", task.id(), task.retries());
            if (task.retries() > 0) {
                taskService.getReadyQueue().offer(task.withRetry());
            } else {
                log.error("Task {} failed after max retries", task.id());
            }
            return;
        } finally {
            long endTime = System.nanoTime();
            resourceLock.lock();
            try {
                availableResources += task.resourceUnits();
                log.info("Resources released for Task {}, now available: {}",
                        task.id(), availableResources);
            } finally {
                resourceLock.unlock();
            }
            if (endTime > startTime) { // Only record if not interrupted
                taskService.completeTask(task, startTime, endTime);
            }
        }
    }

    public int getAvailableResources() {
        return availableResources;
    }
}
