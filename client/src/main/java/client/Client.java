package client;

import com.google.gson.Gson;
import model.*;
import model.requests.CreateGameRequest;
import model.requests.JoinGameRequest;
import model.requests.LoginRequest;
import model.requests.RegisterRequest;
import model.responses.ListGamesResponse;
import serverfacade.ServerFacade;
import ui.BoardDrawer;
import ui.EscapeSequences;
import websocket.ChessNotificationHandler;
import websocket.ChessWebSocketClient;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;

import java.util.*;

import static ui.BoardDrawer.printColumnLetters;
import static ui.EscapeSequences.*;

public class Client implements ChessNotificationHandler {

    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private String authToken = null;
    private String username = null; // store logged-in username
    private final Scanner scanner = new Scanner(System.in);
    private final Gson gson = new Gson();
    private ChessWebSocketClient wsClient;

    // map index -> gameID from last listing
    private final Map<Integer, Integer> lastListed = new HashMap<>();

    // current game state received via WS (set in onLoadGame)
    private volatile ChessGame currentGame = null;
    private volatile boolean currentBlackPerspective = false;

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
        String[] tokens = input.trim().split("\\s+");

        String cmd = tokens[0].toLowerCase();
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch (cmd) {
            case "help" -> help();
            case "quit", "exit" -> "quit";
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
        this.username = auth.username(); // store username
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
        this.username = auth.username(); // store username
        this.state = State.SIGNEDIN;
        return "Logged in as " + auth.username() + "\n";
    }

    private String logout() throws Exception {
        assertSignedIn();
        server.logout(authToken);
        authToken = null;
        username = null;
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
        try {
            listNum = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return "Invalid number. Run listGames first to see indexes.\n";
        }

        if (!lastListed.containsKey(listNum)) {
            return "Unknown game index - run listGames first.\n";
        }

        int gameId = lastListed.get(listNum);

        String color;
        if (System.console() != null) {
            System.out.print("Color (white/black): ");
            color = scanner.nextLine().trim().toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                return "Invalid color. Choose 'white' or 'black'.\n";
            }
        } else {
            color = "WHITE";
        }

        server.joinGame(authToken, new JoinGameRequest(color, gameId));
        String wsUrl = buildWebSocketUrl(server.getServerUrl());

        this.wsClient = new ChessWebSocketClient(wsUrl, authToken, gameId, color, this);
        this.state = State.PLAYING;

        if (System.console() == null) {
            System.out.println("Connected as " + color + " (test mode).");
            return "";
        }

        System.out.println("Joined game as " + color + ". Type 'help' inside game for commands.\n");
        printPlayHelp();

        while (state == State.PLAYING) {
            System.out.print("\nGame> ");
            String line = scanner.nextLine().trim();
            if (line.isBlank()) continue;

            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            switch (cmd) {
                case "help" -> printPlayHelp();

                case "board" -> {
                    if (currentGame != null) {
                        BoardDrawer.drawBoard(currentGame, currentBlackPerspective);
                    } else {
                        System.out.println("Game not loaded yet.");
                    }
                }

                case "move" -> {
                    if (parts.length != 3) {
                        System.out.println("Usage: move <from> <to>   (ex: move e2 e4)");
                        break;
                    }
                    String fromStr = parts[1].toLowerCase();
                    String toStr = parts[2].toLowerCase();

                    try {
                        int[] from = parseChessPosition(fromStr);
                        int[] to   = parseChessPosition(toStr);
                        wsClient.sendMove(from[0], from[1], to[0], to[1]);
                    } catch (Exception ex) {
                        System.out.println("Invalid move format. Use columns a-h and rows 1-8. Example: move e2 e4");
                    }
                }

                case "resign" -> {
                    System.out.print("Are you sure you want to resign? (yes/no): ");
                    String answer = scanner.nextLine().trim().toLowerCase();
                    if (answer.equals("yes") || answer.equals("y")) {
                        wsClient.resign();
                        System.out.println("You resigned the game.");
                        state = State.SIGNEDIN;
                    } else {
                        System.out.println("Resign canceled. Continue playing.");
                    }
                }

                case "leave" -> {
                    wsClient.leave();
                    System.out.println("You left the game.");
                    state = State.SIGNEDIN;
                }

                case "highlight" -> {
                    if (parts.length != 2) {
                        System.out.println("Usage: highlight <square>   (ex: highlight e2)");
                        break;
                    }
                    if (currentGame == null) {
                        System.out.println("Game not loaded yet.");
                        break;
                    }

                    String sq = parts[1].toLowerCase();
                    try {
                        int col = sq.charAt(0) - 'a' + 1;
                        int row = Integer.parseInt(sq.substring(1));
                        ChessPosition start = new ChessPosition(row, col);

                        Collection<ChessMove> legal = currentGame.validMoves(start);
                        if (legal == null || legal.isEmpty()) {
                            System.out.println("No legal moves for that square.");
                            break;
                        }

                        Set<ChessPosition> highlights = new HashSet<>();
                        highlights.add(start);
                        for (ChessMove m : legal) {
                            highlights.add(m.getEndPosition());
                        }

                        drawBoardWithHighlights(currentGame, currentBlackPerspective, highlights);

                    } catch (Exception ex) {
                        System.out.println("Invalid square. Use columns a-h and rows 1-8. Example: highlight e2");
                    }
                }

                case "quit", "exit" -> {
                    wsClient.leave();
                    System.out.println("Exiting client.");
                    System.exit(0);
                }

                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }

        this.state = State.SIGNEDIN;
        return "";
    }
    private void drawBoardWithHighlights(ChessGame game, boolean blackPerspective, Set<ChessPosition> highlights) {
        if (game == null) {
            System.out.println("No game to draw.");
            return;
        }
        System.out.print(EscapeSequences.ERASE_SCREEN);

        var pieceSymbolAt = (java.util.function.BiFunction<ChessPosition, ChessGame, String>) (pos, g) -> {
            ChessPiece piece = g.getBoard().getPiece(pos);
            if (piece == null) return EscapeSequences.EMPTY;
            switch (piece.getPieceType()) {
                case PAWN:   return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
                case ROOK:   return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
                case KNIGHT: return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
                case BISHOP: return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
                case QUEEN:  return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
                case KING:   return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
                default:     return EscapeSequences.EMPTY;
            }
        };

        var squareBg = (java.util.function.BiFunction<Integer, Integer, String>) (r, c) -> {
            boolean light = ((r + c) % 2 == 1);
            return light ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_DARK_GREY;
        };

        if (!blackPerspective) {
            printColumnLetters(false);
            for (int row = 8; row >= 1; row--) {
                StringBuilder line = new StringBuilder();
                line.append(EscapeSequences.SET_TEXT_COLOR_WHITE).append(row).append(" ").append(EscapeSequences.RESET_TEXT_COLOR);
                for (int col = 1; col <= 8; col++) {
                    ChessPosition pos = new ChessPosition(row, col);
                    String piece = pieceSymbolAt.apply(pos, game);
                    String bg = squareBg.apply(row, col);
                    if (highlights != null && highlights.contains(pos)) bg = EscapeSequences.SET_BG_COLOR_YELLOW;
                    line.append(bg).append(piece).append(EscapeSequences.RESET_BG_COLOR);
                }
                System.out.println(line);
            }
            printColumnLetters(false);
        } else {
            printColumnLetters(true);
            for (int row = 1; row <= 8; row++) {
                StringBuilder line = new StringBuilder();
                line.append(EscapeSequences.SET_TEXT_COLOR_WHITE).append(row).append(" ").append(EscapeSequences.RESET_TEXT_COLOR);
                for (int col = 8; col >= 1; col--) {
                    ChessPosition pos = new ChessPosition(row, col);
                    String piece = pieceSymbolAt.apply(pos, game);
                    String bg = squareBg.apply(row, col);
                    if (highlights != null && highlights.contains(pos)) bg = EscapeSequences.SET_BG_COLOR_YELLOW;
                    line.append(bg).append(piece).append(EscapeSequences.RESET_BG_COLOR);
                }
                System.out.println(line);
            }
            printColumnLetters(true);
        }
    }


