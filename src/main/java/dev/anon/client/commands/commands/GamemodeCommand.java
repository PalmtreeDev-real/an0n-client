/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anon.client.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.level.GameType;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        super("gamemode", "Changes your gamemode client-side.", "gm");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        for (GameType gameMode : GameType.values()) {
            builder.then(literal(gameMode.getName()).executes(_ -> {
                mc.gameMode.setLocalMode(gameMode);
                return SINGLE_SUCCESS;
            }));
        }
    }
}
