package handler;

public class Message {
    public String message;  // must be public for Gson

    public Message(String message) {
        this.message = message;
    }

    public Message() {}  // default constructor
}