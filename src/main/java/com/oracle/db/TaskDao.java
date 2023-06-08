package com.oracle.db;

import com.google.common.collect.ImmutableCollection;
import com.oracle.core.Task;

import java.util.Optional;

public interface TaskDao {

    /**
     * Insert a new task.
     * @param task Task to insert. Id field will be ignored.
     * @return A copy of the supplied task with a valid unique id.
     */
    Task insert(Task task);

    Optional<Task> find(long taskId);

    /**
     * Update existing task by id
     * @return The updated task, or an empty optional if no task exists for this id.
     */
    Optional<Task> update(Task task);

    /**
     * Delete a task by id
     * @return true if the task was deleted, false if no task exists for this id.
     */
    boolean delete(long id);

    ImmutableCollection<Task> findAll();
}
