package server;

import io.javalin.Javalin;
import handler.*;
import handler.exceptions.*;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Handlers
        ClearHandler clear = new ClearHandler();
        RegisterHandler register = new RegisterHandler();
        LoginHandler login = new LoginHandler();
        LogoutHandler logout = new LogoutHandler();
        GameHandler game = new GameHandler();

        // Register endpoints
        javalin.delete("/db", clear::clear);
        javalin.post("/user", register::register);
        javalin.post("/session", login::login);
        javalin.delete("/session", logout::logout);
        javalin.get("/game", game::listGames);
        javalin.post("/game", game::createGame);
        javalin.put("/game", game::joinGame);

        // Register exception handling
        ExceptionHandler exceptionHandler = new ExceptionHandler();
        exceptionHandler.register(javalin); // handles 400, 401, 403, and 500
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
