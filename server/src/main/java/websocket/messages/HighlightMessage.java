package websocket.messages;

import chess.ChessPosition;
import java.util.List;

public class HighlightMessage {

    private final String type = "HIGHLIGHT";
    private final List<ChessPosition> positions;

    public HighlightMessage(List<ChessPosition> positions) {
        this.positions = positions;
    }

    public List<ChessPosition> getPositions() {
        return positions;
    }

    public String getType() {
        return type;
    }
}
