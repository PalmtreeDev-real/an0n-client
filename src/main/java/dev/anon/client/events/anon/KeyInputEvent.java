/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.anon;

import dev.anon.client.events.Cancellable;
import dev.anon.client.utils.misc.input.KeyAction;

public class KeyInputEvent extends Cancellable {
    private static final KeyInputEvent INSTANCE = new KeyInputEvent();

    public net.minecraft.client.input.KeyEvent input;
    public KeyAction action;

    public static KeyInputEvent get(net.minecraft.client.input.KeyEvent input, KeyAction action) {
        INSTANCE.setCancelled(false);
        INSTANCE.input = input;
        INSTANCE.action = action;
        return INSTANCE;
    }

    public int key() {
        return INSTANCE.input.key();
    }

    public int modifiers() {
        return INSTANCE.input.modifiers();
    }
}
