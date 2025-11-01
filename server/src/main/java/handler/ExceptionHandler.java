package handler;

import com.google.gson.Gson;
import handler.exceptions.BadRequestException;
import handler.exceptions.ForbiddenException;
import handler.exceptions.UnauthorizedException;
import io.javalin.Javalin;

public class ExceptionHandler {

    private final Gson gson = new Gson();

    public void register(Javalin app) {
        app.exception(Exception.class, (e, ctx) -> {
            ctx.result(gson.toJson(new Message("Error: " + e.getMessage())));
            ctx.status(getStatusCode(e));
        });
    }

    private int getStatusCode(Exception e) {
        return switch(e){
            case BadRequestException ignored -> 400;
            case UnauthorizedException ignored -> 401;
            case ForbiddenException ignored -> 403;
            default -> 500;
        };
    }
}
