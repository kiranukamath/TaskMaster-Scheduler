package com.kiran.taskmaster.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.kiran.taskmaster.model.Task;

@Component
public class StressTestRunner implements CommandLineRunner {
    private final TaskService taskService;
    private final TaskScheduler taskScheduler;

    public StressTestRunner(TaskService taskService, TaskScheduler taskScheduler) {
        this.taskService = taskService;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void run(String... args) throws Exception {
        Random random = new Random();
        List<Task> tasks = new ArrayList<>();

        // Generate 100 tasks
        for (int i = 1; i <= 100; i++) {
            List<Integer> dependencies = new ArrayList<>();
            if (i > 1) {
                // Randomly depend on up to 3 previous tasks
                for (int j = Math.max(1, i - 3); j < i; j++) {
                    if (random.nextBoolean()) {
                        dependencies.add(j);
                    }
                }
            }
            tasks.add(new Task(
                i,
                random.nextInt(10) + 1, // Priority 1-10
                random.nextInt(1000) + 100, // 100-1100 ms execution time
                dependencies,
                random.nextInt(20) + 1 // 1-20 resource units
            ));
        }

        // Add tasks
        tasks.forEach(taskService::addTask);

        // Start scheduler
        taskScheduler.start();
    }
}