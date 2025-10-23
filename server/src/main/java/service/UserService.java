package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import requests.RegisterRequest;
import requests.LoginRequest;
import results.AuthResult;

import java.util.UUID;

/**
 * Handles user-related operations: registration, login, logout, and authentication.
 */
public class UserService {
    private final DataAccess dao;

    public UserService(DataAccess dao) {
        this.dao = dao;
    }

    public AuthResult register(RegisterRequest req) throws DataAccessException {
        if (req == null || req.username() == null || req.password() == null || req.email() == null)
            throw new BadRequestException("Bad request: missing fields");

        if (dao.userExists(req.username())) throw new ForbiddenException("Username already taken");

        UserData user = new UserData(req.username(), req.password(), req.email());
        dao.createUser(user);

        String token = generateToken();
        AuthData auth = new AuthData(token, req.username());
        dao.createAuth(auth);

        return new AuthResult(req.username(), token);
    }

    public AuthResult login(LoginRequest req) throws DataAccessException {
        if (req == null || req.username() == null || req.password() == null)
            throw new BadRequestException("Bad request: missing fields");

        UserData user = dao.getUser(req.username());
        if (user == null) throw new UnauthorizedException("Invalid username or password");

        if (!user.password().equals(req.password())) throw new UnauthorizedException("Invalid username or password");

        String token = generateToken();
        AuthData auth = new AuthData(token, req.username());
        dao.createAuth(auth);

        return new AuthResult(req.username(), token);
    }

    public void logout(String token) throws DataAccessException {
        if (token == null) throw new UnauthorizedException("No token provided");
        AuthData auth = dao.getAuth(token);
        if (auth == null) throw new UnauthorizedException("Invalid token");
        dao.deleteAuth(token);
    }

    // --- EDITED METHOD: Throws UnauthorizedException if token is invalid or missing. ---
    public String authenticate(String token) throws DataAccessException {
        if (token == null) throw new UnauthorizedException("No token provided");
        AuthData auth = dao.getAuth(token);
        if (auth == null) throw new UnauthorizedException("Invalid token");
        return auth.username();
    }
    // ----------------------------------------------------------------------------------

    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public static class BadRequestException extends DataAccessException {
        public BadRequestException(String msg) { super(msg); }
    }

    public static class UnauthorizedException extends DataAccessException {
        public UnauthorizedException(String msg) { super(msg); }
    }

    public static class ForbiddenException extends DataAccessException {
        public ForbiddenException(String msg) { super(msg); }
    }
}