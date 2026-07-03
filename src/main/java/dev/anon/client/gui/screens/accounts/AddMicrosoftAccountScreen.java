/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.screens.accounts;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.widgets.containers.WHorizontalList;
import dev.anon.client.gui.widgets.pressable.WButton;
import dev.anon.client.systems.accounts.MicrosoftLogin;
import dev.anon.client.systems.accounts.types.MicrosoftAccount;

import static dev.anon.client.AnonClient.mc;

public class AddMicrosoftAccountScreen extends AddAccountScreen {
    public AddMicrosoftAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, "Add Microsoft Account", parent);
    }

    @Override
    public void initWidgets() {
        String url = MicrosoftLogin.getRefreshToken(refreshToken -> {

            if (refreshToken != null) {
                MicrosoftAccount account = new MicrosoftAccount(refreshToken);
                AccountsScreen.addAccount(null, parent, account);
            }

            onClose();
        });

        add(theme.label("Please select the account to log into in your browser."));
        add(theme.label("If the link does not automatically open in a few seconds, copy it into your browser."));

        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        WButton copy = l.add(theme.button("Copy link")).expandX().widget();
        copy.action = () -> mc.keyboardHandler.setClipboard(url);

        WButton cancel = l.add(theme.button("Cancel")).expandX().widget();
        cancel.action = () -> {
            MicrosoftLogin.stopServer();
            onClose();
        };
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
