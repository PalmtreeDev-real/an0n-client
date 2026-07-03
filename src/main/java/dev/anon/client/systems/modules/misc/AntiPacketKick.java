/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.misc;

import dev.anon.client.settings.BoolSetting;
import dev.anon.client.settings.Setting;
import dev.anon.client.settings.SettingGroup;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;

public class AntiPacketKick extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> catchExceptions = sgGeneral.add(new BoolSetting.Builder()
        .name("catch-exceptions")
        .description("Drops corrupted packets.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> logExceptions = sgGeneral.add(new BoolSetting.Builder()
        .name("log-exceptions")
        .description("Logs caught exceptions.")
        .defaultValue(true)
        .visible(catchExceptions::get)
        .build()
    );

    public AntiPacketKick() {
        super(Categories.Misc, "anti-packet-kick", "Attempts to prevent you from being disconnected by large packets.");
    }

    public boolean catchExceptions() {
        return isActive() && catchExceptions.get();
    }
}
