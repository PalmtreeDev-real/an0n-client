/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.notebot.instrumentdetect;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.core.BlockPos;

public interface InstrumentDetectFunction {
    /**
     * Detects an instrument for noteblock
     *
     * @param noteBlock Noteblock state
     * @param blockPos  Noteblock position
     * @return Detected instrument
     */
    NoteBlockInstrument detectInstrument(BlockState noteBlock, BlockPos blockPos);
}
