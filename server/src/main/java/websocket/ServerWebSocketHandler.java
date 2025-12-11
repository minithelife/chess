//package websocket;
//
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.servlet.ServletContextHandler;
//import org.eclipse.jetty.websocket.server.WebSocketHandler;
//import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
//import service.GameService;
//
//public class ServerWebSocketHandler {
//
//    private final Server server;
//    private final GameService gameService;
//
//    public ServerWebSocketHandler(Server server, GameService gameService) {
//        this.server = server;
//        this.gameService = gameService;
//    }
//
//    public void configure() {
//
//        WebSocketHandler wsHandler = new WebSocketHandler() {
//            @Override
//            public void configure(WebSocketServletFactory factory) {
//                // Optional timeouts
//                factory.getPolicy().setIdleTimeout(10000);
//
//                // Register endpoint factory
//                factory.setCreator((req, resp) -> new ChessWebSocketHandler(gameService));
//            }
//        };
//
//        // Map the handler at "/ws"
//        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        context.setContextPath("/");
//        context.setHandler(wsHandler);
//
//        server.setHandler(context);
//
//        System.out.println("WebSocket listening on ws://localhost:8080/ws");
//    }
//}
