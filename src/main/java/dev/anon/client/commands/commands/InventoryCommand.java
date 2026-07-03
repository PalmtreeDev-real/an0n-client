/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anon.client.commands.Command;
import dev.anon.client.commands.arguments.PlayerArgumentType;
import dev.anon.client.utils.Utils;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class InventoryCommand extends Command {
    public InventoryCommand() {
        super("inventory", "Allows you to see parts of another player's inventory.", "inv", "invsee");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(argument("player", PlayerArgumentType.create()).executes(context -> {
            Utils.screenToOpen = new InventoryScreen(PlayerArgumentType.get(context));
            return SINGLE_SUCCESS;
        }));
    }
}
