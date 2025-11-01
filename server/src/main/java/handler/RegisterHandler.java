package handler;

import com.google.gson.Gson;
import handler.exceptions.BadRequestException;
import handler.exceptions.ForbiddenException;
import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import service.RegisterService;

public class RegisterHandler {

    private final Gson gson = new Gson();
    private final RegisterService service;

    public RegisterHandler(RegisterService service) {
        this.service = service;
    }

    public void register(Context ctx) throws ForbiddenException, BadRequestException {
        UserData user = gson.fromJson(ctx.body(), UserData.class);
        AuthData auth = service.register(user);
        ctx.status(200);
        ctx.result(gson.toJson(auth));
    }
}
