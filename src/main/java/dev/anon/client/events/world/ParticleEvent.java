/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.world;

import dev.anon.client.events.Cancellable;
import net.minecraft.core.particles.ParticleOptions;

public class ParticleEvent extends Cancellable {
    private static final ParticleEvent INSTANCE = new ParticleEvent();

    public ParticleOptions particle;

    public static ParticleEvent get(ParticleOptions particle) {
        INSTANCE.setCancelled(false);
        INSTANCE.particle = particle;
        return INSTANCE;
    }
}
