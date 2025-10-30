package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import model.*;
import service.*;


public class RegisterHandler {
    private final Gson gson = new Gson();
    private final RegisterService service = new RegisterService();

    public void register(Context ctx) throws Exception {
        // Parse the request body
        UserData user = gson.fromJson(ctx.body(), UserData.class);

        // Try registering (may throw exceptions handled globally)
        AuthData auth = service.register(user);

        // Return successful response
        ctx.status(200);
        ctx.json(auth);
    }
}
