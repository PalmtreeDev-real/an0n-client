package dev.anon.client.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anon.client.commands.Command;
import dev.anon.client.features.chat.ChatManager;
import dev.anon.client.features.chat.packet.ServerPackets;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ChatJwtCommand extends Command {
    public ChatJwtCommand() {
        super("chatjwt", "Request a new JWT token from AN0N Chat.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(context -> {
            ChatManager chat = ChatManager.get();

            if (!chat.isLoggedIn()) {
                error("You must be logged in to request a JWT token.");
                return SINGLE_SUCCESS;
            }

            chat.getClient().sendRawPacket(new ServerPackets.RequestJWTPacket());
            info("JWT token requested.");
            return SINGLE_SUCCESS;
        });
    }
}
