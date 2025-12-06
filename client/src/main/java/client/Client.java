package client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import model.*;
import model.requests.CreateGameRequest;
import model.requests.JoinGameRequest;
import model.requests.LoginRequest;
import model.requests.RegisterRequest;
import model.responses.ListGamesResponse;
import serverfacade.ServerFacade;
import ui.BoardDrawer;
import ui.EscapeSequences;

import chess.ChessGame;

import java.util.*;

import static ui.EscapeSequences.*;

public class Client {

    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private String authToken = null;
    private final Scanner scanner = new Scanner(System.in);
    private final Gson gson = new Gson();

    // map index -> gameID from last listing
    private final Map<Integer, Integer> lastListed = new HashMap<>();

    public Client(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
    }

    public Client(ServerFacade server) {
        this.server = server;
    }

    public void run() {
        System.out.println( EscapeSequences.ERASE_SCREEN + "♕ 240 Chess Client");
        System.out.print(help());

        String result = "";
        while (!"quit".equals(result)) {
            printPrompt();
            String line = scanner.nextLine().trim();
            try {
                result = eval(line);
                if (!result.isBlank()) {
                    System.out.print(result);
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

        }
        System.out.println("Goodbye.");
    }

    private void printPrompt() {
        System.out.print("\n>>> ");
    }

    public String eval(String input) throws Exception {
        if (input.isBlank()) {
            return "";
        }
        String[] tokens = input.split("\\s+");
        String cmd = tokens[0].toLowerCase();
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch (cmd) {
            case "help" -> help();
            case "quit" -> "quit";
            case "register" -> register(params);
            case "login" -> login(params);
            case "logout" -> logout();
            case "creategame" -> createGame(params);
            case "listgames" -> listGames();
            case "play" -> play(params);
            case "observe" -> observe(params);
            case "redraw" -> redraw();
            default -> "Unknown command. Type 'help'.\n";
        };
    }

    // ----- commands -----
    private String register(String... params) throws Exception {
        if (params.length != 3) {
            return "Usage: register <username> <password> <email>\n";
        }
        RegisterRequest req = new RegisterRequest(params[0], params[1], params[2]);
        AuthData auth = server.register(req);
        this.authToken = auth.authToken();
        this.state = State.SIGNEDIN;
        return "Registered and logged in as " + auth.username() + "\n";
    }

    private String login(String... params) throws Exception {
        if (params.length != 2) {
            return "Usage: login <username> <password>\n";
        }
        LoginRequest req = new LoginRequest(params[0], params[1]);
        AuthData auth = server.login(req);
        this.authToken = auth.authToken();
        this.state = State.SIGNEDIN;
        return "Logged in as " + auth.username() + "\n";
    }

    private String logout() throws Exception {
        assertSignedIn();
        server.logout(authToken);
        authToken = null;
        state = State.SIGNEDOUT;
        lastListed.clear();
        return "Logged out.\n";
    }

    private String createGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length < 1) {
            return "Usage: createGame <name>\n";
        }
        String name = String.join(" ", params);
        var req = new CreateGameRequest(name);
        var res = server.createGame(authToken, req);
        return "Created game: " + name + "\n";
    }

    private String listGames() throws Exception {
        assertSignedIn();
        ListGamesResponse resp = server.listGames(authToken);
        StringBuilder sb = new StringBuilder();
        lastListed.clear();
        int i = 1;
        for (GameData g : resp.games()) {
            lastListed.put(i, g.gameID());
            sb.append(String.format("%d) %s  [White: %s | Black: %s]%n",
                    i, g.gameName(),
                    g.whiteUsername() == null ? "-" : g.whiteUsername(),
                    g.blackUsername() == null ? "-" : g.blackUsername()));
            i++;
        }
        if (sb.isEmpty()) {
            sb.append("No games.\n");
        }
        return sb.toString();
    }

    private String play(String... params) throws Exception {
        assertSignedIn();
        if (params.length < 1) {
            return "Usage: play <list-number> (you will be prompted for color)\n";
        }
        int listNum;
        try { listNum = Integer.parseInt(params[0]); } catch (NumberFormatException e) {
            return "Invalid number. Run listGames first to see indexes.\n";
        }
        if (!lastListed.containsKey(listNum)) {
            return "Unknown game index - run listGames first.\n";
        }
        int gameId = lastListed.get(listNum);
        System.out.print("Color (white/black): ");
        String color = scanner.nextLine().trim().toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return "Invalid color. Choose 'white' or 'black'.\n";
        }
        server.joinGame(authToken, new JoinGameRequest(color, gameId));
        this.state = State.PLAYING;
        // fetch the game to get board and draw
        GameData gd = server.getGame(gameId, authToken);
        drawGame(gd, color.equals("BLACK"));
        return "Joined game as " + color + "\n";
    }
s
    private String observe(String... params) throws Exception {
        assertSignedIn();
        if (params.length != 1) {
            return "Usage: observe <list-number>\n";
        }
        int listNum;
        try { listNum = Integer.parseInt(params[0]); } catch (NumberFormatException e) {
            return "Invalid number. Run listGames first to see indexes.\n";
        }
        if (!lastListed.containsKey(listNum)) {
            return "Unknown game index - run listGames first.\n";
        }
        int gameId = lastListed.get(listNum);
        this.state = State.OBSERVING;
        GameData gd = server.getGame(gameId, authToken);
        drawGame(gd, false); // observers see white perspective
        return "Observing game\n";
    }

    private String redraw() {
        System.out.println("Redraw command only works while observing/playing via play/observe (board drawn after those actions).\n");
        return "";
    }

    // ----- helpers -----
    private void drawGame(GameData g, boolean blackPerspective) {
        if (g == null) {
            System.out.println("No game data to draw.");
            return;
        }
        ChessGame chessGame = g.game();
        if (chessGame == null) {
            // fallback: draw initial board of given perspective
            BoardDrawer.drawInitialBoard(blackPerspective);
            return;
        }
        // the BoardDrawer you provided draws initial board only; for now use drawInitialBoard
        // but if you have a drawBoard(ChessGame,boolean) you can call it here.
        BoardDrawer.drawInitialBoard(blackPerspective);
    }

    private void assertSignedIn() throws Exception {
        if (state == State.SIGNEDOUT || authToken == null) {
            throw new Exception("You must be logged in.");
        }
    }

    private String help() {
        if (state== State.SIGNEDOUT || authToken == null) {
            return """
                Commands:
                - register <username> <password> <email>
                - login <username> <password>
                - quit
                """;
        }
        return """
                Commands:
                - logout
                - createGame <name>
                - listGames
                - play <list-number>
                - observe <list-number>
                - redraw
                - quit
                """;
    }
}
