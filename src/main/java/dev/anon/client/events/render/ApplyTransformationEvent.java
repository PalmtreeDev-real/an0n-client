/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.render;

import dev.anon.client.events.Cancellable;
import net.minecraft.client.resources.model.cuboid.ItemTransform;

public class ApplyTransformationEvent extends Cancellable {
    private static final ApplyTransformationEvent INSTANCE = new ApplyTransformationEvent();

    public ItemTransform transformation;
    public boolean leftHanded;

    public static ApplyTransformationEvent get(ItemTransform transformation, boolean leftHanded) {
        INSTANCE.setCancelled(false);

        INSTANCE.transformation = transformation;
        INSTANCE.leftHanded = leftHanded;

        return INSTANCE;
    }
}
