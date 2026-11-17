package owewell.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import owewell.application.ConcurrentUpdateException;
import owewell.application.GroupNotFoundException;
import owewell.application.GroupNotSettledException;
import owewell.application.InviteExpiredException;
import owewell.application.InviteNotFoundException;
import owewell.application.NotAGroupMemberException;

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

    @ExceptionHandler(NotAGroupMemberException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError forbidden(NotAGroupMemberException e) {
        return new ApiError(e.getMessage());
    }

    @ExceptionHandler(InviteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError inviteNotFound(InviteNotFoundException e) {
        return new ApiError(e.getMessage());
    }

    @ExceptionHandler(InviteExpiredException.class)
    @ResponseStatus(HttpStatus.GONE)
    public ApiError inviteExpired(InviteExpiredException e) {
        return new ApiError(e.getMessage());
    }

    @ExceptionHandler(GroupNotSettledException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError groupNotSettled(GroupNotSettledException e) {
        return new ApiError(e.getMessage());
    }
}
