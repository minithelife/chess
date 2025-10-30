package handler;

import handler.exceptions.BadRequestException;
import handler.exceptions.ForbiddenException;
import handler.exceptions.UnauthorizedException;
import io.javalin.Javalin;

public class ExceptionHandler {

    // Register exceptions with Javalin
    public void register(Javalin app) {
        // 400 Bad Request
        app.exception(BadRequestException.class, (e, ctx) -> {
            ctx.json(new Message("Error: " + e.getMessage()));
            ctx.status(400);
        });

        // 401 Unauthorized
        app.exception(UnauthorizedException.class, (e, ctx) -> {
            ctx.json(new Message("Error: " + e.getMessage()));
            ctx.status(401);
        });

        // 403 Forbidden
        app.exception(ForbiddenException.class, (e, ctx) -> {
            ctx.json(new Message("Error: " + e.getMessage()));
            ctx.status(403);
        });

        // Catch-all: any other exceptions
        app.exception(Exception.class, (e, ctx) -> {
            System.out.println("exception500 handled");
            ctx.json(new Message("Error: " + e.getMessage()));
            ctx.status(500);
        });
    }
}
