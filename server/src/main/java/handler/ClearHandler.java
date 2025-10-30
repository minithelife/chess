package handler;

import io.javalin.http.Context;
import service.ClearService;

public class ClearHandler {
    private final ClearService service = new ClearService();

    public void clear(Context ctx) {
        service.clear(); // just clears DAO
        ctx.status(200);
    }
}
