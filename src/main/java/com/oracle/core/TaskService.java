package com.oracle.core;

import com.google.common.collect.ImmutableCollection;

import java.time.LocalDate;
import java.util.Optional;

public interface TaskService {
    Task createTask(String description, LocalDate date);
    Optional<Task> getTask(long taskId);
    Optional<Task> updateTask(Task task);
    boolean deleteTask(long id);
    ImmutableCollection<Task> getTasks();
}
