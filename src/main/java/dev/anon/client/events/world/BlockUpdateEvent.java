/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.world;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

public class BlockUpdateEvent {
    private static final BlockUpdateEvent INSTANCE = new BlockUpdateEvent();

    public BlockPos pos;
    public BlockState oldState, newState;

    public static BlockUpdateEvent get(BlockPos pos, BlockState oldState, BlockState newState) {
        INSTANCE.pos = pos;
        INSTANCE.oldState = oldState;
        INSTANCE.newState = newState;

        return INSTANCE;
    }
}
