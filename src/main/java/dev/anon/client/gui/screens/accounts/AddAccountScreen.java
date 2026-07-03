/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.screens.accounts;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.WindowScreen;
import dev.anon.client.gui.widgets.pressable.WButton;

public abstract class AddAccountScreen extends WindowScreen {
    public final AccountsScreen parent;
    public WButton add;
    private int timer;

    protected AddAccountScreen(GuiTheme theme, String title, AccountsScreen parent) {
        super(theme, title);
        this.parent = parent;
    }

    @Override
    public void tick() {
        if (locked) {
            if (timer > 2) {
                add.set(getNext(add));
                timer = 0;
            }
            else {
                timer++;
            }
        }

        else if (!add.getText().equals("Add")) {
            add.set("Add");
        }
    }

    private String getNext(WButton add) {
        return switch (add.getText()) {
            case "Add", "oo0" -> "ooo";
            case "ooo" -> "0oo";
            case "0oo" -> "o0o";
            case "o0o" -> "oo0";
            default -> "Add";
        };
    }
}
