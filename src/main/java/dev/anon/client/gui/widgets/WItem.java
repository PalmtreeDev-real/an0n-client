/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.widgets;

import dev.anon.client.gui.renderer.GuiRenderer;
import net.minecraft.world.item.ItemStack;

public class WItem extends WWidget {
    protected ItemStack itemStack;

    public WItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    protected void onCalculateSize() {
        double s = theme.scale(32);

        width = s;
        height = s;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!itemStack.isEmpty()) {
            renderer.post(() -> {
                double s = theme.scale(2);
                renderer.item(itemStack, (int) x, (int) y, (float) s, true);
            });
        }
    }

    public void set(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
