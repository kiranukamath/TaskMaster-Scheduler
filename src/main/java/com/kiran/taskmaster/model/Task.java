package com.kiran.taskmaster.model;

import java.util.List;

public record Task(
    int id,
    int priority,         // 1 = highest, 10 = lowest
    long executionTime,   // in milliseconds
    List<Integer> dependencies, // Task IDs this depends on
    int resourceUnits     // e.g., CPU units required
) {}