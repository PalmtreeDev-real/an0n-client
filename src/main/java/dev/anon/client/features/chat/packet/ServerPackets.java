package dev.anon.client.features.chat.packet;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;

public class ServerPackets {
    public static class RequestMojangInfoPacket implements AxochatPacket.C2S {
        public RequestMojangInfoPacket() {}
    }

    public static class LoginMojangPacket implements AxochatPacket.C2S {
        @SerializedName("name")
        private final String name;

        @SerializedName("uuid")
        private final UUID uuid;

        @SerializedName("allow_messages")
        private final boolean allowMessages;

        public LoginMojangPacket(String name, UUID uuid, boolean allowMessages) {
            this.name = name;
            this.uuid = uuid;
            this.allowMessages = allowMessages;
        }

        public String getName() { return name; }
        public UUID getUuid() { return uuid; }
        public boolean isAllowMessages() { return allowMessages; }
    }

    public static class LoginJWTPacket implements AxochatPacket.C2S {
        @SerializedName("token")
        private final String token;

        @SerializedName("allow_messages")
        private final boolean allowMessages;

        public LoginJWTPacket(String token, boolean allowMessages) {
            this.token = token;
            this.allowMessages = allowMessages;
        }

        public String getToken() { return token; }
        public boolean isAllowMessages() { return allowMessages; }
    }

    public static class ServerMessagePacket implements AxochatPacket.C2S {
        @SerializedName("content")
        private final String content;

        public ServerMessagePacket(String content) {
            this.content = content;
        }

        public String getContent() { return content; }
    }

    public static class ServerPrivateMessagePacket implements AxochatPacket.C2S {
        @SerializedName("receiver")
        private final String receiver;

        @SerializedName("content")
        private final String content;

        public ServerPrivateMessagePacket(String receiver, String content) {
            this.receiver = receiver;
            this.content = content;
        }

        public String getReceiver() { return receiver; }
        public String getContent() { return content; }
    }

    public static class BanUserPacket implements AxochatPacket.C2S {
        @SerializedName("user")
        private final String user;

        public BanUserPacket(String user) {
            this.user = user;
        }

        public String getUser() { return user; }
    }

    public static class UnbanUserPacket implements AxochatPacket.C2S {
        @SerializedName("user")
        private final String user;

        public UnbanUserPacket(String user) {
            this.user = user;
        }

        public String getUser() { return user; }
    }

    public static class RequestJWTPacket implements AxochatPacket.C2S {
        public RequestJWTPacket() {}
    }
}
