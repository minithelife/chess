package websocket;

import chess.ChessGame;
import chess.ChessPosition;

import java.util.Collection;

public interface ChessNotificationHandler {
    void onNotification(String message);
    void onError(String message);
    void onLoadGame(ChessGame game, boolean blackPerspective);
    void onHighlight(Collection<ChessPosition> positions);

}
