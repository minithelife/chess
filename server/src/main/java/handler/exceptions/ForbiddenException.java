package handler.exceptions;

// 403 Forbidden / Already Taken
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}