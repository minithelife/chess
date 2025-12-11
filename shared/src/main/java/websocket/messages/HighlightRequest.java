package websocket.messages;

import chess.ChessPosition;
import java.util.List;

public class HighlightRequest {
    private String authToken;
    private Integer gameID;
    private List<ChessPosition> highlightPositions;

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    public List<ChessPosition> getPositions() {
        return highlightPositions;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;  // assign value
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;  // assign value
    }

    public void setPositions(List<ChessPosition> highlightPositions) {
        this.highlightPositions = highlightPositions;  // assign value
    }
}
