package websocket;

import chess.ChessGame;
import chess.ChessPosition;

import java.util.List;

public interface ChessNotificationHandler {
    void onNotification(String message);
    void onError(String message);
    void onLoadGame(ChessGame game, boolean blackPerspective);

    void onHighlight(List<ChessPosition> highlightPositions);
}
