/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.packets;

import net.minecraft.network.protocol.game.ClientboundSoundPacket;

public class PlaySoundPacketEvent {

    private static final PlaySoundPacketEvent INSTANCE = new PlaySoundPacketEvent();

    public ClientboundSoundPacket packet;

    public static PlaySoundPacketEvent get(ClientboundSoundPacket packet) {
        INSTANCE.packet = packet;
        return INSTANCE;
    }
}
