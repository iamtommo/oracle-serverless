package com.oracle.api;

import com.codahale.metrics.annotation.Timed;
import com.oracle.core.Task;
import com.oracle.core.TaskService;
import com.oracle.exception.TaskException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;

@Path("/task")
@Produces(MediaType.APPLICATION_JSON)
public class TaskApi {

    private final TaskService taskService;

    @Inject
    public TaskApi(TaskService taskService) {
        this.taskService = taskService;
    }

    @GET
    @Timed
    public TaskListDto list() {
        return new TaskListDto(taskService.getTasks().stream().map(TaskApi::toDto).toList());
    }

    @POST
    @Timed
    public TaskDto create(CreateTaskDto task) {
        var yyyymmdd = LocalDate.parse(task.date());
        return toDto(taskService.createTask(task.description(), yyyymmdd));
    }

    @Path("/{taskId}")
    @GET
    @Timed
    public TaskDto read(@PathParam("taskId") long taskId) {
        return taskService.getTask(taskId)
                .map(TaskApi::toDto)
                .orElseThrow(() -> new TaskException(404, "Task id not found"));
    }

    @Path("/{taskId}")
    @PUT
    @Timed
    public TaskDto update(@PathParam("taskId") long taskId, UpdateTaskDto task) {
        var yyyymmdd = LocalDate.parse(task.date());
        return taskService.updateTask(new Task(taskId, task.description(), yyyymmdd, task.completed()))
                .map(TaskApi::toDto)
                .orElseThrow(() -> new TaskException(404, "Task id not found"));
    }

    @Path("/{taskId}")
    @DELETE
    @Timed
    public Response delete(@PathParam("taskId") long taskId) {
        return taskService.deleteTask(taskId) ? Response.ok().build() : Response.status(404, "Task id not found").build();
    }

    private static TaskDto toDto(Task task) {
        return new TaskDto(task.id(), task.description(), task.date(), task.completed());
    }
}
