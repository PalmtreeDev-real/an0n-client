package dev.anon.client.events.chat;

public class ClientChatStateChange {
    private final String source;
    private final State state;

    public ClientChatStateChange(String source, State state) {
        this.source = source;
        this.state = state;
    }

    public String getSource() { return source; }
    public State getState() { return state; }

    public enum State {
        CONNECTING,
        CONNECTED,
        LOGGING_IN,
        LOGGED_IN,
        DISCONNECTED,
        AUTHENTICATION_FAILED
    }
}
