package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import model.GameRequest;

import io.javalin.http.Context;

/**
 * Handles user-related operations: registration, login, logout, game creation, and joining games.
 */
public class UserService {
    private final DataAccess dao;

    public UserService(DataAccess dao) {
        this.dao = dao;
    }

    // ---------------- User Operations ----------------

    public void registerUser(Context ctx, UserData request) {
        if (request.username() == null || request.password() == null) {
            ctx.status(400).result("Error: missing username or password");
            return;
        }

        try {
            dao.registerUser(request);
            ctx.status(201).result("User registered successfully");
        } catch (DataAccessException e) {
            ctx.status(500).result("Internal server error");
        }
    }

    public void loginUser(Context ctx, AuthData request) {
        if (request.username() == null || request.password() == null) {
            ctx.status(400).result("Error: missing username or password");
            return;
        }

        try {
            boolean success = dao.loginUser(request);
            if (success) {
                ctx.status(200).result("Login successful");
            } else {
                ctx.status(401).result("Invalid credentials");
            }
        } catch (DataAccessException e) {
            ctx.status(500).result("Internal server error");
        }
    }

    // ---------------- Game Operations ----------------

    public void createGame(Context ctx, GameRequest request) {
        if (request.gameName() == null || request.owner() == null) {
            ctx.status(400).result("Error: missing gameName or owner");
            return;
        }

        try {
            dao.createGame(request);
            ctx.status(201).result("Game created successfully");
        } catch (DataAccessException e) {
            ctx.status(500).result("Internal server error");
        }
    }

    public void joinGame(Context ctx, GameRequest request) {
        if (request.gameID() == null) {
            ctx.status(400).result("Error: missing gameID");
            return;
        }

        if (request.color() == null) {
            ctx.status(400).result("Error: missing color");
            return;
        }

        try {
            boolean success = dao.joinGame(request);
            if (success) {
                ctx.status(200).result("Joined game successfully");
            } else {
                ctx.status(403).result("Unable to join game");
            }
        } catch (DataAccessException e) {
            ctx.status(500).result("Internal server error");
        }
    }
}
