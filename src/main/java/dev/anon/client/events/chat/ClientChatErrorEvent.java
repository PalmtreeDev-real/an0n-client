package dev.anon.client.events.chat;

public class ClientChatErrorEvent {
    private final String error;

    public ClientChatErrorEvent(String error) {
        this.error = error;
    }

    public String getError() { return error; }
}
