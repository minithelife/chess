package websocket;

import chess.ChessGame;

public interface ChessNotificationHandler {
    void onNotification(String message);
    void onError(String message);
    void onLoadGame(ChessGame game, boolean blackPerspective);
}
