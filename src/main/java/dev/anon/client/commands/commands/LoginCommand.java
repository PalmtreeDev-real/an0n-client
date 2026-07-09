package dev.anon.client.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anon.client.commands.Command;
import dev.anon.client.features.chat.ChatManager;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class LoginCommand extends Command {
    public LoginCommand() {
        super("login", "Login to AN0N Chat with a registered password.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(
            argument("password", StringArgumentType.greedyString())
                .executes(ctx -> {
                    ChatManager chat = ChatManager.get();
                    String password = ctx.getArgument("password", String.class);

                    if (!chat.isCracked()) {
                        error("Premium accounts use Mojang authentication. This command is for cracked users.");
                        return SINGLE_SUCCESS;
                    }

                    if (chat.isLoggedIn()) {
                        error("Already logged in.");
                        return SINGLE_SUCCESS;
                    }

                    info("Logging in with password...");
                    chat.loginViaPassword(password);
                    return SINGLE_SUCCESS;
                })
        );
    }
}
