package dev.anon.client.events.chat;

public class ClientChatJwtTokenEvent {
    private final String jwt;

    public ClientChatJwtTokenEvent(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() { return jwt; }
}
