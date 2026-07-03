/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.world;

import dev.anon.client.events.packets.PacketEvent;
import dev.anon.client.mixin.BlockHitResultAccessor;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;

public class BuildHeight extends Module {
    public BuildHeight() {
        super(Categories.World, "build-height", "Allows you to interact with objects at the build limit.");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!(event.packet instanceof ServerboundUseItemOnPacket p)) return;
        if (mc.level == null) return;
        if (p.getHitResult().getLocation().y >= mc.level.getHeight() && p.getHitResult().getDirection() == Direction.UP) {
            ((BlockHitResultAccessor) p.getHitResult()).anon$setDirection(Direction.DOWN);
        }
    }
}
