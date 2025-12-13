//package websocket;
//
//import chess.ChessMove;
//import chess.ChessPosition;
//import com.google.gson.Gson;
//import chess.ChessGame;
//import model.GameData;
//import ui.BoardDrawer;
//import websocket.commands.UserGameCommand;
//import websocket.commands.UserMoveCommand;
//import websocket.messages.ErrorMessage;
//import websocket.messages.LoadGameMessage;
//import websocket.messages.NotificationMessage;
//import websocket.messages.ServerMessage;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.WebSocket;
//import java.util.concurrent.CompletionStage;
//import java.util.concurrent.CountDownLatch;
//
//
//
//public class ChessWebSocketClient implements WebSocket.Listener {
//
//    private WebSocket webSocket;
//    private final Gson gson = new Gson();
//    private final boolean isPlayer;
//    private final String color;
//    private volatile ChessGame game;
//    private final Integer gameId;
//    private final String authToken;
//    private final ChessNotificationHandler handler;
//
//    private final CountDownLatch openLatch = new CountDownLatch(1);
//
//    public ChessWebSocketClient(String url, String authToken, Integer gameId, String color, ChessNotificationHandler handler) throws Exception {
//        this.authToken = authToken;
//        this.gameId = gameId;
//        this.color = (color != null) ? color.toUpperCase() : null;
//        this.handler = handler;
//        this.isPlayer = this.color != null && !this.color.isEmpty();
//
//        connect(url);
//        openLatch.await(); // wait until WS is connected
//        sendJoinMessage(); // send join only after open
//    }
//
//    private void connect(String url) {
//        webSocket = HttpClient.newHttpClient()
//                .newWebSocketBuilder()
//                .buildAsync(URI.create(url), this)
//                .join();
//    }
//
//    private void sendJoinMessage() {
//        ClientMessage joinMsg = new ClientMessage();
//        joinMsg.type = "join";
//        joinMsg.authToken = authToken;
//        joinMsg.gameId = gameId;
//        joinMsg.playerColor = isPlayer ? color : null;
//
//        webSocket.sendText(gson.toJson(joinMsg), true);
//    }
//
//    // ---------------- WebSocket Listener ----------------
//    @Override
//    public void onOpen(WebSocket webSocket) {
//        System.out.println("Connected to game server.");
//        openLatch.countDown(); // allow join to be sent
//        WebSocket.Listener.super.onOpen(webSocket);
//    }
//
//    @Override
//    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
//        String msgStr = data.toString();
//        ServerMessage msg = gson.fromJson(msgStr, ServerMessage.class);
//
//        switch (msg.getServerMessageType()) {
//            case LOAD_GAME -> {
//                LoadGameMessage load = gson.fromJson(msgStr, LoadGameMessage.class);
//                ChessGame g = load.getGame().game();
//                this.game = g;
//
//                handler.onLoadGame(g, "BLACK".equals(color));
//            }
//            case NOTIFICATION -> {
//                NotificationMessage notif = gson.fromJson(msgStr, NotificationMessage.class);
//                handler.onNotification(notif.getMessage());
//            }
//            case ERROR -> {
//                ErrorMessage err = gson.fromJson(msgStr, ErrorMessage.class);
//                handler.onError(err.errorMessage);
//            }
//        }
//        return WebSocket.Listener.super.onText(ws, data, last);
//    }
//
//
//    @Override
//    public void onError(WebSocket webSocket, Throwable error) {
//        System.out.println("WebSocket error: " + error.getMessage());
//        WebSocket.Listener.super.onError(webSocket, error);
//    }
//
//    // ---------------- Player commands ----------------
//    public void sendMove(int startRow, int startCol, int endRow, int endCol) {
//        if (!isPlayer || game == null) return;
//
//        UserMoveCommand moveCmd = new UserMoveCommand(this.authToken, gameId,
//                new ChessMove(
//                        new ChessPosition(startRow, startCol),
//                        new ChessPosition(endRow, endCol),
//                        null
//                )
//        );
//        webSocket.sendText(gson.toJson(moveCmd), true);
//    }
//
//
//    public void resign() {
//        if (!isPlayer) return;
//        UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId);
//
//        webSocket.sendText(gson.toJson(cmd), true);
//    }
//
//    public void leave() {
//        UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId);
//        webSocket.sendText(gson.toJson(cmd), true);
//    }
//
//    public void redraw() {
//        if (game != null) {
//            BoardDrawer.drawBoard(game, "BLACK".equals(color));
//        }
//    }
//
//    // ---------------- Nested ClientMessage ----------------
//    private static class ClientMessage {
//        String type;
//        String authToken;
//        Integer gameId;
//        String playerColor;
//        String move;
//    }
//}
package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import websocket.commands.UserGameCommand;
import websocket.commands.UserMoveCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import websocket.messages.HighlightRequest;

