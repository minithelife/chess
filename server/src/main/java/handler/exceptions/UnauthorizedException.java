package handler.exceptions;

// 401 Unauthorized
public class UnauthorizedException extends Exception {
    public UnauthorizedException(String message) { super(message); }
}