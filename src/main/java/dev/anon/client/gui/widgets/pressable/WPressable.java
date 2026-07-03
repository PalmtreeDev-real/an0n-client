/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.widgets.pressable;

import dev.anon.client.gui.widgets.WWidget;
import net.minecraft.client.input.MouseButtonEvent;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public abstract class WPressable extends WWidget {
    public Runnable action;

    protected boolean pressed;

    @Override
    public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
        if (mouseOver && (click.button() == GLFW_MOUSE_BUTTON_LEFT || click.button() == GLFW_MOUSE_BUTTON_RIGHT))
            pressed = true;
        return pressed;
    }

    @Override
    public boolean onMouseReleased(MouseButtonEvent click) {
        if (pressed) {
            onPressed(click.button());
            if (action != null) action.run();

            pressed = false;
        }

        return false;
    }

    protected void onPressed(int button) {
    }
}
