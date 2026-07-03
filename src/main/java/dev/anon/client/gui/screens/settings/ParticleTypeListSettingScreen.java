/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.screens.settings;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.screens.settings.base.CollectionListSettingScreen;
import dev.anon.client.gui.widgets.WWidget;
import dev.anon.client.settings.Setting;
import dev.anon.client.utils.misc.Names;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

public class ParticleTypeListSettingScreen extends CollectionListSettingScreen<ParticleType<?>> {
    public ParticleTypeListSettingScreen(GuiTheme theme, Setting<List<ParticleType<?>>> setting) {
        super(theme, "Select Particles", setting, setting.get(), BuiltInRegistries.PARTICLE_TYPE);
    }

    @Override
    protected WWidget getValueWidget(ParticleType<?> value) {
        return theme.label(Names.get(value));
    }

    @Override
    protected String[] getValueNames(ParticleType<?> value) {
        return new String[]{
            Names.get(value),
            BuiltInRegistries.PARTICLE_TYPE.getKey(value).toString()
        };
    }
}
