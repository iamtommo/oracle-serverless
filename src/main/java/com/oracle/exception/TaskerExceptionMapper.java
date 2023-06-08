package com.oracle.exception;

import com.oracle.api.ApiError;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class TaskerExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof TaskException taskException) {
            return Response.status(taskException.statusCode()).entity(new ApiError(taskException.getMessage())).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ApiError("Internal server error")).build();
    }
}