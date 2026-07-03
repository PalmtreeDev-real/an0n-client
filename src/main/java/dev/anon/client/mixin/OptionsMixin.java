/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.AnonClient;
import dev.anon.client.events.game.ChangePerspectiveEvent;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.render.Freecam;
import dev.anon.client.utils.misc.input.KeyBinds;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Shadow
    @Final
    @Mutable
    public KeyMapping[] keyMappings;

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;keyMappings:[Lnet/minecraft/client/KeyMapping;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void onInitAfterKeysAll(Minecraft minecraft, File workingDirectory, CallbackInfo ci) {
        keyMappings = KeyBinds.apply(keyMappings);
    }

    @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
    private void setPerspective(CameraType cameraType, CallbackInfo ci) {
        if (Modules.get() == null) return; // nothing is loaded yet, shouldersurfing compat

        ChangePerspectiveEvent event = AnonClient.EVENT_BUS.post(ChangePerspectiveEvent.get(cameraType));

        if (event.isCancelled()) ci.cancel();

        if (Modules.get().isActive(Freecam.class)) ci.cancel();
    }
}
