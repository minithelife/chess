package handler;

import handler.exceptions.BadRequestException;
import handler.exceptions.ForbiddenException;
import handler.exceptions.UnauthorizedException;
import io.javalin.Javalin;

public class ExceptionHandler {

    public void register(Javalin app) {
        app.exception(BadRequestException.class, (e, ctx) -> {
            ctx.json(new Message("Error: " + e.getMessage()));
            ctx.status(400);
        });

        app.exception(UnauthorizedException.class, (e, ctx) -> {
            ctx.json(new Message("Error: " + e.getMessage()));
            ctx.status(401);
        });

        app.exception(ForbiddenException.class, (e, ctx) -> {
            ctx.json(new Message("Error: " + e.getMessage()));
            ctx.status(403);
        });

        app.exception(Exception.class, (e, ctx) -> {
            System.out.println("exception500 handled");
            ctx.json(new Message("Error: " + e.getMessage()));
            ctx.status(500);
        });
    }
}
