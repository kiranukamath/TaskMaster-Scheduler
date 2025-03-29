package com.kiran.taskmaster.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiran.taskmaster.model.Task;



@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final List<Task> tasks = new ArrayList<>();
    
    @GetMapping
    public List<Task> getTasks(){
        return tasks;
    }
    
    @PostMapping
    public Task addTask(@RequestBody Task task) {
        tasks.add(task);
        return task;
    }
    
}
