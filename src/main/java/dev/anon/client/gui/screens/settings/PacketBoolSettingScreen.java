/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.screens.settings;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.screens.settings.base.CollectionListSettingScreen;
import dev.anon.client.gui.widgets.WWidget;
import dev.anon.client.settings.PacketListSetting;
import dev.anon.client.settings.Setting;
import dev.anon.client.utils.network.PacketUtils;
import net.minecraft.network.protocol.Packet;

import java.util.Set;
import java.util.function.Predicate;

public class PacketBoolSettingScreen extends CollectionListSettingScreen<Class<? extends Packet<?>>> {
    public PacketBoolSettingScreen(GuiTheme theme, Setting<Set<Class<? extends Packet<?>>>> setting) {
        super(theme, "Select Packets", setting, setting.get(), PacketUtils.PACKETS);
    }

    @Override
    protected boolean includeValue(Class<? extends Packet<?>> value) {
        Predicate<Class<? extends Packet<?>>> filter = ((PacketListSetting) setting).filter;

        if (filter == null) return true;
        return filter.test(value);
    }

    @Override
    protected WWidget getValueWidget(Class<? extends Packet<?>> value) {
        return theme.label(PacketUtils.getName(value));
    }

    @Override
    protected String[] getValueNames(Class<? extends Packet<?>> value) {
        return new String[]{
            PacketUtils.getName(value),
            value.getSimpleName()
        };
    }
}