public class ChessWebSocketClient implements WebSocket.Listener {

    private WebSocket webSocket;
    private final Gson gson = new Gson();
    private final boolean isPlayer;
    private final String color;
    private volatile ChessGame game;
    private final Integer gameId;
    private final String authToken;
    private final ChessNotificationHandler handler;
    private final CountDownLatch openLatch = new CountDownLatch(1);

    public ChessWebSocketClient(String url, String authToken, Integer gameId, String color, ChessNotificationHandler handler) throws Exception {
        this.authToken = authToken;
        this.gameId = gameId;
        this.color = (color != null) ? color.toUpperCase() : null;
        this.handler = handler;
        this.isPlayer = this.color != null;

        connect(url);
        openLatch.await(); // wait until WS is open
        sendJoinMessage(); // send join after connection
    }

    private void connect(String url) {
        webSocket = HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(url), this)
                .join();
    }

    private void sendJoinMessage() {
        // Use UserGameCommand.CONNECT for all players
        UserGameCommand joinCmd = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId);
        webSocket.sendText(gson.toJson(joinCmd), true);
    }
    public void sendHighlight(Collection<ChessPosition> positions) {
        HighlightRequest req =
                new HighlightRequest(gameId, List.copyOf(positions));
        webSocket.sendText(gson.toJson(req), true);
    }


    // ---------------- WebSocket Listener ----------------
    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("Connected to game server.");
        openLatch.countDown();
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
        String msgStr = data.toString();
        try {
            JsonObject json = JsonParser.parseString(msgStr).getAsJsonObject();

            // 1️⃣ Highlight updates
            if (json.has("positions")) {
                HighlightRequest highlight = gson.fromJson(msgStr, HighlightRequest.class);
                handler.onHighlight(highlight.getPositions());

                // 2️⃣ Normal server messages with type
            } else if (json.has("serverMessageType")) {
                ServerMessage msg = gson.fromJson(msgStr, ServerMessage.class);

                switch (msg.getServerMessageType()) {
                    case LOAD_GAME -> {
                        LoadGameMessage load = gson.fromJson(msgStr, LoadGameMessage.class);
                        ChessGame g = load.getGame().game();
                        this.game = g;
                        handler.onLoadGame(g, "BLACK".equals(color));
                    }
                    case NOTIFICATION -> {
                        NotificationMessage notif = gson.fromJson(msgStr, NotificationMessage.class);
                        handler.onNotification(notif.getMessage());
                    }
                    case ERROR -> {
                        ErrorMessage err = gson.fromJson(msgStr, ErrorMessage.class);
                        handler.onError(err.errorMessage);
                    }
                    default -> System.out.println("Unknown serverMessageType: " + msg.getServerMessageType());
                }

                // 3️⃣ Fallback for unknown messages
            } else {
                System.out.println("Unknown message from server: " + msgStr);
            }

        } catch (Exception e) {
            System.out.println("WebSocket parse error: " + e.getMessage());
        }
        return WebSocket.Listener.super.onText(ws, data, last);
    }


    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.out.println("WebSocket error: " + error.getMessage());
        WebSocket.Listener.super.onError(webSocket, error);
    }

    // ---------------- Player commands ----------------
    public void sendMove(int startRow, int startCol, int endRow, int endCol) {
        if (!isPlayer || game == null) return;

        ChessMove move = new ChessMove(
                new ChessPosition(startRow, startCol),
                new ChessPosition(endRow, endCol),
                null
        );

        UserMoveCommand cmd = new UserMoveCommand(authToken, gameId, move);

        webSocket.sendText(gson.toJson(cmd), true);
    }


    public void resign() {
        if (!isPlayer) return;
        UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId);
        webSocket.sendText(gson.toJson(cmd), true);
    }

    public void leave() {
        UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId);
        webSocket.sendText(gson.toJson(cmd), true);
    }

    public void redraw() {
        if (game != null) {
            handler.onLoadGame(game, "BLACK".equals(color));
        }
    }
}
