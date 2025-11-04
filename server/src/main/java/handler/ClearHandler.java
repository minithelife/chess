package handler;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.ClearService;

public class ClearHandler {

    private final ClearService service;

    public ClearHandler(ClearService service) {
        this.service = service;
    }

    public void clear(Context ctx) throws DataAccessException {
        service.clear();
        ctx.status(200);
    }
}
