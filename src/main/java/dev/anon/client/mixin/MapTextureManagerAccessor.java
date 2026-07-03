/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MapTextureManager.class)
public interface MapTextureManagerAccessor {
    @Invoker("getOrCreateMapInstance")
    MapTextureManager.MapInstance anon$invokeGetOrCreateMapInstance(MapId id, MapItemSavedData state);
}
