/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.renderer;

import dev.anon.client.utils.misc.Pool;
import dev.anon.client.utils.render.color.Color;

public abstract class GuiRenderOperation<T extends GuiRenderOperation<T>> {
    protected double x, y;
    protected Color color;

    public void set(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    @SuppressWarnings("unchecked")
    public void run(Pool<T> pool) {
        onRun();
        pool.free((T) this);
    }

    protected abstract void onRun();
}
