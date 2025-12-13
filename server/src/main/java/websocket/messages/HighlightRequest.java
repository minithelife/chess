package websocket.messages;

import chess.ChessPosition;
import java.util.List;

public class HighlightRequest {

    private Integer gameID;
    private List<ChessPosition> positions;

    public Integer getGameID() {
        return gameID;
    }

    public List<ChessPosition> getPositions() {
        return positions;
    }
}
