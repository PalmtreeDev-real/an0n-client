/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.anon.client.AnonClient;
import dev.anon.client.events.render.ApplyTransformationEvent;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemTransform.class)
public abstract class ItemTransformMixin {
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void onApply(boolean applyLeftHandFix, PoseStack.Pose pose, CallbackInfo ci) {
        ApplyTransformationEvent event = AnonClient.EVENT_BUS.post(ApplyTransformationEvent.get((ItemTransform) (Object) this, applyLeftHandFix));
        if (event.isCancelled()) ci.cancel();
    }
}
