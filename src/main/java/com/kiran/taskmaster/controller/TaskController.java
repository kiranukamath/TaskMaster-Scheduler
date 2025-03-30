package com.kiran.taskmaster.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiran.taskmaster.model.Task;
import com.kiran.taskmaster.service.TaskScheduler;
import com.kiran.taskmaster.service.TaskService;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    TaskService taskService;

    @GetMapping
    public List<Task> getTasks() {
        return taskService.getAllTasks();
    }

    @PostMapping
    public Task addTask(@RequestBody Task task) {
        taskService.addTask(task);
        return task;
    }

    @PostMapping("/start")
    public String startScheduler() {
        TaskScheduler scheduler = new TaskScheduler(taskService);
        scheduler.start();
        return "Scheduler started";
    }

    @GetMapping("/metrics")
    public String getMetrics() {
        return String.format(
            "Completed Tasks: %d, Avg Execution Time: %.2f ms, Throughput: %d tasks/sec, " +
            "Avg Wait Time: %.2f ms, Resource Utilization: %.2f%%",
            taskService.getCompletedTasks().size(),
            taskService.getAverageExecutionTime(),
            taskService.getThroughput(),
            taskService.getAverageWaitTime(),
            taskService.getResourceUtilization(100) * 100
        );
    }

}
