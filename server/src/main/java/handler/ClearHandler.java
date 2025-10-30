package handler;

import io.javalin.http.Context;
import service.*;


public class ClearHandler{
    public void clear(Context ctx) throws Exception {
        //System.out.println("called Clear");
        //error handling and call to service
        ClearService service = new ClearService();

        //throw new Exception("does not work");

        service.clear();
        ctx.status(200);

    }
}