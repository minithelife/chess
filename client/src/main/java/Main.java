import client.Client;

public class Main {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        try {
            new Client(serverUrl).run();
        } catch (Throwable ex) {
            System.out.printf("Unable to start client: %s%n", ex.getMessage());
        }
    }
}
