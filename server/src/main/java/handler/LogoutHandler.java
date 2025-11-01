package handler;

import handler.exceptions.UnauthorizedException;
import io.javalin.http.Context;
import service.LogoutService;

public class LogoutHandler {

    private final LogoutService service;

    public LogoutHandler(LogoutService service) {
        this.service = service;
    }

    public void logout(Context ctx) throws UnauthorizedException {
        String token = ctx.header("authorization");
        service.logout(token);
        ctx.status(200);
    }
}
