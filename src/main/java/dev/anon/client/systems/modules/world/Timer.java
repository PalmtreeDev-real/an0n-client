/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.world;

import dev.anon.client.settings.DoubleSetting;
import dev.anon.client.settings.Setting;
import dev.anon.client.settings.SettingGroup;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;

public class Timer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> multiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("multiplier")
        .description("The timer multiplier amount.")
        .defaultValue(1)
        .min(0.1)
        .sliderMin(0.1)
        .build()
    );

    public static final double OFF = 1;
    private double override = 1;

    public Timer() {
        super(Categories.World, "timer", "Changes the speed of everything in your game.");
    }

    public double getMultiplier() {
        return override != OFF ? override : (isActive() ? multiplier.get() : OFF);
    }

    public void setOverride(double override) {
        this.override = override;
    }
}
