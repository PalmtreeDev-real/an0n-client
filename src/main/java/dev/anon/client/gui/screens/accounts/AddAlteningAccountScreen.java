/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.screens.accounts;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.widgets.containers.WTable;
import dev.anon.client.gui.widgets.input.WTextBox;
import dev.anon.client.systems.accounts.types.TheAlteningAccount;

public class AddAlteningAccountScreen extends AddAccountScreen {
    public AddAlteningAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, "Add The Altening Account", parent);
    }

    @Override
    public void initWidgets() {
        WTable t = add(theme.table()).widget();

        // Token
        t.add(theme.label("Token: "));
        WTextBox token = t.add(theme.textBox("")).minWidth(400).expandX().widget();
        token.setFocused(true);
        t.row();

        // Add
        add = t.add(theme.button("Add")).expandX().widget();
        add.action = () -> {
            if (!token.get().isEmpty()) {
                AccountsScreen.addAccount(this, parent, new TheAlteningAccount(token.get()));
            }
        };

        enterAction = add.action;
    }
}