    private String buildWebSocketUrl(String baseUrl) {
        if (baseUrl.startsWith("https://")) {
            return "wss://" + baseUrl.substring(8) + "/ws";
        } else if (baseUrl.startsWith("http://")) {
            return "ws://" + baseUrl.substring(7) + "/ws";
        } else {
            return baseUrl + "/ws";
        }
    }

    private void printPlayHelp() {
        System.out.println("""
                In-game commands:
                  help                 Show this help menu
                  board                Redraw the chessboard
                  move <from> <to>     Make a move  (ex: move e2 e4)
                  highlight <square>   Highlight legal moves for that piece
                  resign               Forfeit the game
                  leave                Leave game (back to post-login UI)
                  quit                 Quit entire client
                """);
    }

    private int[] parseChessPosition(String algebraic) {
        algebraic = algebraic.toLowerCase().trim();
        int col = (algebraic.charAt(0) - 'a') + 1;
        int row = (algebraic.charAt(1) - '1') + 1;
        return new int[] { row, col };
    }

    private String observe(String... params) throws Exception {
        assertSignedIn();
        if (params.length != 1) return "Usage: observe <list-number>\n";
        int listNum;
        try { listNum = Integer.parseInt(params[0]); } catch (NumberFormatException e) {
            return "Invalid number. Run listGames first to see indexes.\n";
        }
        if (!lastListed.containsKey(listNum)) return "Unknown game index - run listGames first.\n";

        int gameId = lastListed.get(listNum);
        this.state = State.OBSERVING;
        String wsUrl = buildWebSocketUrl(server.getServerUrl());
        this.wsClient = new ChessWebSocketClient(wsUrl, authToken, gameId, null, this);

        GameData gd = server.getGame(gameId, authToken);
        if (gd != null) {
            ChessGame g = gd.game();
            if (g != null) {
                currentGame = g;
                currentBlackPerspective = false;
                BoardDrawer.drawBoard(currentGame, currentBlackPerspective);
            } else {
                BoardDrawer.drawInitialBoard(false);
            }
        }
        return "Observing game\n";
    }

    private String redraw() {
        if (state != State.PLAYING && state != State.OBSERVING) {
            return "You must be in a game (play/observe) to redraw the board.\n";
        }
        if (wsClient == null) return "No active WebSocket connection.\n";
        wsClient.redraw();
        return "";
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

    @Override
    public void onNotification(String message) {
        if (username != null && (message.contains("in check") || message.contains("checkmate"))) {
            message = username + " " + message;
        }
        System.out.println("\n[NOTIFICATION] " + message);
        printPrompt();
    }

    @Override
    public void onError(String message) {
        System.out.println("\n[ERROR] " + message);
        printPrompt();
    }

    @Override
    public void onLoadGame(ChessGame game, boolean blackPerspective) {
        this.currentGame = game;
        this.currentBlackPerspective = blackPerspective;
        BoardDrawer.drawBoard(game, blackPerspective);
        printPrompt();
    }
}
