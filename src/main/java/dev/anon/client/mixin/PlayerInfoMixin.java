/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.authlib.GameProfile;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.player.NameProtect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin {
    @Shadow
    public abstract GameProfile getProfile();

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void onGetTexture(CallbackInfoReturnable<PlayerSkin> cir) {
        if (getProfile().name().equals(Minecraft.getInstance().getUser().getName())) {
            if (Modules.get().get(NameProtect.class).skinProtect()) {
                cir.setReturnValue(DefaultPlayerSkin.get(getProfile()));
            }
        }
    }
}
