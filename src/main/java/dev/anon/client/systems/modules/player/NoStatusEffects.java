/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.player;

import dev.anon.client.settings.Setting;
import dev.anon.client.settings.SettingGroup;
import dev.anon.client.settings.StatusEffectListSetting;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

import static net.minecraft.world.effect.MobEffects.*;

public class NoStatusEffects extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<MobEffect>> blockedEffects = sgGeneral.add(new StatusEffectListSetting.Builder()
        .name("blocked-effects")
        .description("Effects to block.")
        .defaultValue(
            LEVITATION.value(),
            JUMP_BOOST.value(),
            SLOW_FALLING.value(),
            DOLPHINS_GRACE.value()
        )
        .build()
    );

    public NoStatusEffects() {
        super(Categories.Player, "no-status-effects", "Blocks specified status effects.");
    }

    public boolean shouldBlock(MobEffect effect) {
        return isActive() && blockedEffects.get().contains(effect);
    }
}
