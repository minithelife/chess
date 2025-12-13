package websocket.messages;

import chess.ChessPosition;
import java.util.List;

public class HighlightRequest {
    private Integer gameID;
    private List<ChessPosition> positions;

    public HighlightRequest(Integer gameID, List<ChessPosition> positions) {
        this.gameID = gameID;
        this.positions = positions;
    }

    // ✅ Add getters
    public Integer getGameID() {
        return gameID;
    }

    public List<ChessPosition> getPositions() {
        return positions;
    }

    // Optional: Add setters if needed
    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }

    public void setPositions(List<ChessPosition> positions) {
        this.positions = positions;
    }
}
