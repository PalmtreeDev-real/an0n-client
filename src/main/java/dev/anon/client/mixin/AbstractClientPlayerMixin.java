/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.utils.misc.FakeClientPlayer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.anon.client.AnonClient.mc;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    // Player model rendering in main menu

    @Inject(method = "getPlayerInfo", at = @At("HEAD"), cancellable = true)
    private void onGetPlayerListEntry(CallbackInfoReturnable<PlayerInfo> cir) {
        if (mc.getConnection() == null) cir.setReturnValue(FakeClientPlayer.getPlayerListEntry());
    }
}
