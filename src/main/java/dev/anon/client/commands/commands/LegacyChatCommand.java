package dev.anon.client.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anon.client.AnonClient;
import dev.anon.client.commands.Command;
import dev.anon.client.events.chat.*;
import dev.anon.client.features.chat.AxochatClient;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.net.URI;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class LegacyChatCommand extends Command {
    private static final URI LEGACY_URI = URI.create("wss://chat.liquidbounce.net:7886/legacy");
    private final AxochatClient client;
    private boolean subscribed;

    public LegacyChatCommand() {
        super("legacychat", "Send a message through Legacy LiquidBounce chat.");
        this.client = new AxochatClient(LEGACY_URI, "legacy");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(
            argument("message", StringArgumentType.greedyString())
                .executes(context -> {
                    if (!client.isConnected() && !client.isConnecting()) {
                        String token = mc.getUser().getAccessToken();
                        boolean isPremium = token != null && !token.isEmpty() && !token.equals("0");
                        client.setOfflineMode(!isPremium);
                        info("Connecting to Legacy Chat...");
                        var future = client.connect();
                        future.thenRun(client::requestMojangLogin);
                        if (!subscribed) {
                            AnonClient.EVENT_BUS.subscribe(this);
                            subscribed = true;
                        }
                    }

                    if (!client.isLoggedIn()) {
                        error("Not logged into Legacy Chat yet. Try again in a moment.");
                        return SINGLE_SUCCESS;
                    }

                    String message = context.getArgument("message", String.class);
                    client.sendMessage(message);
                    return SINGLE_SUCCESS;
                })
        );
    }

    @EventHandler
    private void onLegacyMessage(ClientChatMessageEvent event) {
        if (!"legacy".equals(event.getSource())) return;
        MutableComponent msg = Component.literal("")
            .append(Component.literal(ChatFormatting.GOLD + "[Legacy Chat] " + event.getUser().getName() + ": ")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)))
            .append(Component.literal(event.getMessage())
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        info(msg);
    }

    @EventHandler
    private void onLegacyError(ClientChatErrorEvent event) {
        if (!"legacy".equals(event.getSource())) return;
        error("[Legacy Chat] %s", event.getError());
    }

    @EventHandler
    private void onLegacyState(ClientChatStateChange event) {
        if (!"legacy".equals(event.getSource())) return;
        String message = switch (event.getState()) {
            case CONNECTING -> ChatFormatting.YELLOW + "Connecting to Legacy Chat...";
            case CONNECTED -> ChatFormatting.GREEN + "Connected to Legacy Chat.";
            case LOGGING_IN -> ChatFormatting.YELLOW + "Logging in to Legacy Chat...";
            case LOGGED_IN -> ChatFormatting.GREEN + "Logged into Legacy Chat.";
            case DISCONNECTED -> ChatFormatting.RED + "Disconnected from Legacy Chat.";
            case AUTHENTICATION_FAILED -> ChatFormatting.RED + "Legacy Chat authentication failed.";
        };
        info(Component.literal(message));
    }
}
