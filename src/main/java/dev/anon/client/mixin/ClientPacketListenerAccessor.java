/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessor {
    @Accessor("serverChunkRadius")
    int anon$getServerChunkRadius();

    @Accessor("signedMessageEncoder")
    SignedMessageChain.Encoder anon$getSignedMessageEncoder();

    @Accessor("lastSeenMessages")
    LastSeenMessagesTracker anon$getLastSeenMessages();

    @Accessor("registryAccess")
    RegistryAccess.Frozen anon$getRegistryAccess();

    @Accessor("enabledFeatures")
    FeatureFlagSet anon$getEnabledFeatures();

    @Accessor("COMMAND_NODE_BUILDER")
    static ClientboundCommandsPacket.NodeBuilder<ClientSuggestionProvider> anon$getCommandNodeFactory() {
        return null;
    }
}
