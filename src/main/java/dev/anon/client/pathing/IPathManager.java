/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.pathing;

import dev.anon.client.settings.Setting;
import dev.anon.client.settings.Settings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;

import java.util.function.Predicate;

public interface IPathManager {
    String getName();

    boolean isPathing();

    void pause();

    void resume();

    void stop();

    default void moveTo(BlockPos pos) {
        moveTo(pos, false);
    }

    void moveTo(BlockPos pos, boolean ignoreY);

    void moveInDirection(float yaw);

    void mine(Block... blocks);

    void follow(Predicate<Entity> entity);

    float getTargetYaw();

    float getTargetPitch();

    ISettings getSettings();

    interface ISettings {
        Settings get();

        Setting<Boolean> getWalkOnWater();

        Setting<Boolean> getWalkOnLava();

        Setting<Boolean> getStep();

        Setting<Boolean> getNoFall();

        void save();
    }
}
