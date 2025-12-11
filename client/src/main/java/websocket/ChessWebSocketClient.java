package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.commands.UserMoveCommand;
import websocket.messages.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

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
        UserGameCommand joinCmd = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId);
        webSocket.sendText(gson.toJson(joinCmd), true);
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
            case HIGHLIGHT -> {
                HighlightMessage highlightMsg = gson.fromJson(msgStr, HighlightMessage.class);
                handler.onHighlight(highlightMsg.getHighlightPositions());
            }
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

    public void sendHighlight(List<ChessPosition> positions) {
        if (positions == null || positions.isEmpty()) return;

        // Convert positions to row/col maps for server
        List<Map<String, Integer>> posList = new ArrayList<>();
        for (ChessPosition p : positions) {
            Map<String, Integer> m = new HashMap<>();
            m.put("row", p.getRow());
            m.put("col", p.getColumn());
            posList.add(m);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("highlightPositions", posList);
        map.put("authToken", authToken);
        map.put("gameID", gameId);

        webSocket.sendText(gson.toJson(map), true);
    }
}
