/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixininterface;

import net.minecraft.network.chat.Component;

public interface IChatHud {
    void anon$add(Component message, int id);
}
