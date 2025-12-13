package websocket;

import io.javalin.websocket.WsContext;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    // Map gameID -> set of websocket sessions
    private final Map<Integer, Set<WsContext>> gameConnections = new ConcurrentHashMap<>();

    // Add a session to a game
    public void add(int gameID, WsContext session) {
        gameConnections
                .computeIfAbsent(gameID, id -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    // Remove a session from all games
    public void remove(WsContext session) {
        for (Set<WsContext> sessions : gameConnections.values()) {
            sessions.remove(session);
        }
    }

    // Get all sessions in a game
    public Set<WsContext> getSessions(int gameID) {
        return gameConnections.getOrDefault(gameID, Set.of());
    }

    // Send a message to a single client
    public void send(WsContext session, String json) {
        if (session != null && session.session.isOpen()) {
            synchronized (session) {
                session.send(json);
            }
        }
    }

    // Broadcast to all clients in the game
    public void broadcast(int gameID, String json) {
        for (WsContext s : getSessions(gameID)) {
            send(s, json);
        }
    }

    // Broadcast to all clients **except** one (root)
    public void broadcastExcluding(int gameID, WsContext exclude, String json) {
        for (WsContext s : getSessions(gameID)) {
            if (!s.equals(exclude)) {
                send(s, json);
            }
        }
    }
}
