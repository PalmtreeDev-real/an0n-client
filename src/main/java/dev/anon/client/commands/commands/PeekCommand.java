/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.anon.client.commands.Command;
import dev.anon.client.utils.Utils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

public class PeekCommand extends Command {
    private static final ItemStack[] ITEMS = new ItemStack[27];
    private static final SimpleCommandExceptionType CANT_PEEK = new SimpleCommandExceptionType(Component.literal("You must be holding a storage block or looking at an item frame."));

    public PeekCommand() {
        super("peek", "Lets you see what's inside storage block items.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(_ -> {
            if (Utils.openContainer(mc.player.getMainHandItem(), ITEMS, true)) return SINGLE_SUCCESS;
            else if (Utils.openContainer(mc.player.getOffhandItem(), ITEMS, true)) return SINGLE_SUCCESS;
            else if (mc.crosshairPickEntity instanceof ItemFrame itemFrame && Utils.openContainer(itemFrame.getItem(), ITEMS, true))
                return SINGLE_SUCCESS;
            else throw CANT_PEEK.create();
        });
    }
}
