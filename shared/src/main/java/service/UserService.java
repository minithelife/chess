package service;

import dataaccess.*;
import model.*;

import java.util.UUID;

public class UserService {
    private final DataAccess dao;

    public UserService(DataAccess dao) {
        this.dao = dao;
    }

    public AuthResult register(RegisterRequest req) throws DataAccessException {
        if (req == null || req.username == null || req.password == null || req.email == null)
            throw new BadRequestException("bad request");

        if (dao.userExists(req.username)) throw new ForbiddenException("already taken");

        var u = new UserData(req.username, req.password, req.email);
        dao.createUser(u);

        String token = generateToken();
        var auth = new AuthData(token, req.username);
        dao.createAuth(auth);

        return new AuthResult(req.username, token);
    }

    public AuthResult login(LoginRequest req) throws DataAccessException {
        if (req == null || req.username == null || req.password == null)
            throw new BadRequestException("bad request");

        var u = dao.getUser(req.username);
        if (u == null) throw new UnauthorizedException("unauthorized");

        if (!u.password().equals(req.password)) throw new UnauthorizedException("unauthorized");

        String token = generateToken();
        var auth = new AuthData(token, req.username);
        dao.createAuth(auth);

        return new AuthResult(req.username, token);
    }

    public void logout(String token) throws DataAccessException {
        if (token == null) throw new UnauthorizedException("unauthorized");
        var auth = dao.getAuth(token);
        if (auth == null) throw new UnauthorizedException("unauthorized");
        dao.deleteAuth(token);
    }

    public String authenticate(String token) throws DataAccessException {
        if (token == null) return null;
        var auth = dao.getAuth(token);
        return auth == null ? null : auth.username();
    }

    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    // exceptions to let server map to correct status
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
