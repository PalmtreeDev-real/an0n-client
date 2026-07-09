package dev.anon.client.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.anon.client.commands.Command;
import dev.anon.client.features.chat.ChatManager;
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
                .executes(ctx -> {
                    ChatManager chat = ChatManager.get();

                    if (!chat.isConnected()) {
                        chat.connect();
                        info("Connecting to AN0N Chat...");
                    }

                    if (!chat.isLoggedIn()) {
                        error("Not logged into AN0N Chat yet. Try again in a moment.");
                        return SINGLE_SUCCESS;
                    }

                    String message = ctx.getArgument("message", String.class);
                    chat.sendMessage(message);
                    return SINGLE_SUCCESS;
                })
        );

        builder.then(literal("ban")
            .then(argument("target", StringArgumentType.word())
                .executes(ctx -> punish(chat -> {
                    String t = ctx.getArgument("target", String.class);
                    chat.banUser(t);
                    chat.sendMessage(" --- " + t + " has been permanently banned.");
                    chat.sendMessage(" --- Reason: Administrative action.");
                    chat.sendMessage(" ------------------------------------------------------------------------------------");
                }))
                .then(argument("time", StringArgumentType.word())
                    .executes(ctx -> punish(chat -> {
                        String t = ctx.getArgument("target", String.class);
                        String time = ctx.getArgument("time", String.class);
                        String dur = formatDuration(time);
                        chat.banUser(t);
                        chat.sendMessage(" --- " + t + " has been banned for " + dur + ".");
                        chat.sendMessage(" --- Reason: Administrative action.");
                        chat.sendMessage(" ------------------------------------------------------------------------------------");
                    }))
                )
            )
        );

        builder.then(literal("unban")
            .then(argument("target", StringArgumentType.word())
                .executes(ctx -> punish(chat -> {
                    String t = ctx.getArgument("target", String.class);
                    chat.unbanUser(t);
                    chat.sendMessage(" --- " + t + " has been unbanned.");
                    chat.sendMessage(" --- They may rejoin the community.");
                    chat.sendMessage(" ------------------------------------------------------------------------------------");
                }))
            )
        );

        builder.then(literal("suspend")
            .then(argument("target", StringArgumentType.word())
                .executes(ctx -> punish(chat -> {
                    String t = ctx.getArgument("target", String.class);
                    chat.banUser(t);
                    chat.sendMessage(" --- " + t + " has been suspended from chat.");
                    chat.sendMessage(" --- The AI is watching.");
                    chat.sendMessage(" ------------------------------------------------------------------------------------");
                }))
                .then(argument("time", StringArgumentType.word())
                    .executes(ctx -> punish(chat -> {
                        String t = ctx.getArgument("target", String.class);
                        String time = ctx.getArgument("time", String.class);
                        String dur = formatDuration(time);
                        chat.banUser(t);
                        chat.sendMessage(" --- " + t + " has been suspended for " + dur + ".");
                        chat.sendMessage(" --- The AI is watching.");
                        chat.sendMessage(" ------------------------------------------------------------------------------------");
                    }))
                )
            )
        );

        builder.then(literal("mute")
            .then(argument("target", StringArgumentType.word())
                .executes(ctx -> punish(chat -> {
                    String t = ctx.getArgument("target", String.class);
                    chat.sendMessage(" --- " + t + " has been muted.");
                    chat.sendMessage(" --- They cannot speak in An0n SocialChat.");
                    chat.sendMessage(" ------------------------------------------------------------------------------------");
                }))
                .then(argument("time", StringArgumentType.word())
                    .executes(ctx -> punish(chat -> {
                        String t = ctx.getArgument("target", String.class);
                        String time = ctx.getArgument("time", String.class);
                        String dur = formatDuration(time);
                        chat.sendMessage(" --- " + t + " has been muted for " + dur + ".");
                        chat.sendMessage(" --- They cannot speak in An0n SocialChat.");
                        chat.sendMessage(" ------------------------------------------------------------------------------------");
                    }))
                )
            )
        );

        builder.then(literal("login")
            .then(argument("password", StringArgumentType.greedyString())
                .executes(ctx -> {
                    ChatManager chat = ChatManager.get();
                    String password = ctx.getArgument("password", String.class);

                    if (!chat.isCracked()) {
                        error("Premium accounts use Mojang authentication.");
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
            )
        );

        builder.then(literal("register")
            .then(argument("password", StringArgumentType.word())
                .executes(ctx -> {
                    ChatManager chat = ChatManager.get();
                    if (!chat.isCracked()) {
                        error("Only cracked users can register a password.");
                        return SINGLE_SUCCESS;
                    }
                    if (chat.isLoggedIn()) {
                        error("You are already logged in.");
                        return SINGLE_SUCCESS;
                    }
                    String pw = ctx.getArgument("password", String.class);
                    chat.registerPassword(pw);
                    info("Registration request sent.");
                    return SINGLE_SUCCESS;
                })
            )
        );

        builder.then(literal("grant")
            .then(argument("target", StringArgumentType.word())
                .executes(ctx -> rootAction(chat -> {
                    String t = ctx.getArgument("target", String.class);
                    chat.grantAdmin(t);
                    info("Granted admin to %s", t);
                }))
            )
        );

        builder.then(literal("revoke")
            .then(argument("target", StringArgumentType.word())
                .executes(ctx -> rootAction(chat -> {
                    String t = ctx.getArgument("target", String.class);
                    chat.revokeAdmin(t);
                    info("Revoked admin from %s", t);
                }))
            )
        );
    }

    private String formatDuration(String input) {
        if (input == null || input.isEmpty()) return null;
        String lower = input.toLowerCase();
        if (lower.endsWith("mm")) {
            String num = lower.substring(0, lower.length() - 2);
            try {
                int v = Integer.parseInt(num);
                if (v == 1) return "1 month";
                return v + " months";
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (lower.endsWith("s")) {
            String num = lower.substring(0, lower.length() - 1);
            try {
                int v = Integer.parseInt(num);
                if (v == 1) return "1 second";
                return v + " seconds";
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (lower.endsWith("m")) {
            String num = lower.substring(0, lower.length() - 1);
            try {
                int v = Integer.parseInt(num);
                if (v == 1) return "1 minute";
                return v + " minutes";
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (lower.endsWith("h")) {
            String num = lower.substring(0, lower.length() - 1);
            try {
                int v = Integer.parseInt(num);
                if (v == 1) return "1 hour";
                return v + " hours";
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (lower.endsWith("d")) {
            String num = lower.substring(0, lower.length() - 1);
            try {
                int v = Integer.parseInt(num);
                if (v == 1) return "1 day";
                return v + " days";
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface PunishAction {
        void run(ChatManager chat);
    }

    private int punish(PunishAction action) {
        ChatManager chat = ChatManager.get();
        if (chat.isCracked()) { error("Cracked accounts cannot use admin commands."); return SINGLE_SUCCESS; }
        if (!chat.isAdmin()) { error("You do not have permission to use this command."); return SINGLE_SUCCESS; }
        if (!chat.isLoggedIn()) { error("Not logged into AN0N Chat."); return SINGLE_SUCCESS; }
        action.run(chat);
        return SINGLE_SUCCESS;
    }

    private int rootAction(PunishAction action) {
        ChatManager chat = ChatManager.get();
        if (chat.isCracked()) { error("Cracked accounts cannot use admin commands."); return SINGLE_SUCCESS; }
        if (!chat.isRootAdmin()) { error("Only the root admin can use this command."); return SINGLE_SUCCESS; }
        if (!chat.isLoggedIn()) { error("Not logged into AN0N Chat."); return SINGLE_SUCCESS; }
        action.run(chat);
        return SINGLE_SUCCESS;
    }
}
