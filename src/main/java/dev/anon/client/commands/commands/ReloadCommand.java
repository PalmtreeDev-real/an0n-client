/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anon.client.commands.Command;
import dev.anon.client.renderer.Fonts;
import dev.anon.client.systems.Systems;
import dev.anon.client.systems.friends.Friend;
import dev.anon.client.systems.friends.Friends;
import dev.anon.client.utils.network.Capes;
import dev.anon.client.utils.network.AnonExecutor;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "Reloads many systems.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(_ -> {
            warning("Reloading systems, this may take a while.");

            Systems.load();
            Capes.init();
            Fonts.refresh();
            AnonExecutor.execute(() -> Friends.get().forEach(Friend::updateInfo));

            return SINGLE_SUCCESS;
        });
    }
}
