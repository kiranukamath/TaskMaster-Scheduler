package com.kiran.taskmaster.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kiran.taskmaster.model.Task;

class TaskServiceTest {
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService();
    }

    @Test
    void testTopologicalSort() {
        taskService.addTask(new Task(1, 1, 100, List.of(), 10));
        taskService.addTask(new Task(2, 2, 100, List.of(1), 10));
        taskService.addTask(new Task(3, 3, 100, List.of(1, 2), 10));

        List<Integer> order = taskService.topologicalSort();
        assertEquals(List.of(1, 2, 3), order);
    }

    @Test
    void testCircularDependency() {
        taskService.addTask(new Task(1, 1, 100, List.of(2), 10));
        taskService.addTask(new Task(2, 2, 100, List.of(1), 10));
        assertThrows(IllegalStateException.class, taskService::topologicalSort);
    }
}