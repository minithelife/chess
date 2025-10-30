package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import service.LoginService;
import service.Message;

public class LoginHandler {

    private final Gson gson = new Gson();
    private final LoginService service = new LoginService();

    public void login(Context ctx) throws Exception {
        UserData request = gson.fromJson(ctx.body(), UserData.class); // reuse UserData for username/password
        AuthData auth = service.login(request.username(), request.password());
        ctx.status(200);
        ctx.json(auth);
    }
}
