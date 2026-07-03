/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.tabs.builtin;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.GuiThemes;
import dev.anon.client.gui.tabs.Tab;
import dev.anon.client.gui.tabs.TabScreen;
import net.minecraft.client.gui.screens.Screen;

public class ModulesTab extends Tab {
    public ModulesTab() {
        super("Modules");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return theme.modulesScreen();
    }

    @Override
    public boolean isScreen(Screen screen) {
        return GuiThemes.get().isModulesScreen(screen);
    }
}
