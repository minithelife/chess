package server;

import com.google.gson.Gson;
import dataaccess.*;
import handler.*;
import service.*;

import io.javalin.Javalin;

public class Server {

    private Javalin app;

    // Run the server on the given port
    public int run(int port) {
        // Initialize DAOs
        AuthDAO authDAO = new InMemoryAuth();
        UserDAO userDAO = new InMemoryUser();
        GameDAO gameDAO = new InMemoryGame();

        // Initialize Services
        ClearService clearService = new ClearService(authDAO, gameDAO, userDAO);
        RegisterService registerService = new RegisterService(userDAO, authDAO);
        LoginService loginService = new LoginService(userDAO, authDAO);
        LogoutService logoutService = new LogoutService(authDAO);
        GameService gameService = new GameService(authDAO, gameDAO, userDAO);

        // Initialize Handlers
        ClearHandler clearHandler = new ClearHandler(clearService);
        RegisterHandler registerHandler = new RegisterHandler(registerService);
        LoginHandler loginHandler = new LoginHandler(loginService);
        LogoutHandler logoutHandler = new LogoutHandler(logoutService);
        GameHandler gameHandler = new GameHandler(gameService);
        ExceptionHandler exceptionHandler = new ExceptionHandler();

        // Create Javalin app
        app = Javalin.create(config -> config.staticFiles.add("web"));

        // Endpoints
        app.delete("/db", clearHandler::clear);
        app.post("/user", registerHandler::register);
        app.post("/session", loginHandler::login);
        app.delete("/session", logoutHandler::logout);
        app.get("/game", gameHandler::listGames);
        app.post("/game", gameHandler::createGame);
        app.put("/game", gameHandler::joinGame);

        // Register global exception handler
        exceptionHandler.register(app);

        System.out.println("Server running on http://localhost:" + port);

        app.start(port);

        return app.port();
    }

    // Stop the server
    public void stop() {
        if (app != null) {
            app.stop();
        }
    }


}
