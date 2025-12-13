package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import model.GameData;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.commands.UserMoveCommand;
import websocket.messages.*;

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

                // 1️⃣ Highlight requests have "positions" — handle first
                if (json.has("positions")) {
                    HighlightRequest highlight = gson.fromJson(ctx.message(), HighlightRequest.class);
                    handleHighlight(highlight, ctx);

                    // 2️⃣ Normal game commands have "commandType"
                } else if (json.has("commandType")) {
                    UserGameCommand cmd = gson.fromJson(ctx.message(), UserGameCommand.class);

                    // If it's a MAKE_MOVE, parse fully as UserMoveCommand
                    if (cmd.getCommandType() == UserGameCommand.CommandType.MAKE_MOVE) {
                        cmd = gson.fromJson(ctx.message(), UserMoveCommand.class);
                    }

                    handle(cmd, ctx);

                    // 3️⃣ Anything else is invalid
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
    private void handleHighlight(HighlightRequest req, WsContext ctx) {
        try {
            // Just send the highlight back to the same client
            connections.send(ctx, gson.toJson(req));
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

            // join websocket room
            connections.add(cmd.getGameID(), ctx);

            // send LOAD_GAME to root client
            connections.send(ctx, gson.toJson(new LoadGameMessage(game)));

            // broadcast NOTIFICATION to all other clients
            String username = gameService.getUsernameForAuth(cmd.getAuthToken());
            String role = (game.whiteUsername().equals(username)) ? "WHITE" :
                    (game.blackUsername().equals(username)) ? "BLACK" : "observer";

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

            // Apply move
            GameData updated = gameService.makeMove(cmd.getGameID(), cmd.getAuthToken(), move);

            // Broadcast updated board to all clients (including root)
            connections.broadcast(cmd.getGameID(), gson.toJson(new LoadGameMessage(updated)));

            // Notify other clients of the move (exclude root)
            String username = gameService.getUsernameForAuth(cmd.getAuthToken());
            connections.broadcastExcluding(cmd.getGameID(), ctx,
                    gson.toJson(new NotificationMessage(username + " played: " + move)));

            ChessGame chess = updated.chessGame();
            ChessGame.TeamColor nextTurn = chess.getTeamTurn();

            // Notify check
            if (chess.isInCheck(nextTurn) && !updated.isGameOver()) {
                String playerInCheck = (nextTurn == ChessGame.TeamColor.WHITE)
                        ? updated.whiteUsername()
                        : updated.blackUsername();
                connections.broadcast(cmd.getGameID(),
                        gson.toJson(new NotificationMessage(playerInCheck + " is in check")));
            }

            // Notify checkmate/game over
            if (updated.isGameOver()) {
                String winnerUsername = updated.getWinnerUsername();
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

            NotificationMessage msg = new NotificationMessage(loser + " resigned");
            connections.broadcast(cmd.getGameID(), gson.toJson(msg));

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

            // Notify other clients only
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
