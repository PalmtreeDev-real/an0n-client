/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.tabs;

import dev.anon.client.gui.GuiTheme;
import net.minecraft.client.gui.screens.Screen;

import static dev.anon.client.AnonClient.mc;

public abstract class Tab {
    public final String name;

    public Tab(String name) {
        this.name = name;
    }

    public void openScreen(GuiTheme theme) {
        TabScreen screen = this.createScreen(theme);
        screen.addDirect(theme.topBar()).top().centerX();
        mc.setScreen(screen);
    }

    public abstract TabScreen createScreen(GuiTheme theme);

    public abstract boolean isScreen(Screen screen);
}
