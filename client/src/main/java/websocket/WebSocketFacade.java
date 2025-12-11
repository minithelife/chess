package websocket;

import com.google.gson.Gson;

import java.net.URI;

import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

@ClientEndpoint
public class WebSocketFacade {

    private Session session;
    private final Gson gson = new Gson();

    public WebSocketFacade(String url) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, URI.create(url));
    }

    @OnMessage
    public void onMessage(String msg) {
        ServerMessage serverMsg = gson.fromJson(msg, ServerMessage.class);
        System.out.println("WS MESSAGE: " + serverMsg.getServerMessageType());
    }

    public void send(UserGameCommand cmd) {
        session.getAsyncRemote().sendText(gson.toJson(cmd));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connected to server");
    }

    @OnClose
    public void onClose() {
        System.out.println("Disconnected");
    }
}
