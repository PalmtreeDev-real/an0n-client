/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.tabs.builtin;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.tabs.Tab;
import dev.anon.client.gui.tabs.TabScreen;
import dev.anon.client.gui.tabs.WindowTabScreen;
import dev.anon.client.gui.widgets.containers.WHorizontalList;
import dev.anon.client.gui.widgets.containers.WTable;
import dev.anon.client.gui.widgets.input.WTextBox;
import dev.anon.client.gui.widgets.pressable.WMinus;
import dev.anon.client.gui.widgets.pressable.WPlus;
import dev.anon.client.systems.friends.Friend;
import dev.anon.client.systems.friends.Friends;
import dev.anon.client.utils.misc.NbtUtils;
import dev.anon.client.utils.network.AnonExecutor;
import net.minecraft.client.gui.screens.Screen;

import static dev.anon.client.AnonClient.mc;

public class FriendsTab extends Tab {
    public FriendsTab() {
        super("Friends");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new FriendsScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof FriendsScreen;
    }

    private static class FriendsScreen extends WindowTabScreen {
        public FriendsScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override
        public void initWidgets() {
            WTable table = add(theme.table()).expandX().minWidth(400).widget();
            initTable(table);

            add(theme.horizontalSeparator()).expandX();

            // New
            WHorizontalList list = add(theme.horizontalList()).expandX().widget();

            WTextBox nameW = list.add(theme.textBox("", (_, c) -> c != ' ')).expandX().widget();
            nameW.setFocused(true);

            WPlus add = list.add(theme.plus()).widget();
            add.action = () -> {
                String name = nameW.get().trim();
                Friend friend = new Friend(name);

                if (Friends.get().add(friend)) {
                    nameW.set("");
                    initTable(table);
                    nameW.setFocused(true);

                    AnonExecutor.execute(() -> {
                        friend.updateInfo();
                        mc.execute(() -> {
                            initTable(table);
                            nameW.setFocused(true);
                        });
                    });
                }
            };

            enterAction = add.action;
        }

        private void initTable(WTable table) {
            table.clear();
            if (Friends.get().isEmpty()) return;

            Friends.get().forEach(friend ->
                AnonExecutor.execute(() -> {
                    if (friend.headTextureNeedsUpdate()) {
                        friend.updateInfo();
                    }
                })
            );

            for (Friend friend : Friends.get()) {
                table.add(theme.texture(32, 32, friend.getHead().needsRotate() ? 90 : 0, friend.getHead()));
                table.add(theme.label(friend.getName()));

                WMinus remove = table.add(theme.minus()).expandCellX().right().widget();
                remove.action = () -> {
                    Friends.get().remove(friend);
                    initTable(table);
                };

                table.row();
            }
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Friends.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Friends.get());
        }
    }
}
