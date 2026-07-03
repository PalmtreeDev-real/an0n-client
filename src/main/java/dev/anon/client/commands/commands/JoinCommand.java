/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anon.client.commands.Command;
import dev.anon.client.utils.network.Http;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinCommand extends Command {
    public JoinCommand() {
        super("join", "Try to join a player's server by their username.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(argument("username", StringArgumentType.word()).executes(context -> {
            String username = context.getArgument("username", String.class);

            // Resolve UUID from Mojang API
            ProfileResponse response = Http.get("https://api.mojang.com/users/profiles/minecraft/" + username)
                .exceptionHandler(e -> error("Could not contact Mojang API."))
                .sendJson(ProfileResponse.class);

            if (response == null) {
                error("Player not found.");
                return SINGLE_SUCCESS;
            }

            // Format UUID with dashes
            String id = response.id();
            String uuidFormatted = id.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            UUID uuid = UUID.fromString(uuidFormatted);

            // Check if on same server
            boolean found = false;
            if (mc.getConnection() != null) {
                for (var player : mc.getConnection().getOnlinePlayers()) {
                    if (player.getProfile().id().equals(uuid)) {
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                info(username + " is on this server!");
            } else {
                info("UUID: " + uuidFormatted);
                info("Fetching NameMC profile...");

                try {
                    String html = Http.get("https://namemc.com/profile/" + id)
                        .ignoreExceptions()
                        .sendString();

                    if (html != null) {
                        Pattern pattern = Pattern.compile(
                            "play\\.[\\w.-]+\\.(?:net|com|org|us|io|gg|co|me)|" +
                            "[\\w-]+\\.(?:net|com|org|us|io|gg|co|me)\\b(?![^<]*>)"
                        );
                        Matcher matcher = pattern.matcher(html);
                        if (matcher.find()) {
                            info("Found server: " + matcher.group());
                        } else {
                            info("NameMC: https://namemc.com/profile/" + id);
                            info("Remote server lookup not available. Join manually.");
                        }
                    } else {
                        info("NameMC: https://namemc.com/profile/" + id);
                    }
                } catch (Exception e) {
                    info("NameMC: https://namemc.com/profile/" + id);
                }
            }

            return SINGLE_SUCCESS;
        }));
    }

    private record ProfileResponse(String id, String name) {}
}
