/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.anon.client.AnonClient;
import dev.anon.client.events.render.RenderBossBarEvent;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.render.NoRender;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noBossBar()) ci.cancel();
    }

    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    public Iterator<LerpingBossEvent> modifyBossBarIterator(Iterator<LerpingBossEvent> original) {
        RenderBossBarEvent.BossIterator event = AnonClient.EVENT_BUS.post(RenderBossBarEvent.BossIterator.get(original));
        return event.iterator;
    }

    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/LerpingBossEvent;getName()Lnet/minecraft/network/chat/Component;"))
    public Component modifyBossBarName(Component original, @Local(name = "event") LerpingBossEvent event) {
        RenderBossBarEvent.BossText bossTextEvent = AnonClient.EVENT_BUS.post(RenderBossBarEvent.BossText.get(event, original));
        return bossTextEvent.name;
    }

    @ModifyConstant(method = "extractRenderState", constant = @Constant(intValue = 9, ordinal = 1))
    public int modifySpacingConstant(int j) {
        RenderBossBarEvent.BossSpacing event = AnonClient.EVENT_BUS.post(RenderBossBarEvent.BossSpacing.get(j));
        return event.spacing;
    }
}
