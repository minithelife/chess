package websocket;

import io.javalin.websocket.WsContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    // gameID -> set of websocket sessions
    private final Map<Integer, Set<WsContext>> gameConnections = new ConcurrentHashMap<>();

    public void add(int gameID, WsContext session) {
        gameConnections.computeIfAbsent(gameID, id -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void remove(WsContext session) {
        for (Set<WsContext> sessions : gameConnections.values()) {
            sessions.remove(session);
        }
    }

    public Set<WsContext> getSessions(int gameID) {
        return gameConnections.getOrDefault(gameID, Set.of());
    }

    public void send(WsContext session, String json) {
        if (session != null && session.session.isOpen()) {
            session.send(json);
        }
    }

    public void broadcast(int gameID, WsContext exclude, String json) {
        for (WsContext s : getSessions(gameID)) {
            if (!s.equals(exclude) && s.session.isOpen()) {
                s.send(json);
            }
        }
    }
    // Optional: keep old version if you want to exclude root player for normal moves
    public void broadcastExcluding(int gameID, WsContext exclude, String json) {
        for (WsContext s : getSessions(gameID)) {
            if (!s.equals(exclude) && s.session.isOpen()) {
                s.send(json);
            }
        }
    }
}
