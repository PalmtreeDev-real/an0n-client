/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.tabs;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.utils.Cell;
import dev.anon.client.gui.widgets.WWidget;
import dev.anon.client.gui.widgets.containers.WWindow;

public abstract class WindowTabScreen extends TabScreen {
    protected final WWindow window;

    public WindowTabScreen(GuiTheme theme, Tab tab) {
        super(theme, tab);

        window = super.add(theme.window(tab.name)).center().widget();
    }

    @Override
    public <W extends WWidget> Cell<W> add(W widget) {
        return window.add(widget);
    }

    @Override
    public void clear() {
        window.clear();
    }
}
