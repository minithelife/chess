package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import model.GameData;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.commands.UserMoveCommand;
import websocket.messages.HighlightRequest;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;

import java.lang.reflect.Type;
import java.util.List;

public class ChessWebSocketHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();
    private final ConnectionManager connections = new ConnectionManager();

    public ChessWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    // --------------------------------------------------------
    // CONFIGURE WEBSOCKET
    // --------------------------------------------------------
    public void configure(WsConfig ws) {

        ws.onConnect(ctx -> {
            ctx.enableAutomaticPings();
            System.out.println("WS Connect: " + ctx.sessionId());
        });

        ws.onClose(ctx -> {
            connections.remove(ctx);
            System.out.println("WS Close: " + ctx.sessionId());
        });

        ws.onMessage(ctx -> {
            try {
                JsonObject json = JsonParser.parseString(ctx.message()).getAsJsonObject();

                // Highlight requests
                if (json.has("positions")) {
                    Integer gameID = json.get("gameID").getAsInt();

                    Type listType = new TypeToken<List<ChessPosition>>() {}.getType();
                    List<ChessPosition> positions = gson.fromJson(json.get("positions"), listType);

                    HighlightRequest highlight = new HighlightRequest(gameID, positions);
                    handleHighlight(highlight, ctx);

                    // Game commands
                } else if (json.has("commandType")) {
                    UserGameCommand cmd = gson.fromJson(ctx.message(), UserGameCommand.class);

                    if (cmd.getCommandType() == UserGameCommand.CommandType.MAKE_MOVE) {
                        cmd = gson.fromJson(ctx.message(), UserMoveCommand.class);
                    }

                    handle(cmd, ctx);

                } else {
                    sendError(ctx, "Unknown command");
                }

            } catch (Exception ex) {
                sendError(ctx, "Invalid JSON: " + ex.getMessage());
            }
        });

    }

    // --------------------------------------------------------
    // COMMAND DISPATCH
    // --------------------------------------------------------
    private void handle(UserGameCommand cmd, WsContext ctx) {
        if (cmd == null || cmd.getCommandType() == null) {
            sendError(ctx, "Missing command type");
            return;
        }

        switch (cmd.getCommandType()) {
            case CONNECT -> handleConnect(cmd, ctx);
            case MAKE_MOVE -> handleMove(cmd, ctx);
            case RESIGN -> handleResign(cmd, ctx);
            case LEAVE -> handleLeave(cmd, ctx);
        }
    }

    // --------------------------------------------------------
    // HIGHLIGHT
    // --------------------------------------------------------
    private void handleHighlight(HighlightRequest req, WsContext ctx) {
        try {
            // Get the game; observers do not need auth
            GameData game = gameService.getGame(null, req.getGameID());
            ChessGame chess = game.chessGame();

            if (req.getPositions() == null || req.getPositions().isEmpty()) {
                sendError(ctx, "No position provided");
                return;
            }

            ChessPosition pos = req.getPositions().get(0);

            if (pos.getRow() < 1 || pos.getRow() > 8 || pos.getColumn() < 1 || pos.getColumn() > 8) {
                sendError(ctx, "Position out of bounds");
                return;
            }

            // Compute legal moves for the piece at this position
            var moves = chess.validMoves(pos);

            List<ChessPosition> highlights = moves == null ? List.of() :
                    moves.stream().map(ChessMove::getEndPosition).toList();

            // Send back highlight response
            HighlightRequest response = new HighlightRequest(req.getGameID(), highlights);
            connections.send(ctx, gson.toJson(response));

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    // --------------------------------------------------------
    // CONNECT
    // --------------------------------------------------------
    private void handleConnect(UserGameCommand cmd, WsContext ctx) {
        try {
            GameData game = gameService.getGame(cmd.getAuthToken(), cmd.getGameID());

            connections.add(cmd.getGameID(), ctx);

            connections.send(ctx, gson.toJson(new LoadGameMessage(game)));

            String username = gameService.getUsernameForAuth(cmd.getAuthToken());
            String role;
            if (username.equals(game.whiteUsername())) {
                role = "WHITE";
            } else if (username.equals(game.blackUsername())) {
                role = "BLACK";
            } else {
                role = "observer";
            }

            NotificationMessage notify = new NotificationMessage(username + " connected as " + role);
            connections.broadcastExcluding(cmd.getGameID(), ctx, gson.toJson(notify));

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    // --------------------------------------------------------
    // MAKE_MOVE
    // --------------------------------------------------------
    private void handleMove(UserGameCommand cmd, WsContext ctx) {
        try {
            if (!(cmd instanceof UserMoveCommand)) {
                sendError(ctx, "Expected a move command");
                return;
            }

            UserMoveCommand userMoveCommand = (UserMoveCommand) cmd;
            ChessMove move = userMoveCommand.getMove();

            if (!isValidPosition(move.getStartPosition()) || !isValidPosition(move.getEndPosition())) {
                sendError(ctx, "Move out of bounds: " + move);
                return;
            }

            GameData updated = gameService.makeMove(cmd.getGameID(), cmd.getAuthToken(), move);

            connections.broadcast(cmd.getGameID(), gson.toJson(new LoadGameMessage(updated)));

            String username = gameService.getUsernameForAuth(cmd.getAuthToken());
            connections.broadcastExcluding(cmd.getGameID(), ctx,
                    gson.toJson(new NotificationMessage(username + " played: " + move)));

            ChessGame chess = updated.chessGame();
            ChessGame.TeamColor nextTurn = chess.getTeamTurn();

            if (chess.isInCheck(nextTurn) && !updated.isGameOver()) {
                String playerInCheck = nextTurn == ChessGame.TeamColor.WHITE ?
                        updated.whiteUsername() != null ? updated.whiteUsername() : "WHITE player (not joined)" :
                        updated.blackUsername() != null ? updated.blackUsername() : "BLACK player (not joined)";

                connections.broadcast(cmd.getGameID(),
                        gson.toJson(new NotificationMessage(playerInCheck + " is in check")));
            }

            if (updated.isGameOver()) {
                String winnerUsername = updated.getWinnerUsername();
                if (winnerUsername == null) winnerUsername = "Unknown";
                ChessGame.TeamColor winnerColor = updated.getWinner();
                connections.broadcast(cmd.getGameID(),
                        gson.toJson(new NotificationMessage(
                                "Checkmate! " + winnerUsername + " (" + winnerColor + ") wins!")));
            }

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    // --------------------------------------------------------
    // RESIGN
    // --------------------------------------------------------
    private void handleResign(UserGameCommand cmd, WsContext ctx) {
        try {
            String loser = gameService.resignGame(cmd.getAuthToken(), cmd.getGameID());
            connections.broadcast(cmd.getGameID(), gson.toJson(new NotificationMessage(loser + " resigned")));
        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    // --------------------------------------------------------
    // LEAVE
    // --------------------------------------------------------
    private void handleLeave(UserGameCommand cmd, WsContext ctx) {
        try {
            connections.remove(ctx);

            String username = gameService.leaveGame(cmd.getAuthToken(), cmd.getGameID());

            connections.broadcastExcluding(cmd.getGameID(), ctx,
                    gson.toJson(new NotificationMessage(username + " left the game")));

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    // --------------------------------------------------------
    // HELPERS
    // --------------------------------------------------------
    private boolean isValidPosition(ChessPosition pos) {
        int row = pos.getRow();
        int col = pos.getColumn();
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private void sendError(WsContext ctx, String msg) {
        connections.send(ctx, gson.toJson(new ErrorMessage(msg)));
    }
}
