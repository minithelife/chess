//package websocket;
//
//import chess.ChessMove;
//import chess.ChessPosition;
//import com.google.gson.Gson;
//import io.javalin.websocket.WsConfig;
//import io.javalin.websocket.WsContext;
//import model.GameData;
//import service.GameService;
//import websocket.messages.*;
//
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * WebSocket Handler for chess
// */
//public class ChessWebSocketHandler {
//
//    private final GameService gameService;
//    private final Gson gson = new Gson();
//    private final Set<WsContext> sessions = ConcurrentHashMap.newKeySet();
//
//    public ChessWebSocketHandler(GameService gameService) {
//        this.gameService = gameService;
//    }
//
//    public void configure(WsConfig ws) {
//
//        // ----- CONNECT -----
//        ws.onConnect(ctx -> {
//            sessions.add(ctx);
//            System.out.println("Client connected: " + ctx.sessionId());
//
//            // Client expects JSON, not text
//            //ctx.send(gson.toJson(new NotificationMessage("Connected to Chess WebSocket")));
//        });
//
//        // ----- MESSAGE -----
//        ws.onMessage(ctx -> {
//
//            String msg = ctx.message();
//
//            ClientMessage input;
//
//            // Safely decode JSON — ignore invalid
//            try {
//                input = gson.fromJson(msg, ClientMessage.class);
//            } catch (Exception e) {
//                ctx.send(gson.toJson(new ErrorMessage("Invalid message JSON")));
//                return;
//            }
//
//            // Null check avoids NPE crash
//            if (input == null || input.type == null) {
//                ctx.send(gson.toJson(new ErrorMessage("Missing type field")));
//                return;
//            }
//
//            switch (input.type) {
//                case "join" -> joinGame(input, ctx);
//                case "move" -> makeMove(input, ctx);
//                default ->
//                        ctx.send(gson.toJson(new ErrorMessage("Unknown command: " + input.type)));
//            }
//        });
//
//        // ----- CLOSE -----
//        ws.onClose(ctx -> {
//            sessions.remove(ctx);
//            System.out.println("Client disconnected: " + ctx.sessionId());
//        });
//
//        // ----- ERROR -----
//        ws.onError(ctx ->
//                System.out.println("WebSocket error: " + ctx.error())
//        );
//    }
//
//    // ============================================================
//    // JOIN GAME
//    // ============================================================
//
////    private void joinGame(ClientMessage msg, WsContext ctx) {
////        try {
////            // gameService.joinGame returns void
////            GameData data = gameService.joinGame(msg.authToken, msg.gameId, msg.playerColor);
////
////            // Send initial board
////            ctx.send(gson.toJson(new LoadGameMessage(data)));
////
////            // Notify other players
////            broadcastExcept(ctx, gson.toJson(new NotificationMessage(
////                    msg.playerColor + " joined game " + msg.gameId
////            )));
////
////        } catch (Exception e) {
////            ctx.send(gson.toJson(new ErrorMessage("Join failed: " + e.getMessage())));
////        }
////    }
//private void joinGame(ClientMessage msg, WsContext ctx) {
//    try {
//        System.out.println("Trying to join game: auth=" + msg.authToken +
//                ", gameId=" + msg.gameId +
//                ", color=" + msg.playerColor);
//
//        GameData data = gameService.joinGame(msg.authToken, msg.gameId, msg.playerColor);
//
//        ctx.send(gson.toJson(new LoadGameMessage(data)));
//        broadcastExcept(ctx, gson.toJson(new NotificationMessage(
//                msg.playerColor + " joined game " + msg.gameId
//        )));
//    } catch (Exception e) {
//        System.out.println("Join failed: " + e.getMessage());
//        ctx.send(gson.toJson(new ErrorMessage("Join failed: " + e.getMessage())));
//    }
//}
//
//
//    // ============================================================
//    // MAKE MOVE
//    // ============================================================
//
//    private void makeMove(ClientMessage msg, WsContext ctx) {
//        try {
//            // parse "e2 e4"
//            String[] parts = msg.move.split(" ");
//            if (parts.length != 2) throw new IllegalArgumentException("Expected 'e2 e4'");
//
//            ChessMove move = parseMove(parts[0], parts[1]);
//
//            GameData data = gameService.makeMove(msg.gameId, msg.authToken, move);
//
//            // Send updated board to this client
//            ctx.send(gson.toJson(new LoadGameMessage(data)));
//
//            // Notify other players
//            broadcastExcept(ctx, gson.toJson(new NotificationMessage(
//                    "Move played in game " + msg.gameId + ": " + msg.move
//            )));
//
//        } catch (Exception e) {
//            ctx.send(gson.toJson(new ErrorMessage("Move failed: " + e.getMessage())));
//        }
//    }
//
//    private ChessMove parseMove(String s, String e) {
//        int sc = s.charAt(0) - 'a' + 1;
//        int sr = Integer.parseInt(s.substring(1));
//        int ec = e.charAt(0) - 'a' + 1;
//        int er = Integer.parseInt(e.substring(1));
//
//        return new ChessMove(
//                new ChessPosition(sr, sc),
//                new ChessPosition(er, ec),
//                null
//        );
//    }
//
//    // ============================================================
//    // BROADCAST (JSON only)
//    // ============================================================
//
//    private void broadcastExcept(WsContext origin, String json) {
//        for (WsContext s : sessions) {
//            if (!s.equals(origin)) {
//                s.send(json);
//            }
//        }
//    }
//
//    // ============================================================
//    // MODEL
//    // ============================================================
//
//    private static class ClientMessage {
//        String type;
//        String authToken;
//        Integer gameId;
//        String playerColor;
//        String move;
//    }
//}
package websocket;

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

