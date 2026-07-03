package dev.anon.client.events.chat;

import dev.anon.client.features.chat.packet.AxoUser;

public class ClientChatMessageEvent {
    private final AxoUser user;
    private final String message;
    private final ChatGroup chatGroup;

    public ClientChatMessageEvent(AxoUser user, String message, ChatGroup chatGroup) {
        this.user = user;
        this.message = message;
        this.chatGroup = chatGroup;
    }

    public AxoUser getUser() { return user; }
    public String getMessage() { return message; }
    public ChatGroup getChatGroup() { return chatGroup; }

    public enum ChatGroup {
        PUBLIC_CHAT,
        PRIVATE_CHAT
    }
}
