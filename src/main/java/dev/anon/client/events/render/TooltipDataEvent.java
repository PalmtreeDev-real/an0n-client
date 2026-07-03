/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.events.render;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;


public class TooltipDataEvent {
    private static final TooltipDataEvent INSTANCE = new TooltipDataEvent();

    public TooltipComponent tooltipData;
    public ItemStack itemStack;

    public static TooltipDataEvent get(ItemStack itemStack) {
        INSTANCE.tooltipData = null;
        INSTANCE.itemStack = itemStack;
        return INSTANCE;
    }
}
