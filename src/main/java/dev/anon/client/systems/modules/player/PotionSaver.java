/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.player;

import dev.anon.client.settings.BoolSetting;
import dev.anon.client.settings.Setting;
import dev.anon.client.settings.SettingGroup;
import dev.anon.client.settings.StatusEffectListSetting;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.utils.player.PlayerUtils;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

import static net.minecraft.world.effect.MobEffects.*;

public class PotionSaver extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<MobEffect>> effects = sgGeneral.add(new StatusEffectListSetting.Builder()
        .name("effects")
        .description("The effects to preserve.")
        .defaultValue(
            STRENGTH.value(),
            ABSORPTION.value(),
            RESISTANCE.value(),
            FIRE_RESISTANCE.value(),
            SPEED.value(),
            HASTE.value(),
            REGENERATION.value(),
            WATER_BREATHING.value(),
            SATURATION.value(),
            LUCK.value(),
            SLOW_FALLING.value(),
            DOLPHINS_GRACE.value(),
            CONDUIT_POWER.value(),
            HERO_OF_THE_VILLAGE.value()
        )
        .build()
    );

    public final Setting<Boolean> onlyWhenStationary = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-stationary")
        .description("Only freezes effects when you aren't moving.")
        .defaultValue(false)
        .build()
    );

    public PotionSaver() {
        super(Categories.Player, "potion-saver", "Stops potion effects ticking when you stand still.");
    }

    public boolean shouldFreeze(MobEffect effect) {
        return isActive() && (!onlyWhenStationary.get() || !PlayerUtils.isMoving()) && !mc.player.getActiveEffects().isEmpty() && effects.get().contains(effect);
    }
}
