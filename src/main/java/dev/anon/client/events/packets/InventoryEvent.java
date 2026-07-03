/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.packets;

import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;

public class InventoryEvent {
    private static final InventoryEvent INSTANCE = new InventoryEvent();

    public ClientboundContainerSetContentPacket packet;

    public static InventoryEvent get(ClientboundContainerSetContentPacket packet) {
        INSTANCE.packet = packet;
        return INSTANCE;
    }
}
