package owewell.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import owewell.application.ConcurrentUpdateException;
import owewell.application.GroupNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(GroupNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(GroupNotFoundException e) {
        return new ApiError(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequest(IllegalArgumentException e) {
        return new ApiError(e.getMessage());
    }

    @ExceptionHandler(ConcurrentUpdateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflict(ConcurrentUpdateException e) {
        return new ApiError(e.getMessage());
    }
}
