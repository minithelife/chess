package client;

import com.google.gson.Gson;
import model.*;
import model.requests.*;
import model.responses.ListGamesResponse;
import serverfacade.ServerFacade;
import ui.BoardDrawer;
import ui.EscapeSequences;
import websocket.ChessNotificationHandler;
import websocket.ChessWebSocketClient;
import chess.*;

import java.util.*;

import static ui.BoardDrawer.printColumnLetters;
import static ui.EscapeSequences.*;

public class Client implements ChessNotificationHandler {

    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private String authToken = null;
    private String username = null;
    private final Scanner scanner = new Scanner(System.in);
    private final Gson gson = new Gson();
    private ChessWebSocketClient wsClient;
    private final Map<Integer, Integer> lastListed = new HashMap<>();
    private volatile ChessGame currentGame = null;
    private volatile boolean currentBlackPerspective = false;

    public Client(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
    }

    public Client(ServerFacade server) {
        this.server = server;
    }

    public void run() {
        System.out.println(ERASE_SCREEN + "♕ 240 Chess Client");
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

    private void printGamePrompt() {
        System.out.print("Game> ");
    }
    public String eval(String input) throws Exception {
        if (input.isBlank()) return "";
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
        if (params.length != 3) return "Usage: register <username> <password> <email>\n";
        RegisterRequest req = new RegisterRequest(params[0], params[1], params[2]);
        AuthData auth = server.register(req);
        this.authToken = auth.authToken();
        this.username = auth.username();
        this.state = State.SIGNEDIN;
        return "Registered and logged in as " + auth.username() + "\n";
    }

    private String login(String... params) throws Exception {
        if (params.length != 2) return "Usage: login <username> <password>\n";
        LoginRequest req = new LoginRequest(params[0], params[1]);
        AuthData auth = server.login(req);
        this.authToken = auth.authToken();
        this.username = auth.username();
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
        if (params.length < 1) return "Usage: createGame <name>\n";
        String name = String.join(" ", params);
        var req = new CreateGameRequest(name);
        server.createGame(authToken, req);
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
        if (sb.isEmpty()) sb.append("No games.\n");
        return sb.toString();
    }

    private String play(String... params) throws Exception {
        assertSignedIn();
        if (params.length < 1) return "Usage: play <list-number> (you will be prompted for color)\n";

        int listNum;
        try { listNum = Integer.parseInt(params[0]); }
        catch (NumberFormatException e) { return "Invalid number. Run listGames first.\n"; }
        if (!lastListed.containsKey(listNum)) return "Unknown game index.\n";

        int gameId = lastListed.get(listNum);

        String color;
        if (System.console() != null) {
            System.out.print("Color (white/black): ");
            color = scanner.nextLine().trim().toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) return "Invalid color.\n";
        } else color = "WHITE";

        server.joinGame(authToken, new JoinGameRequest(color, gameId));
        String wsUrl = buildWebSocketUrl(server.getServerUrl());
        wsClient = new ChessWebSocketClient(wsUrl, authToken, gameId, color, this);
        state = State.PLAYING;

        if (System.console() != null) {
            System.out.println("Joined game as " + color + ". Type 'help' inside game for commands.\n");
            printInGameHelp();
        }
        inGameLoop();
        return "";
    }

