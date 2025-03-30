package com.kiran.taskmaster.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.springframework.stereotype.Service;

import com.kiran.taskmaster.model.Task;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TaskService {
    private final Map<Integer, Task> taskMap = new HashMap<>();
    private final Map<Integer, List<Integer>> dependencyGraph = new HashMap<>();
    private final PriorityQueue<Task> readyQueue = new PriorityQueue<>(
            Comparator.comparingInt(Task::priority)
    );

    private final List<Task> completedTasks = Collections.synchronizedList(new ArrayList<>()); // Thread-safe

    public PriorityQueue<Task> getReadyQueue() {
        return readyQueue;
    }

    public void addTask(Task task) {
        taskMap.put(task.id(), task);
        dependencyGraph.put(task.id(), new ArrayList<>());

        for (int dep : task.dependencies()) {
            dependencyGraph.computeIfAbsent(dep, k -> new ArrayList<>()).add(task.id());
        }
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }

    public Map<Integer, List<Integer>> getDependencyGraph() {
        return Collections.unmodifiableMap(dependencyGraph);
    }

    public List<Integer> topologicalSort() throws IllegalStateException {
        Map<Integer, Integer> inDegree = new HashMap<>();
        for (Task task : taskMap.values()) {
            inDegree.putIfAbsent(task.id(), 0);
            for (int dep : task.dependencies()) {
                inDegree.merge(task.id(), 1, Integer::sum);
            }
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int id : taskMap.keySet()) {
            if (inDegree.getOrDefault(id, 0) == 0) {
                queue.add(id);
            }
        }

        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int current = queue.poll();
            order.add(current);
            for (int dependent : dependencyGraph.getOrDefault(current, List.of())) {
                inDegree.merge(dependent, -1, Integer::sum);
                if (inDegree.get(dependent) == 0) {
                    queue.add(dependent);
                }
            }
        }

        if (order.size() != taskMap.size()) {
            throw new IllegalStateException("Circular dependency detected");
        }
        return order;
    }

    public void enqueueReadyQueues(){
        List<Integer> order = topologicalSort();
        for(int id : order){
            readyQueue.add(taskMap.get(id));
        }
    }

    public void completeTask(Task task, long startTime, long endTime) {
        completedTasks.add(task.withTiming(startTime, endTime));
    }

    public List<Task> getCompletedTasks() {
        return new ArrayList<>(completedTasks);
    }

    public double getAverageExecutionTime() {
        if (completedTasks.isEmpty()) return 0.0;
        return completedTasks.stream()
            .mapToLong(t -> t.endTime() - t.startTime())
            .average()
            .orElse(0.0) / 1_000_000; // Convert to milliseconds
    }

    public long getThroughput() {
        long totalTime = completedTasks.stream()
            .mapToLong(Task::endTime)
            .max()
            .orElse(0L) - completedTasks.stream()
            .mapToLong(Task::startTime)
            .min()
            .orElse(0L);
        return totalTime == 0 ? 0 : (completedTasks.size() * 1_000_000_000L) / totalTime; // Tasks per second
    }

    public double getAverageWaitTime() {
        return completedTasks.isEmpty() ? 0.0 : completedTasks.stream()
            .mapToLong(t -> t.startTime() - taskMap.get(t.id()).startTime()) // Assuming initial enqueue time is 0
            .average()
            .orElse(0.0) / 1_000_000; // ms
    }

    public double getResourceUtilization(int totalResources) {
        long totalTaskTime = completedTasks.stream()
            .mapToLong(t -> t.endTime() - t.startTime())
            .sum();
        long totalRuntime = completedTasks.stream().mapToLong(Task::endTime).max().orElse(0L) -
                            completedTasks.stream().mapToLong(Task::startTime).min().orElse(0L);
        return totalRuntime == 0 ? 0.0 : (double) totalTaskTime * totalResources / (totalRuntime * totalResources);
    }
    
}
