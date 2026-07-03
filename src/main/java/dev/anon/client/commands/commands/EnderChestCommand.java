/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anon.client.commands.Command;
import dev.anon.client.utils.Utils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EnderChestCommand extends Command {
    public EnderChestCommand() {
        super("ender-chest", "Allows you to preview memory of your ender chest.", "ec", "echest");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(_ -> {
            Utils.openContainer(Items.ENDER_CHEST.getDefaultInstance(), new ItemStack[27], true);
            return SINGLE_SUCCESS;
        });
    }
}