    private void inGameLoop() {
        while (state == State.PLAYING || state == State.OBSERVING) {
            System.out.print("\n");
            printGamePrompt();
            String line = scanner.nextLine().trim();
            if (line.isBlank()) continue;

            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            switch (cmd) {
                case "help" -> printInGameHelp();
                case "board" -> {
                    if (currentGame != null) BoardDrawer.drawBoard(currentGame, currentBlackPerspective);
                    else System.out.println("Game not loaded yet.");
                }
                case "move" -> {
                    if (state != State.PLAYING) {
                        System.out.println("Observers cannot move.");
                        break;
                    }
                    if (parts.length != 3) { System.out.println("Usage: move <from> <to>"); break; }
                    try {
                        int[] from = parseChessPosition(parts[1]);
                        int[] to = parseChessPosition(parts[2]);
                        wsClient.sendMove(from[0], from[1], to[0], to[1]);

                        // ---- NEW CHECK/CHECKMATE NOTIFICATION ----
                        ChessGame.TeamColor currentTurn = currentGame.getTeamTurn();
                        ChessGame.TeamColor opponent = currentTurn == ChessGame.TeamColor.WHITE ?
                                ChessGame.TeamColor.BLACK :
                                ChessGame.TeamColor.WHITE;

                        if (currentGame.isInCheck(opponent)) {
                            System.out.println("Opponent's king is in check!");
                        }
                        if (currentGame.isInCheckmate(opponent)) {
                            System.out.println("Checkmate! " + currentTurn + " wins!");
                        }
                        // -----------------------------------------

                    } catch (Exception e) {
                        System.out.println("Invalid move format.");
                    }
                }
                // ... existing code ...
                case "highlight" -> {
                    if (parts.length != 2) { System.out.println("Usage: highlight <square>"); break; }
                    if (currentGame == null) { System.out.println("Game not loaded."); break; }
                    try {
                        int col = parts[1].charAt(0) - 'a' + 1;
                        int row = Integer.parseInt(parts[1].substring(1));
                        ChessPosition start = new ChessPosition(row, col);

                        Collection<ChessMove> legal = currentGame.validMoves(start);
                        if (legal == null || legal.isEmpty()) { System.out.println("No legal moves."); break; }

                        Set<ChessPosition> highlights = new HashSet<>();
                        highlights.add(start);
                        highlights.addAll(legal.stream().map(ChessMove::getEndPosition).toList());

                        // Highlight is a LOCAL operation (per spec). Do not send over WebSocket.
                        BoardDrawer.drawBoardWithHighlights(currentGame, currentBlackPerspective, highlights);
                    } catch (Exception e) {
                        System.out.println("Invalid square. Use a1-h8.");
                    }
                }
// ... existing code ...
                case "resign" -> {
                    if (state != State.PLAYING) { System.out.println("Observers cannot resign."); break; }
                    System.out.print("Are you sure you want to resign? (yes/no): ");
                    String ans = scanner.nextLine().trim().toLowerCase();
                    if (ans.equals("yes") || ans.equals("y")) {
                        wsClient.resign();
                        System.out.println("You resigned.");
                        state = State.SIGNEDIN;
                    }
                }
                case "leave" -> {
                    if (wsClient != null) wsClient.leave();
                    System.out.println("Left game.");
                    state = State.SIGNEDIN;
                }
                case "quit", "exit" -> {
                    if (wsClient != null) wsClient.leave();
                    System.out.println("Exiting client.");
                    System.exit(0);
                }
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }
    }


    private void printInGameHelp() {
        System.out.println("""
                In-game commands:
                  help                 Show this menu
                  board                Redraw the chessboard
                  highlight <square>   Highlight legal moves for that piece
                  leave                Leave game (back to post-login UI)
                  quit                 Quit entire client
                """);
        if (state == State.PLAYING) {
            System.out.println("""
                  move <from> <to>     Make a move (ex: move e2 e4)
                  resign               Forfeit the game
                """);
        }
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
        int listNum = Integer.parseInt(params[0]);
        if (!lastListed.containsKey(listNum)) return "Unknown game index.\n";

        int gameId = lastListed.get(listNum);
        state = State.OBSERVING;

        String wsUrl = buildWebSocketUrl(server.getServerUrl());
        wsClient = new ChessWebSocketClient(wsUrl, authToken, gameId, null, this);

        // IMPORTANT: don't draw here; wait for LOAD_GAME from the server to avoid double-draw interleaving
        inGameLoop();
        return "";
    }

    private String redraw() {
        if ((state != State.PLAYING && state != State.OBSERVING) || wsClient == null) {
            return "No active game to redraw.\n";
        }
        wsClient.redraw();
        return "";
    }

    private void assertSignedIn() throws Exception {
        if (state == State.SIGNEDOUT || authToken == null) throw new Exception("You must be logged in.");
    }

    private String help() {
        if (state == State.SIGNEDOUT || authToken == null) {
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

    private String buildWebSocketUrl(String baseUrl) {
        if (baseUrl.startsWith("https://")) return "wss://" + baseUrl.substring(8) + "/ws";
        if (baseUrl.startsWith("http://")) return "ws://" + baseUrl.substring(7) + "/ws";
        return baseUrl + "/ws";
    }
    private void clearCurrentConsoleLine() {
        System.out.print("\r" + EscapeSequences.ERASE_LINE);
    }

    // ----- Notification handlers -----
    @Override
    public void onNotification(String message) {
        clearCurrentConsoleLine();
        System.out.println("[NOTIFICATION] " + message);

        System.out.print("\n");
        if (state == State.PLAYING || state == State.OBSERVING) {
            printGamePrompt();
        } else {
            printPrompt();
        }
    }

    @Override
    public void onError(String message) {
        clearCurrentConsoleLine();
        System.out.println("[ERROR] " + message);

        System.out.print("\n");
        if (state == State.PLAYING || state == State.OBSERVING) {
            printGamePrompt();
        } else {
            printPrompt();
        }
    }

    @Override
    public void onLoadGame(chess.ChessGame game, boolean blackPerspective) {
        clearCurrentConsoleLine();          // <-- clears "Game> ..." line if it's sitting there
        this.currentGame = game;
        this.currentBlackPerspective = blackPerspective;

        ui.BoardDrawer.drawBoard(game, blackPerspective);

        System.out.print("\n");
        if (state == State.PLAYING || state == State.OBSERVING) {
            printGamePrompt();
        } else {
            printPrompt();
        }
    }

    @Override
    public void onHighlight(Collection<ChessPosition> positions) {
        if (currentGame == null) return;
        drawBoardWithHighlights(currentGame, currentBlackPerspective, new HashSet<>(positions));
        if (state == State.PLAYING || state == State.OBSERVING) {
            printGamePrompt();
        } else {
            printPrompt();
        }
    }

    private void drawBoardWithHighlights(ChessGame game, boolean blackPerspective, Set<ChessPosition> highlights) {
        if (game == null) {
            System.out.println("No game to draw.");
            return;
        }

        System.out.print(EscapeSequences.ERASE_SCREEN);

        // Loop over rows depending on perspective
        for (int row = blackPerspective ? 1 : 8;
             blackPerspective ? row <= 8 : row >= 1;
             row += blackPerspective ? 1 : -1) {

            StringBuilder line = new StringBuilder();
            line.append(EscapeSequences.SET_TEXT_COLOR_WHITE).append(row).append(" ").append(EscapeSequences.RESET_TEXT_COLOR);

            for (int col = blackPerspective ? 8 : 1;
                 blackPerspective ? col >= 1 : col <= 8;
                 col += blackPerspective ? -1 : 1) {

                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = game.getBoard().getPiece(pos);

                // Get the correct symbol for this piece
                String symbol;
                if (piece == null) {
                    symbol = EscapeSequences.EMPTY;
                } else {
                    symbol = switch (piece.getPieceType()) {
                        case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
                        case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
                        case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
                        case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
                        case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
                        case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
                    };
                }

                // Background: normal or highlighted
                String bg = ((row + col) % 2 == 1) ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                if (highlights.contains(pos)) {
                    bg = (state == State.PLAYING) ? EscapeSequences.SET_BG_COLOR_YELLOW : EscapeSequences.SET_BG_COLOR_GREEN;
                }

                line.append(bg).append(symbol).append(EscapeSequences.RESET_BG_COLOR);
            }

            System.out.println(line);
        }

        // Print column letters
        BoardDrawer.printColumnLetters(blackPerspective);
    }


}
