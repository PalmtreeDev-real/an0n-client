/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.entity.player;

import dev.anon.client.events.Cancellable;

/**
 * Some of our other injections coming from {@link net.minecraft.client.Minecraft#startUseItem()}
 * (e.g. InteractItemEvent) are called twice because the method loops over the Mainhand and the Offhand. This event is
 * only called once, before any interaction logic is called.
 */
public class DoItemUseEvent extends Cancellable {
    private static final DoItemUseEvent INSTANCE = new DoItemUseEvent();

    public static DoItemUseEvent get() {
        return INSTANCE;
    }
}
