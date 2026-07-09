package dev.anon.client.features.chat;

import dev.anon.client.AnonClient;
import dev.anon.client.events.chat.*;
import dev.anon.client.mixininterface.IChatHud;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.HashSet;
import java.util.Set;

public class ChatManager {
    private static ChatManager instance;
    private final AxochatClient client;
    private boolean displayMessages;
    private final Set<String> grantedAdmins;

    public static ChatManager get() {
        if (instance == null) instance = new ChatManager();
        return instance;
    }

    private ChatManager() {
        this.client = new AxochatClient();
        this.displayMessages = true;
        this.grantedAdmins = new HashSet<>();
        AnonClient.EVENT_BUS.subscribe(this);
    }

    public AxochatClient getClient() { return client; }

    public boolean isConnected() { return client.isConnected(); }
    public boolean isLoggedIn() { return client.isLoggedIn(); }

    public void setDisplayMessages(boolean display) { this.displayMessages = display; }

    public void connect() {
        connect(true);
    }

    public void connect(boolean autoLogin) {
        Minecraft mc = Minecraft.getInstance();
        String token = mc.getUser().getAccessToken();
        boolean isPremium = token != null && !token.isEmpty() && !token.equals("0");
        client.setOfflineMode(!isPremium);
        var future = client.connect();
        if (autoLogin) {
            future.thenRun(client::requestMojangLogin);
        }
    }

    public void disconnect() { client.disconnect(); }

    public void sendMessage(String message) { client.sendMessage(message); }
    public void banUser(String target) { client.banUser(target); }
    public void unbanUser(String target) { client.unbanUser(target); }
    public void registerPassword(String password) {
        if (!client.isConnected()) {
            client.setOfflineMode(true);
            var future = client.connect();
            future.thenRun(() -> client.registerPassword(password));
        } else {
            client.registerPassword(password);
        }
    }
    public void loginViaPassword(String password) {
        if (!client.isConnected()) {
            client.setOfflineMode(true);
            var future = client.connect();
            future.thenRun(() -> {
                client.loginViaPassword(password);
            });
        } else {
            client.loginViaPassword(password);
        }
    }

    public boolean isCracked() {
        Minecraft mc = Minecraft.getInstance();
        String token = mc.getUser().getAccessToken();
        return token == null || token.isEmpty() || token.equals("0");
    }

    public boolean isRootAdmin() {
        Minecraft mc = Minecraft.getInstance();
        java.util.UUID profileId = mc.getUser().getProfileId();
        if (profileId == null) return false;
        String uuid = profileId.toString();
        return uuid.equals("22de3a67-db6f-45f2-8d27-e26efaea42db")
            || uuid.replace("-", "").equals("22de3a67db6f45f28d27e26efaea42db");
    }

    public boolean isAdmin() {
        if (isRootAdmin()) return true;
        Minecraft mc = Minecraft.getInstance();
        return grantedAdmins.contains(mc.getUser().getName());
    }

    public void grantAdmin(String username) {
        grantedAdmins.add(username);
        sendMessage(" --- " + username + " has been granted admin powers.");
        sendMessage(" ------------------------------------------------------------------------------------");
    }

    public void revokeAdmin(String username) {
        grantedAdmins.remove(username);
        sendMessage(" --- " + username + "'s admin powers have been revoked.");
        sendMessage(" ------------------------------------------------------------------------------------");
    }

    public Set<String> getGrantedAdmins() {
        return new HashSet<>(grantedAdmins);
    }

    private void addChat(Component msg) {
        Minecraft.getInstance().execute(() ->
            ((IChatHud) Minecraft.getInstance().gui.getChat()).anon$add(msg, 0));
    }

    @EventHandler
    private void onChatMessage(ClientChatMessageEvent event) {
        if (!displayMessages) return;
        if (!"main".equals(event.getSource())) return;
        String displayName = event.getUser().getName();
        if (isCracked() && displayName.equals(Minecraft.getInstance().getUser().getName())) {
            displayName = "+" + displayName;
        }
        String prefix = switch (event.getChatGroup()) {
            case PUBLIC_CHAT -> ChatFormatting.GREEN + "[AN0N Chat] ";
            case PRIVATE_CHAT -> ChatFormatting.AQUA + "[AN0N Chat PM] ";
        };
        MutableComponent msg = Component.literal("")
            .append(Component.literal(prefix + displayName + ": ")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)))
            .append(Component.literal(event.getMessage())
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        addChat(msg);
    }

    @EventHandler
    private void onChatError(ClientChatErrorEvent event) {
        if (!displayMessages) return;
        if (!"main".equals(event.getSource())) return;
        addChat(Component.literal(ChatFormatting.RED + "[AN0N Chat] " + event.getError()));
    }

    @EventHandler
    private void onChatStateChange(ClientChatStateChange event) {
        if (!displayMessages) return;
        if (!"main".equals(event.getSource())) return;
        String message = switch (event.getState()) {
            case CONNECTING -> ChatFormatting.YELLOW + "Connecting to AN0N Chat...";
            case CONNECTED -> ChatFormatting.GREEN + "Connected to AN0N Chat.";
            case LOGGING_IN -> ChatFormatting.YELLOW + "Logging in...";
            case LOGGED_IN -> ChatFormatting.GREEN + "Logged into AN0N Chat.";
            case DISCONNECTED -> ChatFormatting.RED + "Disconnected from AN0N Chat.";
            case AUTHENTICATION_FAILED -> ChatFormatting.RED + "AN0N Chat authentication failed.";
        };
        addChat(Component.literal(message));
    }
}
