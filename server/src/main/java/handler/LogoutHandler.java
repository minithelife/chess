package handler;

import io.javalin.http.Context;
import service.LogoutService;

public class LogoutHandler {
    private final LogoutService service = new LogoutService();

    public void logout(Context ctx) {
        String token = ctx.header("authorization");
        service.logout(token); // may throw UnauthorizedException
        ctx.status(200);
    }
}
