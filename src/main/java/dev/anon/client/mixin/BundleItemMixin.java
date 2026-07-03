/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.AnonClient;
import dev.anon.client.events.render.TooltipDataEvent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BundleItem.class)
public abstract class BundleItemMixin {
    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    private void onGetTooltipImage(ItemStack bundle, CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        TooltipDataEvent event = AnonClient.EVENT_BUS.post(TooltipDataEvent.get(bundle));
        if (event.tooltipData != null) {
            cir.setReturnValue(Optional.of(event.tooltipData));
        }
    }
}
