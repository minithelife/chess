package server;

import io.javalin.*;
import handler.*;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        ClearHandler clear = new ClearHandler();
        RegisterHandler register = new RegisterHandler();
        // Register your endpoints and exception handlers here.
        ExceptionHandler exceptionHandler = new ExceptionHandler();
        //layout all the exceptions here
        javalin.delete("/db", clear::clear);
        javalin.post("/user", register::register);

        javalin.exception(Exception.class, exceptionHandler::exception500);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
