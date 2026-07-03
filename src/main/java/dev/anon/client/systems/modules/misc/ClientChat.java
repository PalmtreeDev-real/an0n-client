package dev.anon.client.systems.modules.misc;

import com.mojang.brigadier.StringReader;
import dev.anon.client.AnonClient;
import dev.anon.client.events.chat.*;
import dev.anon.client.events.world.TickEvent;
import dev.anon.client.features.chat.AxochatClient;
import dev.anon.client.settings.*;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.concurrent.CompletableFuture;

public class ClientChat extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> autoConnect = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-connect")
        .description("Automatically connect to AN0N Chat when the module is enabled.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> allowOffline = sgGeneral.add(new BoolSetting.Builder()
        .name("allow-offline")
        .description("Allow connecting with offline/cracked accounts (may not work if server requires premium).")
        .defaultValue(true)
        .build()
    );

    private final AxochatClient chatClient = new AxochatClient();

    private CompletableFuture<Void> connectFuture;

    public ClientChat() {
        super(Categories.Misc, "client-chat", "Chat with other AN0N users across servers.");
        title = "AN0N SocialChat";
        setGradient(new Color(0, 0, 0), new Color(128, 128, 128));
    }

    @Override
    public void onActivate() {
        if (autoConnect.get()) {
            connect();
        }
    }

    @Override
    public void onDeactivate() {
        chatClient.disconnect();
    }

    public void connect() {
        if (!chatClient.isConnected() && !chatClient.isConnecting()) {
            String token = mc.getUser().getAccessToken();
            boolean isPremium = token != null && !token.isEmpty() && !token.equals("0");
            chatClient.setOfflineMode(!isPremium && allowOffline.get());
            connectFuture = chatClient.connect();
            connectFuture.thenRun(chatClient::requestMojangLogin);
        }
    }

    public void disconnect() {
        chatClient.disconnect();
    }

    public AxochatClient getChatClient() {
        return chatClient;
    }

    public boolean isConnected() {
        return chatClient.isConnected();
    }

    public boolean isLoggedIn() {
        return chatClient.isLoggedIn();
    }

    @EventHandler
    private void onChatMessage(ClientChatMessageEvent event) {
        String prefix = switch (event.getChatGroup()) {
            case PUBLIC_CHAT -> ChatFormatting.GREEN + "[AN0N Chat] ";
            case PRIVATE_CHAT -> ChatFormatting.AQUA + "[AN0N Chat PM] ";
        };

        MutableComponent msg = Component.literal("")
            .append(Component.literal(prefix + event.getUser().getName() + ": ")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)))
            .append(Component.literal(event.getMessage())
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));

        info(msg);
    }

    @EventHandler
    private void onChatError(ClientChatErrorEvent event) {
        error("[AN0N Chat] %s", event.getError());
    }

    @EventHandler
    private void onChatStateChange(ClientChatStateChange event) {
        String message = switch (event.getState()) {
            case CONNECTING -> ChatFormatting.YELLOW + "Connecting to AN0N Chat...";
            case CONNECTED -> ChatFormatting.GREEN + "Connected to AN0N Chat.";
            case LOGGING_IN -> ChatFormatting.YELLOW + "Logging in...";
            case LOGGED_IN -> ChatFormatting.GREEN + "Logged into AN0N Chat.";
            case DISCONNECTED -> ChatFormatting.RED + "Disconnected from AN0N Chat.";
            case AUTHENTICATION_FAILED -> ChatFormatting.RED + "AN0N Chat authentication failed. Use a premium Minecraft account.";
        };

        info(Component.literal(message));
    }

    @Override
    public String getInfoString() {
        if (chatClient.isLoggedIn()) return "Logged in";
        if (chatClient.isConnected()) return "Connected";
        if (chatClient.isConnecting()) return "Connecting...";
        return null;
    }
}
