package websocket.messages;

import chess.ChessPosition;
import java.util.List;

public class HighlightMessage {
    private String type;
    private List<ChessPosition> positions;

    public List<ChessPosition> getPositions() {
        return positions;
    }

    public String getType() {
        return type;
    }
}
