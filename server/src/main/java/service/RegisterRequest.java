package service;

// User registration request
public record RegisterRequest(String username, String password, String email) {}