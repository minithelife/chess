
package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import model.GameData;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.commands.UserMoveCommand;
import websocket.messages.*;

import java.util.List;

public class ChessWebSocketHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();
    private final ConnectionManager connections = new ConnectionManager();

    public ChessWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }
    private void handleHighlight(HighlightRequest req, WsContext ctx) {
        try {
            // Broadcast highlight to all sessions (players + observers)
            for (WsContext s : connections.getSessions(req.getGameID())) {
                connections.send(s, gson.toJson(new HighlightMessage(req.getPositions())));
            }
        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }


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
                UserGameCommand cmd = gson.fromJson(ctx.message(), UserGameCommand.class);

                // Check if it's a move
                if(cmd.getCommandType() == UserGameCommand.CommandType.MAKE_MOVE){
                    cmd = gson.fromJson(ctx.message(), UserMoveCommand.class);
                    handle(cmd, ctx);
                }
                // Check if it's a highlight request (not a CommandType)
                else if (ctx.message().contains("highlightPositions")) {
                    HighlightRequest highlightReq = gson.fromJson(ctx.message(), HighlightRequest.class);
                    handleHighlight(highlightReq, ctx);
                }
                else {
                    handle(cmd, ctx);
                }
            } catch (Exception ex) {
                sendError(ctx, "Invalid JSON");
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
    // CONNECT
    // --------------------------------------------------------
    // --------------------------------------------------------
// CONNECT
// --------------------------------------------------------
    private void handleConnect(UserGameCommand cmd, WsContext ctx) {
        try {
            GameData game = gameService.getGame(cmd.getAuthToken(), cmd.getGameID());

            // join websocket room
            connections.add(cmd.getGameID(), ctx);

            // send LOAD_GAME to sender
            var load = new LoadGameMessage(game);
            connections.send(ctx, gson.toJson(load));

            // broadcast NOTIFICATION to all other sessions
            String username = gameService.getUsernameForAuth(cmd.getAuthToken());
            var notify = new NotificationMessage(username + " connected");
            connections.broadcast(cmd.getGameID(), ctx, gson.toJson(notify));

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    // --------------------------------------------------------
// MOVE
// --------------------------------------------------------
    private void handleMove(UserGameCommand cmd, WsContext ctx) {
        try {
            if (!(cmd instanceof UserMoveCommand)) {
                sendError(ctx, "Expected a move command");
                return;
            }

            UserMoveCommand userMoveCommand = (UserMoveCommand) cmd;
            ChessMove move = userMoveCommand.getMove();

            // Validate move positions
            if (!isValidPosition(move.getStartPosition()) || !isValidPosition(move.getEndPosition())) {
                sendError(ctx, "Move out of bounds: " + move);
                return;
            }

            // Apply move
            GameData updated = gameService.makeMove(cmd.getGameID(), cmd.getAuthToken(), move);

            // Send board update to all sessions (players + observers)
            connections.broadcast(cmd.getGameID(), null, gson.toJson(new LoadGameMessage(updated)));

            // Notify all players of the move
            String username = gameService.getUsernameForAuth(cmd.getAuthToken());
            String moveDesc = username + " played: " + move;
            connections.broadcast(cmd.getGameID(), null, gson.toJson(new NotificationMessage(moveDesc)));

            // Check for check
            ChessGame chess = updated.chessGame();
            ChessGame.TeamColor nextTurn = chess.getTeamTurn();
            if (chess.isInCheck(nextTurn) && !updated.isCheckmate()) {
                String playerInCheck = (nextTurn == ChessGame.TeamColor.WHITE)
                        ? updated.whiteUsername()
                        : updated.blackUsername();
                connections.broadcast(cmd.getGameID(), null,
                        gson.toJson(new NotificationMessage(playerInCheck + " is in check")));
            }

            // Check for checkmate
            if (updated.isCheckmate()) {
                String winner = updated.getWinner();
                connections.broadcast(cmd.getGameID(), null,
                        gson.toJson(new NotificationMessage("Checkmate! " + winner + " wins!")));
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



            var msg = new NotificationMessage(loser + " resigned");
            String json = gson.toJson(msg);
            connections.send(ctx, json);
            connections.broadcast(cmd.getGameID(), ctx, gson.toJson(msg));

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    // --------------------------------------------------------
    // LEAVE
    // --------------------------------------------------------
    private void handleLeave(UserGameCommand cmd, WsContext ctx) {
        try {
            // Remove connection from manager
            connections.remove(ctx);

            // Determine if leaving player is a regular player or observer
            String username = gameService.leaveGame(cmd.getAuthToken(), cmd.getGameID());

            // Broadcast that someone left
            connections.broadcast(cmd.getGameID(), ctx,
                    gson.toJson(new NotificationMessage(username + " left the game")));

            // Observers who have left should no longer receive updates
            // This is handled by removing them from the ConnectionManager

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    private boolean isValidPosition(chess.ChessPosition pos) {
        int row = pos.getRow();
        int col = pos.getColumn();
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
    // --------------------------------------------------------
    // ERROR HELPER
    // --------------------------------------------------------
    private void sendError(WsContext ctx, String msg) {
        connections.send(ctx, gson.toJson(new ErrorMessage(msg)));
    }
//    private void broadcastHighlight(int gameID, WsContext origin, List<ChessPosition> positions, String username) {
//        var highlightMsg = new HighlightMessage(positions, username);
//        connections.broadcast(gameID, origin, gson.toJson(highlightMsg));
//    }

}
