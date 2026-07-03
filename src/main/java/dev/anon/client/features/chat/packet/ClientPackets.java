package dev.anon.client.features.chat.packet;

import com.google.gson.annotations.SerializedName;

public class ClientPackets {
    public static class MojangInfoPacket implements AxochatPacket.S2C {
        @SerializedName("session_hash")
        private String sessionHash;

        public MojangInfoPacket() {}

        public MojangInfoPacket(String sessionHash) {
            this.sessionHash = sessionHash;
        }

        public String getSessionHash() { return sessionHash; }
    }

    public static class NewJWTPacket implements AxochatPacket.S2C {
        @SerializedName("token")
        private String token;

        public NewJWTPacket() {}

        public NewJWTPacket(String token) {
            this.token = token;
        }

        public String getToken() { return token; }
    }

    public static class ClientMessagePacket implements AxochatPacket.S2C {
        @SerializedName("author_id")
        private String id;

        @SerializedName("author_info")
        private AxoUser user;

        @SerializedName("content")
        private String content;

        public ClientMessagePacket() {}

        public ClientMessagePacket(String id, AxoUser user, String content) {
            this.id = id;
            this.user = user;
            this.content = content;
        }

        public String getId() { return id; }
        public AxoUser getUser() { return user; }
        public String getContent() { return content; }
    }

    public static class ClientPrivateMessagePacket implements AxochatPacket.S2C {
        @SerializedName("author_id")
        private String id;

        @SerializedName("author_info")
        private AxoUser user;

        @SerializedName("content")
        private String content;

        public ClientPrivateMessagePacket() {}

        public ClientPrivateMessagePacket(String id, AxoUser user, String content) {
            this.id = id;
            this.user = user;
            this.content = content;
        }

        public String getId() { return id; }
        public AxoUser getUser() { return user; }
        public String getContent() { return content; }
    }

    public static class SuccessPacket implements AxochatPacket.S2C {
        @SerializedName("reason")
        private String reason;

        public SuccessPacket() {}

        public SuccessPacket(String reason) {
            this.reason = reason;
        }

        public String getReason() { return reason; }
    }

    public static class ErrorPacket implements AxochatPacket.S2C {
        @SerializedName("message")
        private String message;

        public ErrorPacket() {}

        public ErrorPacket(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
    }
}
