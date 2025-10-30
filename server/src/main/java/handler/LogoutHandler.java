package handler;

import io.javalin.http.Context;
import service.LogoutService;
import service.Message;

public class LogoutHandler {
    private final LogoutService service = new LogoutService();

    public void logout(Context ctx) throws Exception {
        String token = ctx.header("authorization");
        service.logout(token);
        ctx.status(200);
    }
}
