package dev.anon.client.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.anon.client.commands.Command;
import dev.anon.client.systems.ai.An0nAI;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AiCommand extends Command {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    public AiCommand() {
        super("ai", "An0nAI: message, connect provider, or view context.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(literal("message")
            .then(argument("message", StringArgumentType.greedyString()).executes(context -> {
                String message = context.getArgument("message", String.class);
                sendToAi(message);
                return SINGLE_SUCCESS;
            }))
        );

        builder.then(literal("ollama").executes(context -> {
            info("Detecting local Ollama...");
            boolean found = An0nAI.get().autoDetectOllama();
            if (found) {
                info("Found Ollama at localhost:11434 (model: " + An0nAI.get().getModel() + ").");
                info("Use .ai message <msg> to chat.");
            } else {
                error("Could not find Ollama. Is it running on localhost:11434?");
            }
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("connect")
            .then(argument("provider", StringArgumentType.word())
                .then(argument("key", StringArgumentType.greedyString()).executes(context -> {
                    String provider = context.getArgument("provider", String.class);
                    String key = context.getArgument("key", String.class);
                    connectProvider(provider, key);
                    return SINGLE_SUCCESS;
                }))
            )
            .executes(context -> {
                An0nAI ai = An0nAI.get();
                if (ai.isConfigured()) {
                    info("Connected to (highlight)" + ai.getProvider().getDisplayName() + " (default)with model (highlight)" + ai.getModel());
                } else {
                    info("Usage: .ai connect <provider> <key>");
                    info("Providers: openai, anthropic, groq, deepseek, mistral, openrouter, ollama, custom");
                }
                return SINGLE_SUCCESS;
            })
        );

        builder.then(literal("context").executes(context -> {
            An0nAI ai = An0nAI.get();
            var msgs = ai.getRecentChat(20);
            if (msgs.isEmpty()) {
                info("No recent chat messages.");
            } else {
                info("--- Recent Chat (last " + msgs.size() + " messages) ---");
                for (An0nAI.ChatMessage msg : msgs) {
                    String time = TIME_FMT.format(msg.timestamp());
                    info("[" + time + "] " + msg.text());
                }
            }
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("clear").executes(context -> {
            An0nAI.get().clearConversation();
            info("Conversation history cleared.");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("clear-chat").executes(context -> {
            An0nAI.get().clearChatHistory();
            info("Chat history cleared.");
            return SINGLE_SUCCESS;
        }));

        builder.executes(context -> {
            info("Usage: .ai message <msg> | .ai connect <provider> <key> | .ai ollama | .ai context | .ai clear");
            return SINGLE_SUCCESS;
        });
    }

    private void sendToAi(String message) {
        An0nAI ai = An0nAI.get();
        if (!ai.isConfigured()) {
            error("An0nAI not configured. Use .ai connect <provider> <key> first.");
            return;
        }
        info("Thinking...");
        ai.sendMessage(message);
    }

    private void connectProvider(String providerStr, String key) {
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
            return;
        }

        ai.setProvider(selected);
        ai.setApiKey(key);

        if ((selected == An0nAI.AiProvider.Ollama || key.startsWith("http://") || key.startsWith("https://"))
            && (key.startsWith("http://") || key.startsWith("https://"))) {
            ai.setCustomEndpoint(key);
            if (selected == An0nAI.AiProvider.Custom || selected == An0nAI.AiProvider.Ollama) {
                ai.setApiKey("ollama");
            }
            info("Connected to endpoint: " + key);
            info("Default model: " + ai.getModel());
            return;
        }

        info("Connected to " + selected.getDisplayName() + " (model: " + selected.getDefaultModel() + ").");
        info("Use .ai message <msg> to chat.");
    }
}
