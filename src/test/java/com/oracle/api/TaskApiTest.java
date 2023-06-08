package com.oracle.api;

import com.oracle.TaskerApplication;
import com.oracle.TaskerConfiguration;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;

import static jakarta.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class TaskApiTest {
    private static final DropwizardAppExtension<TaskerConfiguration> app = new DropwizardAppExtension<>(
            TaskerApplication.class,
            "config_test.yml"
    );

    @Test
    public void testCreateReadUpdateDelete() {
        // create
        var desc = "task description";
        var date = "2025-01-02";
        TaskDto created = app.client().target(baseUri() + "/task")
                .request()
                .post(json(new CreateTaskDto(desc, date)), TaskDto.class);
        assertThat(created.id()).isGreaterThan(0);
        assertThat(created.description()).isEqualTo(desc);
        assertThat(created.date()).isEqualTo(LocalDate.parse(date));
        assertThat(created.completed()).isFalse();

        // read
        TaskDto read = app.client().target(baseUri() + "/task/" + created.id())
                .request()
                .get(TaskDto.class);
        assertThat(read.id()).isEqualTo(created.id());
        assertThat(read.description()).isEqualTo(created.description());
        assertThat(read.date()).isEqualTo(created.date());
        assertThat(read.completed()).isEqualTo(created.completed());

        // update
        var desc2 = "task description 2";
        var date2 = "2026-12-25";
        TaskDto updated = app.client().target(baseUri() + "/task/" + created.id())
                .request()
                .put(json(new UpdateTaskDto(desc2, date2, true)), TaskDto.class);
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.description()).isEqualTo(desc2);
        assertThat(updated.date()).isEqualTo(date2);
        assertThat(updated.completed()).isTrue();

        // delete
        Response deleted = app.client().target(baseUri() + "/task/" + updated.id())
                .request()
                .delete();
        assertThat(deleted.getStatus()).isEqualTo(200);
    }

    @Test
    public void testList() {
        var numTasks = 10;
        for (var i = 0; i < numTasks; i++) {
            app.client().target(baseUri() + "/task")
                    .request()
                    .post(json(new CreateTaskDto(String.valueOf(i), "1970-01-01"))).close();
        }

        var tasks = app.client().target(baseUri() + "/task")
                .request()
                .get(TaskListDto.class);
        assertThat(tasks.tasks()).hasSize(numTasks);
    }

    @Test
    public void testGetNonExistent() {
        var response = app.client().target(baseUri() + "/task/50")
                .request()
                .get(Response.class);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testUpdateNonExistent() {
        var response = app.client().target(baseUri() + "/task/100")
                .request()
                .put(json(new UpdateTaskDto("desc", "1970-01-01", true)), Response.class);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDeleteNonExistent() {
        var response = app.client().target(baseUri() + "/task/150")
                .request()
                .delete();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    private String baseUri() {
        return "http://localhost:" + app.getLocalPort() + "";
    }
}
