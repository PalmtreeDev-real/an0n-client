/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixininterface;

import com.mojang.authlib.GameProfile;

public interface IGuiMessage {
    String anon$getText();

    int anon$getId();

    void anon$setId(int id);

    GameProfile anon$getSender();

    void anon$setSender(GameProfile profile);
}
