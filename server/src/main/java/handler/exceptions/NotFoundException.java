package handler.exceptions;

// 404 Not Found (if needed)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}