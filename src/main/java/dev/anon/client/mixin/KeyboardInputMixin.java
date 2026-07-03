/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.movement.Sneak;
import dev.anon.client.systems.modules.render.Freecam;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    @Inject(method = "tick", at = @At("TAIL"))
    private void isPressed(CallbackInfo ci) {
        if (Modules.get().get(Sneak.class).doVanilla() || Modules.get().get(Freecam.class).staySneaking())
            keyPresses = new Input(
                keyPresses.forward(),
                keyPresses.backward(),
                keyPresses.left(),
                keyPresses.right(),
                keyPresses.jump(),
                true,
                keyPresses.sprint()
            );
    }
}
