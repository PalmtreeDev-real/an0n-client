/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.packets;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;

public class ContainerSlotUpdateEvent {
    private static final ContainerSlotUpdateEvent INSTANCE = new ContainerSlotUpdateEvent();

    public ClientboundContainerSetSlotPacket packet;

    public static ContainerSlotUpdateEvent get(ClientboundContainerSetSlotPacket packet) {
        INSTANCE.packet = packet;
        return INSTANCE;
    }
}
