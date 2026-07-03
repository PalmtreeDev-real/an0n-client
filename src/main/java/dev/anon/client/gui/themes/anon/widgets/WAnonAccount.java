/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.themes.anon.widgets;

import dev.anon.client.gui.WidgetScreen;
import dev.anon.client.gui.themes.anon.AnonWidget;
import dev.anon.client.gui.widgets.WAccount;
import dev.anon.client.systems.accounts.Account;
import dev.anon.client.utils.render.color.Color;

public class WAnonAccount extends WAccount implements AnonWidget {
    public WAnonAccount(WidgetScreen screen, Account<?> account) {
        super(screen, account);
    }

    @Override
    protected Color loggedInColor() {
        return theme().loggedInColor.get();
    }

    @Override
    protected Color accountTypeColor() {
        return theme().textSecondaryColor.get();
    }
}
