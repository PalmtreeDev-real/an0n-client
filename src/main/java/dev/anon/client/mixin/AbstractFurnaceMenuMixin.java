/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.mixininterface.IAbstractFurnaceMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceMenu.class)
public abstract class AbstractFurnaceMenuMixin implements IAbstractFurnaceMenu {
    @Shadow
    protected abstract boolean canSmelt(ItemStack itemStack);

    @Override
    public boolean anon$canSmelt(ItemStack itemStack) {
        return canSmelt(itemStack);
    }
}
