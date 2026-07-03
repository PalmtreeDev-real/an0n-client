package dev.anon.client.events.chat;

public class ClientChatStateChange {
    private final State state;

    public ClientChatStateChange(State state) {
        this.state = state;
    }

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
