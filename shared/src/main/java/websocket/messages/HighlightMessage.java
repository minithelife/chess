package websocket.messages;

import chess.ChessPosition;
import java.util.List;

public class HighlightMessage extends ServerMessage {

    private final List<ChessPosition> highlightPositions;

    public HighlightMessage(List<ChessPosition> highlightPositions) {
        super(ServerMessageType.HIGHLIGHT); // you may need to add HIGHLIGHT to ServerMessageType
        this.highlightPositions = highlightPositions;
    }

    public List<ChessPosition> getHighlightPositions() {
        return highlightPositions;
    }
}