public class ChessWebSocketHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();
    private final ConnectionManager connections = new ConnectionManager();

    public ChessWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
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
                if(cmd.getCommandType() == UserGameCommand.CommandType.MAKE_MOVE){
                    cmd = gson.fromJson(ctx.message(), UserMoveCommand.class);
                }
                handle(cmd, ctx);
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
    private void handleConnect(UserGameCommand cmd, WsContext ctx) {
        try {

            GameData game = gameService.getGame(cmd.getAuthToken(), cmd.getGameID());
            //--> 1. if there is no game, send an error.
            //

            // join websocket room
            connections.add(cmd.getGameID(), ctx);

            // send LOAD_GAME to sender
            var load = new LoadGameMessage(game);
            connections.send(ctx, gson.toJson(load));

            // broadcast NOTIFICATION to others
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
            if (cmd instanceof UserMoveCommand) {
                UserMoveCommand userMoveCommand = (UserMoveCommand) cmd;
                userMoveCommand.getMove();

                ChessMove move = userMoveCommand.getMove(); // Test framework inserts this

            GameData updated = gameService.makeMove(cmd.getGameID(), cmd.getAuthToken(), move);

            // Send LOAD_GAME to sender
            connections.send(ctx, gson.toJson(new LoadGameMessage(updated)));

            // Notify others (LOAD_GAME + NOTIFICATION)
            connections.broadcast(cmd.getGameID(), ctx,
                    gson.toJson(new LoadGameMessage(updated)));
            connections.broadcast(cmd.getGameID(), ctx,
                    gson.toJson(new NotificationMessage("Move played")));
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
            String username = gameService.leaveGame(cmd.getAuthToken(), cmd.getGameID());

            connections.remove(ctx);

            var msg = new NotificationMessage(username + " left the game");
            connections.broadcast(cmd.getGameID(), ctx, gson.toJson(msg));

        } catch (Exception e) {
            sendError(ctx, e.getMessage());
        }
    }

    // --------------------------------------------------------
    // ERROR HELPER
    // --------------------------------------------------------
    private void sendError(WsContext ctx, String msg) {
        connections.send(ctx, gson.toJson(new ErrorMessage(msg)));
    }
}
