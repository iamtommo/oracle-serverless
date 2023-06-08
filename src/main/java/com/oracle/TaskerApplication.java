package com.oracle;

import com.oracle.api.TaskApi;
import com.oracle.core.TaskService;
import com.oracle.core.TaskServiceImpl;
import com.oracle.db.TaskDao;
import com.oracle.db.TaskDaoInMemoryImpl;
import com.oracle.exception.TaskerExceptionMapper;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.inject.Singleton;
import org.glassfish.jersey.internal.inject.AbstractBinder;

public class TaskerApplication extends Application<TaskerConfiguration> {

    public static void main(final String[] args) throws Exception {
        new TaskerApplication().run(args);
    }

    @Override
    public String getName() {
        return "Tasker";
    }

    @Override
    public void initialize(final Bootstrap<TaskerConfiguration> bootstrap) {

    }

    @Override
    public void run(final TaskerConfiguration configuration,
                    final Environment environment) {
        // manually register jersey resources (no autoscanning)
        environment.jersey().register(TaskApi.class);
        environment.jersey().register(TaskerExceptionMapper.class);

        // define DI config (typically would live in another class)
        environment
                .jersey()
                .register(
                        new AbstractBinder() {
                            @Override
                            protected void configure() {
                                bindAsContract(TaskApi.class).in(Singleton.class);
                                bind(TaskServiceImpl.class).to(TaskService.class).in(Singleton.class);
                                bind(TaskDaoInMemoryImpl.class).to(TaskDao.class).in(Singleton.class);
                            }
                        }
                );
    }

}
