package dev.anon.client.systems.ai;

import com.google.gson.annotations.SerializedName;
import dev.anon.client.events.game.ReceiveMessageEvent;
import dev.anon.client.systems.System;
import dev.anon.client.systems.Systems;
import dev.anon.client.utils.network.AnonExecutor;
import dev.anon.client.utils.network.Http;
import dev.anon.client.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class An0nAI extends System<An0nAI> {
    private static final int MAX_CHAT_HISTORY = 100;
    private static final int MAX_CONVERSATION_TURNS = 10;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    private AiProvider provider = AiProvider.OpenAI;
    private String apiKey = "";
    private String model = "";
    private String customEndpoint = "";

    private final LinkedList<ChatMessage> chatHistory = new LinkedList<>();
    private final LinkedList<ConversationTurn> conversationHistory = new LinkedList<>();

    public An0nAI() {
        super("an0n-ai");
    }

    public static An0nAI get() {
        return Systems.get(An0nAI.class);
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        String text = event.getMessage().getString();
        addChatMessage(text);
    }

    private void addChatMessage(String text) {
        synchronized (chatHistory) {
            chatHistory.addLast(new ChatMessage(Instant.now(), text));
            while (chatHistory.size() > MAX_CHAT_HISTORY) {
                chatHistory.removeFirst();
            }
        }
    }

    public List<ChatMessage> getRecentChat(int count) {
        synchronized (chatHistory) {
            if (chatHistory.isEmpty()) return List.of();
            int from = Math.max(0, chatHistory.size() - count);
            return new ArrayList<>(chatHistory.subList(from, chatHistory.size()));
        }
    }

    public void clearChatHistory() {
        synchronized (chatHistory) {
            chatHistory.clear();
        }
    }

    public void clearConversation() {
        synchronized (conversationHistory) {
            conversationHistory.clear();
        }
    }

    public AiProvider getProvider() { return provider; }
    public String getApiKey() { return apiKey; }
    public String getModel() { return model; }
    public String getCustomEndpoint() { return customEndpoint; }

    public void setProvider(AiProvider provider) {
        this.provider = provider;
        if (model.isEmpty() || !provider.getModels().contains(model)) {
            model = provider.getDefaultModel();
        }
        save();
    }

    public void setApiKey(String apiKey) { this.apiKey = apiKey; save(); }
    public void setModel(String model) { this.model = model; save(); }
    public void setCustomEndpoint(String customEndpoint) { this.customEndpoint = customEndpoint; save(); }

    public boolean isConfigured() { return provider != null && !apiKey.isEmpty(); }

    public boolean autoDetectOllama() {
        try {
            String body = Http.get("http://localhost:11434/api/tags")
                .header("User-Agent", "An0nAI/1.0")
                .sendString();
            if (body == null || body.isBlank()) return false;
            if (!body.contains("models") && !body.contains("name")) return false;

            String firstModel = null;
            int nameIdx = body.indexOf("\"name\"");
            if (nameIdx != -1) {
                int start = body.indexOf('"', nameIdx + 7);
                if (start != -1) {
                    start++;
                    int end = body.indexOf('"', start);
                    if (end != -1) {
                        firstModel = body.substring(start, end);
                    }
                }
            }

            provider = AiProvider.Ollama;
            apiKey = "ollama";
            customEndpoint = "http://localhost:11434/v1/chat/completions";
            if (firstModel != null) {
                model = firstModel;
            } else {
                model = provider.getDefaultModel();
            }
            save();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getEffectiveEndpoint() {
        if (provider == AiProvider.Custom && !customEndpoint.isEmpty()) return customEndpoint;
        return provider.getEndpoint();
    }

    public String fetchUrl(String url) {
        try {
            String result = Http.get(url)
                .header("User-Agent", "An0nAI/1.0")
                .sendString();

            if (result == null || result.isBlank()) {
                return "Error: No content returned from " + url;
            }

            if (result.length() > 5000) {
                result = result.substring(0, 5000) + "\n... [truncated]";
            }

            return "Content from " + url + ":\n" + result;
        } catch (Exception e) {
            return "Error fetching " + url + ": " + e.getMessage();
        }
    }

    public void sendMessage(String message) {
        if (!isConfigured()) {
            ChatUtils.errorPrefix("An0nAI", "AI not configured. Use .aiconnect <provider> <key> to set up.");
            return;
        }

        AnonExecutor.execute(() -> {
            try {
                processMessage(message);
            } catch (Exception e) {
                ChatUtils.errorPrefix("An0nAI", "Error: " + e.getMessage());
            }
        });
    }

    private void processMessage(String userMessage) {
        String usedModel = model.isEmpty() ? provider.getDefaultModel() : model;
        String endpoint = getEffectiveEndpoint();

        if (endpoint.isEmpty() && provider == AiProvider.Custom) {
            ChatUtils.errorPrefix("An0nAI", "Custom provider needs an endpoint.");
            return;
        }

        synchronized (conversationHistory) {
            conversationHistory.addLast(new ConversationTurn("user", userMessage));
            while (conversationHistory.size() > MAX_CONVERSATION_TURNS * 2) {
                conversationHistory.removeFirst();
            }
        }

        String systemPrompt = buildSystemPrompt();
        String aiResponse = callApiWithHistory(provider, endpoint, apiKey, usedModel, systemPrompt);

        if (aiResponse == null) {
            ChatUtils.errorPrefix("An0nAI", "API returned no response.");
            return;
        }

        String fetchUrl = extractFetchUrl(aiResponse);
        if (fetchUrl != null) {
            ChatUtils.forceNextPrefixClass(An0nAI.class);
            ChatUtils.sendMsg(0, "An0nAI", ChatFormatting.LIGHT_PURPLE, ChatFormatting.GRAY, "Fetching: " + fetchUrl);

            String fetchResult = fetchUrl(fetchUrl);

            String followUpPrompt = "You previously requested to fetch: " + fetchUrl
                + "\n\nHere is the result:\n" + fetchResult
                + "\n\nNow answer the user's original question based on this data."
                + "\nUser's original message: " + userMessage;

            String finalResponse = callApiWithHistory(provider, endpoint, apiKey, usedModel, buildSystemPrompt(), followUpPrompt);

            if (finalResponse != null) {
                ChatUtils.forceNextPrefixClass(An0nAI.class);
                ChatUtils.sendMsg(0, "An0nAI", ChatFormatting.LIGHT_PURPLE, ChatFormatting.WHITE, finalResponse);
                synchronized (conversationHistory) {
                    conversationHistory.addLast(new ConversationTurn("assistant", finalResponse));
                }
                return;
            }
        }

        ChatUtils.forceNextPrefixClass(An0nAI.class);
        ChatUtils.sendMsg(0, "An0nAI", ChatFormatting.LIGHT_PURPLE, ChatFormatting.WHITE, aiResponse);
        synchronized (conversationHistory) {
            conversationHistory.addLast(new ConversationTurn("assistant", aiResponse));
        }
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are An0nAI, an AI assistant integrated into a Minecraft utility mod. ")
           .append("Keep answers concise and relevant to the user's Minecraft context. ")
           .append("You can read recent in-game chat messages and fetch web content. ");

        List<ChatMessage> recent = getRecentChat(30);
        if (!recent.isEmpty()) {
            sb.append("\n\nRecent chat messages (newest first):\n");
            List<ChatMessage> reversed = new ArrayList<>(recent);
            Collections.reverse(reversed);
            for (ChatMessage msg : reversed) {
                String time = TIME_FORMATTER.format(msg.timestamp);
                sb.append("[").append(time).append("] ").append(msg.text).append("\n");
            }
        }

        sb.append("\n\nIf you need to fetch information from the web, respond with exactly:\n")
           .append("[FETCH: url]\n")
           .append("And the system will fetch the URL and give you the result so you can answer the user.\n")
           .append("Only use [FETCH: url] for one URL at a time.");

        return sb.toString();
    }

    private String extractFetchUrl(String response) {
        int idx = response.indexOf("[FETCH:");
        if (idx == -1) return null;
        int start = response.indexOf("http", idx);
        if (start == -1) {
            start = response.indexOf("'", idx + 7);
            if (start == -1) start = response.indexOf('"', idx + 7);
            if (start != -1) {
                start++;
                int end = response.indexOf(start == idx + 8 ? '\'' : '"', start);
                if (end != -1) {
                    String url = response.substring(start, end).trim();
                    if (!url.isEmpty() && (url.startsWith("http://") || url.startsWith("https://"))) {
                        return url;
                    }
                }
            }
            return null;
        }
        int end = response.indexOf(']', idx);
        if (end == -1) end = response.indexOf('\n', start);
        if (end == -1) end = response.indexOf(' ', start + 200);
        if (end == -1 || end <= start) end = Math.min(start + 500, response.length());
        String url = response.substring(start, end).trim()
            .replace("]", "").replace(")", "").replace("'", "").replace("\"", "");
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return null;
    }

    // API calling with history support

    private String callApiWithHistory(AiProvider prov, String endpoint, String key, String usedModel, String systemPrompt) {
        return callApiWithHistory(prov, endpoint, key, usedModel, systemPrompt, null);
    }

    private String callApiWithHistory(AiProvider prov, String endpoint, String key, String usedModel, String systemPrompt, String additionalUserMessage) {
        return switch (prov) {
            case Anthropic -> callAnthropicWithHistory(endpoint, key, usedModel, systemPrompt, additionalUserMessage);
            default -> callOpenAIWithHistory(endpoint, key, usedModel, systemPrompt, additionalUserMessage);
        };
    }

    // OpenAI-compatible with conversation history

    private String callOpenAIWithHistory(String endpoint, String apiKey, String model, String systemPrompt, String additionalUserMessage) {
        try {
            StringBuilder messages = new StringBuilder();
            messages.append("{\"role\":\"system\",\"content\":\"").append(escapeJson(systemPrompt)).append("\"}");

            synchronized (conversationHistory) {
                for (ConversationTurn turn : conversationHistory) {
                    messages.append(",");
                    messages.append("{\"role\":\"").append(turn.role).append("\",\"content\":\"").append(escapeJson(turn.content)).append("\"}");
                }
            }

            if (additionalUserMessage != null) {
                messages.append(",");
                messages.append("{\"role\":\"user\",\"content\":\"").append(escapeJson(additionalUserMessage)).append("\"}");
            }

            String json = "{\"model\":\"" + model + "\",\"messages\":[" + messages + "],\"max_tokens\":4096,\"stream\":false}";

            String body = Http.post(endpoint)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyString(json)
                .sendString();

            if (body == null) return null;
            return parseOpenAIResponse(body);
        } catch (Exception e) {
            ChatUtils.errorPrefix("An0nAI", "API error: " + e.getMessage());
            return null;
        }
    }

    // Anthropic with conversation history

    private String callAnthropicWithHistory(String endpoint, String apiKey, String model, String systemPrompt, String additionalUserMessage) {
        try {
            StringBuilder messages = new StringBuilder();

            synchronized (conversationHistory) {
                for (ConversationTurn turn : conversationHistory) {
                    if (messages.length() > 0) messages.append(",");
                    messages.append("{\"role\":\"").append(turn.role).append("\",\"content\":\"").append(escapeJson(turn.content)).append("\"}");
                }
            }

            if (additionalUserMessage != null) {
                if (messages.length() > 0) messages.append(",");
                messages.append("{\"role\":\"user\",\"content\":\"").append(escapeJson(additionalUserMessage)).append("\"}");
            }

            String json = "{\"model\":\"" + model + "\",\"system\":\"" + escapeJson(systemPrompt) + "\",\"max_tokens\":4096,\"messages\":[" + messages + "]}";

            String body = Http.post(endpoint)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("Content-Type", "application/json")
                .bodyString(json)
                .sendString();

            if (body == null) return null;
            return parseAnthropicResponse(body);
        } catch (Exception e) {
            ChatUtils.errorPrefix("An0nAI", "API error: " + e.getMessage());
            return null;
        }
    }

    // Parsers (unchanged)

    private static String parseOpenAIResponse(String json) {
        try {
            int choicesIdx = json.indexOf("\"choices\"");
            if (choicesIdx == -1) return "Error: unexpected response format";
            int contentIdx = json.indexOf("\"content\"", choicesIdx);
            if (contentIdx == -1) return "Error: unexpected response format";
            int start = json.indexOf('"', contentIdx + 10);
            if (start == -1) return "Error: unexpected response format";
            start += 1;
            int end = json.indexOf('"', start);
            if (end == -1) return json.substring(start);
            return json.substring(start, end)
                .replace("\\n", "\n").replace("\\\"", "\"")
                .replace("\\\\", "\\").replace("\\t", "\t").replace("\\r", "\r");
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    private static String parseAnthropicResponse(String json) {
        try {
            int contentIdx = json.indexOf("\"text\"");
            if (contentIdx == -1) {
                int contentBlockIdx = json.indexOf("\"content\"");
                if (contentBlockIdx != -1) {
                    int textIdx = json.indexOf("\"text\"", contentBlockIdx + 10);
                    if (textIdx != -1) contentIdx = textIdx;
                }
            }
            if (contentIdx == -1) return "Error: unexpected response format";
            int start = json.indexOf('"', contentIdx + 7);
            if (start == -1) return "Error: unexpected response format";
            start += 1;
            int end = json.indexOf('"', start);
            if (end == -1) return json.substring(start);
            return json.substring(start, end)
                .replace("\\n", "\n").replace("\\\"", "\"")
                .replace("\\\\", "\\");
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // NBT persistence

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("provider", provider.name());
        tag.putString("apiKey", apiKey);
        tag.putString("model", model);
        tag.putString("customEndpoint", customEndpoint);
        return tag;
    }

    @Override
    public An0nAI fromTag(CompoundTag tag) {
        String providerName = tag.getStringOr("provider", "OpenAI");
        for (AiProvider p : AiProvider.values()) {
            if (p.name().equalsIgnoreCase(providerName)) {
                provider = p;
                break;
            }
        }
        apiKey = tag.getStringOr("apiKey", "");
        model = tag.getStringOr("model", provider.getDefaultModel());
        customEndpoint = tag.getStringOr("customEndpoint", "");
        return this;
    }

    // Data classes

    public record ChatMessage(Instant timestamp, String text) {}

    private record ConversationTurn(String role, String content) {}

    public enum AiProvider {
        OpenAI("OpenAI", "https://api.openai.com/v1/chat/completions", "gpt-4o", "gpt-4o", "gpt-4o-mini", "gpt-4", "gpt-3.5-turbo"),
        Anthropic("Anthropic", "https://api.anthropic.com/v1/messages", "claude-sonnet-4-20250514", "claude-sonnet-4-20250514", "claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022"),
        Groq("Groq", "https://api.groq.com/openai/v1/chat/completions", "llama-3.3-70b-versatile", "llama-3.3-70b-versatile", "llama-3.1-8b-instant", "mixtral-8x7b-32768"),
        DeepSeek("DeepSeek", "https://api.deepseek.com/v1/chat/completions", "deepseek-chat", "deepseek-chat", "deepseek-reasoner"),
        Mistral("Mistral", "https://api.mistral.ai/v1/chat/completions", "mistral-large-latest", "mistral-large-latest", "mistral-medium-latest", "mistral-small-latest"),
        OpenRouter("OpenRouter", "https://openrouter.ai/api/v1/chat/completions", "auto", "auto", "gpt-4o", "claude-sonnet-4-20250514"),
        Ollama("Ollama", "http://localhost:11434/v1/chat/completions", "llama3", "llama3", "llama3.1", "llama3.2", "mistral", "codellama", "mixtral", "qwen2.5", "deepseek-coder"),
        Custom("Custom", "", "custom", "custom");

        private final String displayName;
        private final String endpoint;
        private final String defaultModel;
        private final List<String> models;

        AiProvider(String displayName, String endpoint, String defaultModel, String... models) {
            this.displayName = displayName;
            this.endpoint = endpoint;
            this.defaultModel = defaultModel;
            this.models = List.of(models);
        }

        public String getDisplayName() { return displayName; }
        public String getEndpoint() { return endpoint; }
        public String getDefaultModel() { return defaultModel; }
        public List<String> getModels() { return models; }
    }
}
