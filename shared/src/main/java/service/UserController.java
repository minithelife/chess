package controller;

import io.javalin.Javalin;
import model.AuthData;
import model.UserData;
import model.GameRequest;
import service.UserService;

/**
 * Registers HTTP endpoints and maps them to UserService methods.
 */
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    public void registerRoutes(Javalin app) {
        // User routes
        app.post("/register", ctx -> {
            UserData request = ctx.bodyAsClass(UserData.class);
            service.registerUser(ctx, request);
        });

        app.post("/login", ctx -> {
            AuthData request = ctx.bodyAsClass(AuthData.class);
            service.loginUser(ctx, request);
        });

        // Game routes
        app.post("/createGame", ctx -> {
            GameRequest request = ctx.bodyAsClass(GameRequest.class);
            service.createGame(ctx, request);
        });

        app.post("/joinGame", ctx -> {
            GameRequest request = ctx.bodyAsClass(GameRequest.class);
            service.joinGame(ctx, request);
        });
    }
}
