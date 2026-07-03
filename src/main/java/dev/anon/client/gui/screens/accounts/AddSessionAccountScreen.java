/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.screens.accounts;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.widgets.containers.WTable;
import dev.anon.client.gui.widgets.input.WTextBox;
import dev.anon.client.systems.accounts.types.SessionAccount;

public class AddSessionAccountScreen extends AddAccountScreen {
    public AddSessionAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, "Add Session Account", parent);
    }

    @Override
    public void initWidgets() {
        WTable t = add(theme.table()).widget();

        // Access token
        t.add(theme.label("Access Token: "));
        WTextBox token = t.add(theme.textBox("")).minWidth(400).expandX().widget();
        token.setFocused(true);
        t.row();

        // Add
        add = t.add(theme.button("Add")).expandX().widget();
        add.action = () -> {
            if (!token.get().isEmpty()) {
                SessionAccount account = new SessionAccount(token.get());
                AccountsScreen.addAccount(this, parent, account);
            }
        };

        enterAction = add.action;
    }
}
