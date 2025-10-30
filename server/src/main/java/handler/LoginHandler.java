package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import service.LoginService;

public class LoginHandler {
    private final Gson gson = new Gson();
    private final LoginService service = new LoginService();

    public void login(Context ctx) {
        UserData request = gson.fromJson(ctx.body(), UserData.class);
        AuthData auth = service.login(request.username(), request.password()); // may throw BadRequestException or UnauthorizedException
        ctx.status(200);
        ctx.json(auth);
    }
}
