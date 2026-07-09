package dev.anon.client.events.chat;

public class ClientChatErrorEvent {
    private final String source;
    private final String error;

    public ClientChatErrorEvent(String source, String error) {
        this.source = source;
        this.error = error;
    }

    public String getSource() { return source; }
    public String getError() { return error; }
}
