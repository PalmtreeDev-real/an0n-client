package dev.anon.client.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anon.client.commands.Command;
import dev.anon.client.features.chat.packet.ServerPackets;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.misc.ClientChat;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ChatJwtCommand extends Command {
    public ChatJwtCommand() {
        super("chatjwt", "Request a new JWT token from AN0N Chat.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(context -> {
            ClientChat module = Modules.get().get(ClientChat.class);
            if (module == null || !module.isActive()) {
                error("Client Chat module is not enabled.");
                return SINGLE_SUCCESS;
            }

            if (!module.isLoggedIn()) {
                error("You must be logged in to request a JWT token.");
                return SINGLE_SUCCESS;
            }

            module.getChatClient().sendRawPacket(
                new ServerPackets.RequestJWTPacket()
            );
            return SINGLE_SUCCESS;
        });
    }
}
