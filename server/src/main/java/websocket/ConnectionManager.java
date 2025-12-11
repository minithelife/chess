package websocket;

import io.javalin.websocket.WsContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    // gameID -> set of websocket sessions
    private final Map<Integer, Set<WsContext>> gameConnections = new ConcurrentHashMap<>();

    // Add a session to a game
    public void add(int gameID, WsContext session) {
        if (session != null && session.session != null && session.session.isOpen()) {
            gameConnections
                    .computeIfAbsent(gameID, id -> ConcurrentHashMap.newKeySet())
                    .add(session);
        }
    }

    // Remove a session from all games
    public void remove(WsContext session) {
        if (session == null) return;
        for (Set<WsContext> sessions : gameConnections.values()) {
            sessions.remove(session);
        }
    }

    // Get all sessions for a game (never null)
    public Set<WsContext> getSessions(int gameID) {
        return gameConnections.getOrDefault(gameID, Collections.emptySet());
    }

    // Send a message to a single session safely
    public void send(WsContext session, String json) {
        if (session != null && session.session != null && session.session.isOpen()) {
            try {
                session.send(json);
            } catch (Exception e) {
                System.err.println("Failed to send WS message to session "
                        + session.sessionId() + ": " + e.getMessage());
                remove(session); // remove broken session
            }
        }
    }

    // Broadcast to all sessions in a game, excluding one (can be null)
    public void broadcast(int gameID, WsContext exclude, String json) {
        Set<WsContext> sessions = new HashSet<>(getSessions(gameID)); // defensive copy
        for (WsContext s : sessions) {
            try {
                if (s != null && s.session != null && s.session.isOpen() && !s.equals(exclude)) {
                    s.send(json);
                } else {
                    // Remove invalid or closed sessions
                    remove(s);
                }
            } catch (Exception e) {
                System.err.println("Failed to broadcast to session " +
                        (s != null ? s.sessionId() : "null") + ": " + e.getMessage());
                remove(s);
            }
        }
    }

    // Broadcast with same behavior (kept for optional semantic difference)
    public void broadcastExcluding(int gameID, WsContext exclude, String json) {
        broadcast(gameID, exclude, json);
    }

}
