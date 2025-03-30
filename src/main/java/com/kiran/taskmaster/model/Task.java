package com.kiran.taskmaster.model;

import java.util.List;

public record Task(
    int id,
    int priority,         // 1 = highest, 10 = lowest
    long executionTime,   // in milliseconds
    List<Integer> dependencies,
    int resourceUnits,
    long startTime,       // Nanoseconds
    long endTime,         // Nanoseconds
    int retries           // Number of retry attempts remaining
) {
    public Task(int id, int priority, long executionTime, List<Integer> dependencies, int resourceUnits) {
        this(id, priority, executionTime, dependencies, resourceUnits, 0L, 0L, 3); // Default 3 retries
    }

    public Task withTiming(long startTime, long endTime) {
        return new Task(id, priority, executionTime, dependencies, resourceUnits, startTime, endTime, retries);
    }

    public Task withRetry() {
        return new Task(id, priority, executionTime, dependencies, resourceUnits, startTime, endTime, retries - 1);
    }
}