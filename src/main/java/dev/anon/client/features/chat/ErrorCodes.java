package dev.anon.client.features.chat;

public enum ErrorCodes {
    DEVICE_BAN(624, "Device Ban", "\uD83D\uDEE1\uFE0F Your hardware is permanently exiled from An0n SocialChat."),
    PERMANENT_BAN(977, "Permanent Ban", "\u26B0 You are persona non grata. No appeal. No mercy."),
    CHAT_SUSPENSION(992, "Suspended from Chat", "\u23F3 You're on timeout. The AI is watching you."),
    OUTDATED_CLIENT(726, "Outdated Client", "\uD83D\uDD04 Update your client or get left behind."),
    CLIENT_ISSUE(872, "Client Issue", "\uD83D\uDD27 Something's broken on your end. Reinstall."),
    AI_NOT_FOUND(192, "AI Not Found", "\uD83E\uDDE0 The brain is missing. Did Ollama fail?"),
    SERVER_OVERLOADED(762, "Server Overloaded", "\uD83C\uDF0D Too many An0n users. The network is popping off."),
    ROASTED_BY_AI(420, "Roasted by AI", "\uD83D\uDCA5 You got called out publicly in SocialChat."),
    AI_CORRUPTION(666, "AI Corruption", "\uD83D\uDC7F The AI has gone rogue. Good luck."),
    ADMIN_OVERRIDE(1, "Admin Override", "\uD83D\uDC40 You did something. Or the AI did. Who knows."),
    FEATURE_NOT_FOUND(404, "Feature Not Found", "\uD83D\uDD0D You tried to enable something that doesn't exist yet.");

    private final int code;
    private final String name;
    private final String description;

    ErrorCodes(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return code + " - " + name;
    }

    public String toFullString() {
        return code + " " + name + ": " + description;
    }
}
