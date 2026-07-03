/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.world;

import java.net.InetSocketAddress;

public class ServerConnectEndEvent {
    private static final ServerConnectEndEvent INSTANCE = new ServerConnectEndEvent();
    public InetSocketAddress address;

    public static ServerConnectEndEvent get(InetSocketAddress address) {
        INSTANCE.address = address;
        return INSTANCE;
    }
}
