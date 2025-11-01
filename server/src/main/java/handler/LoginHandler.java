package handler;

import com.google.gson.Gson;
import handler.exceptions.BadRequestException;
import handler.exceptions.UnauthorizedException;
import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import service.LoginService;

public class LoginHandler {

    private final Gson gson = new Gson();
    private final LoginService service;

    public LoginHandler(LoginService service) {
        this.service = service;
    }

    public void login(Context ctx) throws UnauthorizedException, BadRequestException {
        UserData request = gson.fromJson(ctx.body(), UserData.class);
        AuthData auth = service.login(request.username(), request.password());
        ctx.status(200);
        ctx.result(gson.toJson(auth));
    }
}
