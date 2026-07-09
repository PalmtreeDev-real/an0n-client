package dev.anon.client.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.anon.client.commands.Command;
import dev.anon.client.systems.ai.An0nAI;
import dev.anon.client.utils.player.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class AiConnectCommand extends Command {
    public AiConnectCommand() {
        super("aiconnect", "Configure An0nAI with a provider and API key.", "ai-connect");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(argument("provider", StringArgumentType.word())
            .then(argument("key", StringArgumentType.greedyString()).executes(context -> {
                String providerStr = context.getArgument("provider", String.class);
                String key = context.getArgument("key", String.class);

                An0nAI ai = An0nAI.get();
                An0nAI.AiProvider selected = null;

                for (An0nAI.AiProvider p : An0nAI.AiProvider.values()) {
                    if (p.name().equalsIgnoreCase(providerStr) || p.getDisplayName().equalsIgnoreCase(providerStr)) {
                        selected = p;
                        break;
                    }
                }

                if (selected == null) {
                    StringBuilder sb = new StringBuilder("Available providers: ");
                    for (An0nAI.AiProvider p : An0nAI.AiProvider.values()) {
                        sb.append(p.name().toLowerCase()).append(" ");
                    }
                    error(sb.toString().trim());
                    return SINGLE_SUCCESS;
                }

                ai.setProvider(selected);
                ai.setApiKey(key);

                String modelInfo = selected.getDefaultModel();
                if (selected == An0nAI.AiProvider.Ollama || (key.startsWith("http://") || key.startsWith("https://"))) {
                    if (key.startsWith("http://") || key.startsWith("https://")) {
                        ai.setCustomEndpoint(key);
                        if (selected == An0nAI.AiProvider.Custom || selected == An0nAI.AiProvider.Ollama) {
                            ai.setApiKey("ollama");
                        }
                        info("Connected to custom endpoint: " + key);
                        info("Default model: " + ai.getModel());
                        return SINGLE_SUCCESS;
                    }
                }

                info("Connected to " + selected.getDisplayName() + " (model: " + modelInfo + ").");
                info("Use .ai <message> to chat with An0nAI.");
                return SINGLE_SUCCESS;
            }))
        );

        builder.then(literal("list").executes(context -> {
            StringBuilder sb = new StringBuilder("Providers: ");
            for (An0nAI.AiProvider p : An0nAI.AiProvider.values()) {
                sb.append(p.name().toLowerCase()).append(" ");
            }
            info(sb.toString().trim());
            return SINGLE_SUCCESS;
        }));

        builder.executes(context -> {
            An0nAI ai = An0nAI.get();
            if (ai.isConfigured()) {
                info("Connected to (highlight)" + ai.getProvider().getDisplayName() + " (default)with model (highlight)" + ai.getModel());
            } else {
                info("Not configured. Use: .aiconnect <provider> <key>");
                info("Providers: openai, anthropic, groq, deepseek, mistral, openrouter, ollama, custom");
            }
            return SINGLE_SUCCESS;
        });
    }
}
