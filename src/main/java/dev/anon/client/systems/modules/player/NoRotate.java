/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.player;

import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import dev.anon.client.mixin.ClientPacketListenerMixin;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @see ClientPacketListenerMixin#onHandleMovePlayerHead(ClientboundPlayerPositionPacket, CallbackInfo, LocalFloatRef, LocalFloatRef)
 */
public class NoRotate extends Module {
    public NoRotate() {
        super(Categories.Player, "no-rotate", "Attempts to block rotations sent from server to client.");
    }
}
