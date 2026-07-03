/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.tabs.builtin;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.GuiThemes;
import dev.anon.client.gui.renderer.GuiRenderer;
import dev.anon.client.gui.tabs.Tab;
import dev.anon.client.gui.tabs.TabScreen;
import dev.anon.client.gui.tabs.WindowTabScreen;
import dev.anon.client.gui.widgets.containers.WHorizontalList;
import dev.anon.client.gui.widgets.input.WDropdown;
import dev.anon.client.gui.widgets.pressable.WButton;
import dev.anon.client.utils.misc.NbtUtils;
import net.minecraft.client.gui.screens.Screen;

import static dev.anon.client.AnonClient.mc;

public class GuiTab extends Tab {
    public GuiTab() {
        super("GUI");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new GuiScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof GuiScreen;
    }

    private static class GuiScreen extends WindowTabScreen {
        public GuiScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);

            theme.settings.onActivated();
        }

        @Override
        public void initWidgets() {
            WHorizontalList opts = add(theme.horizontalList()).expandX().widget();

            opts.add(theme.label("Theme:"));
            WDropdown<String> themeW = opts.add(theme.dropdown(GuiThemes.getNames(), GuiThemes.get().name)).widget();
            themeW.action = () -> {
                GuiThemes.select(themeW.get());

                mc.setScreen(null);
                tab.openScreen(GuiThemes.get());
            };

            WButton resetLayout = opts.add(theme.button("Reset Layout")).expandX().widget();
            resetLayout.action = theme::clearWindowConfigs;

            WButton reset = opts.add(theme.button("Reset Colors")).right().widget();
            reset.action = () -> {
                theme.settings.reset();
                mc.setScreen(null);
                tab.openScreen(GuiThemes.get());
            };

            WButton copyButton = opts.add(theme.button(GuiRenderer.COPY)).widget();
            copyButton.action = this::toClipboard;
            copyButton.tooltip = "Copy config";

            WButton pasteButton = opts.add(theme.button(GuiRenderer.PASTE)).right().widget();
            pasteButton.action = this::fromClipboard;
            pasteButton.tooltip = "Paste config";

            add(theme.settings(theme.settings)).expandX();
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(theme);
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(theme);
        }
    }
}
