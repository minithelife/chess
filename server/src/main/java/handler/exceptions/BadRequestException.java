package handler.exceptions;

// 400 Bad Request
public class BadRequestException extends Exception {
    public BadRequestException(String message) { super(message); }
}
