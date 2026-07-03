/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.widgets.pressable;

import dev.anon.client.gui.renderer.packer.GuiTexture;
import net.minecraft.client.input.MouseButtonEvent;

public abstract class WConfirmedButton extends WButton {

    protected boolean pressedOnce = false;
    protected String confirmText;

    public WConfirmedButton(String text, String confirmText, GuiTexture texture) {
        super(text, texture);
        this.confirmText = confirmText;
    }

    @Override
    public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
        boolean pressed = super.onMouseClicked(click, doubled);
        if (!pressed) {
            pressedOnce = false;
            invalidate();
        }
        return pressed;
    }

    @Override
    public boolean onMouseReleased(MouseButtonEvent click) {
        if (pressed && pressedOnce) super.onMouseReleased(click);
        pressedOnce = pressed;
        invalidate();
        return pressed = false;
    }

    @Override
    public String getText() {
        return pressedOnce ? confirmText : text;
    }

    public void set(String text, String confirmText) {
        super.set(text);
        this.confirmText = confirmText;
    }
}
