package com.oracle.core;

import com.google.common.collect.ImmutableCollection;
import com.oracle.db.TaskDao;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.Optional;

public class TaskServiceImpl implements TaskService {

    private final TaskDao taskDao;

    @Inject
    public TaskServiceImpl(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    @Override
    public Task createTask(String description, LocalDate date) {
        return taskDao.insert(new Task(0, description, date, false));
    }

    @Override
    public Optional<Task> getTask(long taskId) {
        return taskDao.find(taskId);
    }

    @Override
    public Optional<Task> updateTask(Task task) {
        return taskDao.update(task);
    }

    @Override
    public boolean deleteTask(long taskId) {
        return taskDao.delete(taskId);
    }

    @Override
    public ImmutableCollection<Task> getTasks() {
        return taskDao.findAll();
    }
}
