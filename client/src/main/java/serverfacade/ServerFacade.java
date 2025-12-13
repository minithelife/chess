package serverfacade;

import com.google.gson.*;
import model.*;
import model.requests.CreateGameRequest;
import model.requests.JoinGameRequest;
import model.requests.LoginRequest;
import model.requests.RegisterRequest;
import model.responses.ListGamesResponse;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import chess.ChessGame;

public class ServerFacade {

    private final String baseUrl;
    private final HttpClient client;
    private final Gson gson = new GsonBuilder().create();

    public ServerFacade(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    private HttpRequest.Builder req(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(5));
    }

    private String doRequest(HttpRequest request) throws Exception {
        HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int code = res.statusCode();
        if (code >= 200 && code < 300) {
            return res.body();
        }


        else {
            try {
                JsonObject obj = JsonParser.parseString(res.body()).getAsJsonObject();
                String msg = obj.has("message") ? obj.get("message").getAsString() : null;
                if (msg != null) {
                    throw new Exception(msg);
                }
            } catch (JsonParseException ignored) {}

            throw new Exception("HTTP " + code);
        }


    }

    // Clear DB (testing)
    public void clear() throws Exception {
        var request = req("/db").DELETE().build();
        doRequest(request);
    }

    // Register -> POST /user
    public AuthData register(RegisterRequest reqBody) throws Exception {
        String json = gson.toJson(reqBody);
        var request = req("/user")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        String body = doRequest(request);
        return gson.fromJson(body, AuthData.class);
    }

    // Login -> POST /session
    public AuthData login(LoginRequest reqBody) throws Exception {
        String json = gson.toJson(reqBody);
        var request = req("/session")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        String body = doRequest(request);
        return gson.fromJson(body, AuthData.class);
    }

    // Logout -> DELETE /session
    public void logout(String authToken) throws Exception {
        var request = req("/session")
                .header("authorization", authToken)
                .DELETE()
                .build();
        doRequest(request);
    }

    // Create Game -> POST /game
    public GameIdResponse createGame(String authToken, CreateGameRequest reqBody) throws Exception {
        String json = gson.toJson(reqBody);
        var request = req("/game")
                .header("Content-Type", "application/json")
                .header("authorization", authToken)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        String body = doRequest(request);
        return gson.fromJson(body, GameIdResponse.class);
    }

    // List Games -> GET /game
    public ListGamesResponse listGames(String authToken) throws Exception {
        var request = req("/game")
                .header("authorization", authToken)
                .GET()
                .build();
        String body = doRequest(request);
        // server returns {"games":[ ... ]}
        return gson.fromJson(body, ListGamesResponse.class);
    }

    // Join -> PUT /game
    public void joinGame(String authToken, JoinGameRequest reqBody) throws Exception {
        String json = gson.toJson(reqBody);
        var request = req("/game")
                .header("Content-Type", "application/json")
                .header("authorization", authToken)
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        doRequest(request);
    }

    // Fetch single game -> not in original spec but helpful: GET /game/{id}
    // Since server doesn't expose GET /game/{id}, we'll list and pick
    public GameData getGame(int gameID, String authToken) throws Exception {
        var list = listGames(authToken);
        for (GameData g : list.games()) {
            if (g.gameID() == gameID) {
                return g;
            }
        }
        return null;
    }

    public String getServerUrl() {
        return baseUrl;
    }

    // helper response for createGame
    public static class GameIdResponse {
        private int gameID;
        public int gameID() { return gameID; }
    }
}
