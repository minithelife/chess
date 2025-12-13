package websocket.messages;

import chess.ChessPosition;
import java.util.List;

public class HighlightRequest {

    private Integer gameID;
    private List<ChessPosition> positions;

    public HighlightRequest() {} // For Gson deserialization

    public HighlightRequest(Integer gameID, List<ChessPosition> positions) {
        this.gameID = gameID;
        this.positions = positions;
    }

    public Integer getGameID() {
        return gameID;
    }

    public List<ChessPosition> getPositions() {
        return positions;
    }
}
