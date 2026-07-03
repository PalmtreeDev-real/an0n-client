/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.mixininterface.ISlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$SlotWrapper")
public abstract class CreativeSlotMixin implements ISlot {
    @Shadow
    @Final
    private Slot target;

    @Override
    public int anon$getIndex() {
        return target.index;
    }

    @Override
    public int anon$getSlot() {
        return target.getContainerSlot();
    }
}
