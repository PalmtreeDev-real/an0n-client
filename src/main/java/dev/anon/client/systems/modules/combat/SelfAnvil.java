/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.combat;

import dev.anon.client.events.game.OpenScreenEvent;
import dev.anon.client.events.world.TickEvent;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.utils.player.InvUtils;
import dev.anon.client.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;

public class SelfAnvil extends Module {
    public SelfAnvil() {
        super(Categories.Combat, "self-anvil", "Automatically places an anvil on you to prevent other players from going into your hole.");
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof AnvilScreen) event.cancel();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (BlockUtils.place(mc.player.blockPosition().offset(0, 2, 0), InvUtils.findInHotbar(itemStack -> Block.byItem(itemStack.getItem()) instanceof AnvilBlock), 0)) {
            toggle();
        }
    }
}
