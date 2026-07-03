package dev.anon.client.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anon.client.commands.Command;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.misc.ClientChat;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ChatCommand extends Command {
    public ChatCommand() {
        super("chat", "Send a message to AN0N Chat.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(
            argument("message", StringArgumentType.greedyString())
                .executes(context -> {
                    ClientChat module = Modules.get().get(ClientChat.class);
                    if (module == null || !module.isActive()) {
                        error("Client Chat module is not enabled.");
                        return SINGLE_SUCCESS;
                    }

                    if (!module.isConnected()) {
                        error("Not connected to AN0N Chat.");
                        return SINGLE_SUCCESS;
                    }

                    if (!module.isLoggedIn()) {
                        error("Not logged into AN0N Chat.");
                        return SINGLE_SUCCESS;
                    }

                    String message = context.getArgument("message", String.class);
                    module.getChatClient().sendMessage(message);
                    return SINGLE_SUCCESS;
                })
        );
    }
}
