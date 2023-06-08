package com.oracle.db;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.oracle.core.Task;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TaskDaoInMemoryImpl implements TaskDao {

    private final AtomicLong serial = new AtomicLong(1);
    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();

    @Override
    public ImmutableCollection<Task> findAll() {
        return ImmutableList.copyOf(tasks.values());
    }

    public Task insert(Task task) {
        var id = serial.getAndIncrement();
        var insertTask = new Task(id, task.description(), task.date(), task.completed());
        tasks.put(id, insertTask);
        return insertTask;
    }

    @Override
    public Optional<Task> find(long taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    public Optional<Task> update(Task task) {
        return Optional.ofNullable(tasks.computeIfPresent(task.id(), (id, existing) -> new Task(id, task.description(), task.date(), task.completed())));
    }

    @Override
    public boolean delete(long id) {
        return tasks.remove(id) != null;
    }
}
