package handler;

import io.javalin.http.Context;
import service.LogoutService;

public class LogoutHandler {

    private final LogoutService service;

    public LogoutHandler(LogoutService service) {
        this.service = service;
    }

    public void logout(Context ctx) {
        String token = ctx.header("authorization");
        service.logout(token);
        ctx.status(200);
    }
}
