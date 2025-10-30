package handler;

import io.javalin.http.Context;
import service.Message;

public class ExceptionHandler {
    public void exception500(Exception e, Context ctx){
        System.out.println("exception500 handled");
        ctx.json(new Message("Error: " + e.getMessage()));
        ctx.status(500);
    }
    //make different exceptions
}
