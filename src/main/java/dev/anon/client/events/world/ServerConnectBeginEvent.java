/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.world;

import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;

public class ServerConnectBeginEvent {
    private static final ServerConnectBeginEvent INSTANCE = new ServerConnectBeginEvent();
    public ServerAddress address;
    public ServerData info;

    public static ServerConnectBeginEvent get(ServerAddress address, ServerData info) {
        INSTANCE.address = address;
        INSTANCE.info = info;
        return INSTANCE;
    }
}
