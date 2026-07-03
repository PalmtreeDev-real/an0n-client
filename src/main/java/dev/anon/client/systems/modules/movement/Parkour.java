/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.movement;

import com.google.common.collect.Streams;
import dev.anon.client.events.world.TickEvent;
import dev.anon.client.settings.DoubleSetting;
import dev.anon.client.settings.Setting;
import dev.anon.client.settings.SettingGroup;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.Stream;

public class Parkour extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> edgeDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("edge-distance")
        .description("How far from the edge should you jump.")
        .range(0.001, 0.1)
        .defaultValue(0.001)
        .build()
    );

    public Parkour() {
        super(Categories.Movement, "parkour", "Automatically jumps at the edges of blocks.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!mc.player.onGround() || mc.options.keyJump.isDown()) return;

        if (mc.player.isShiftKeyDown() || mc.options.keyShift.isDown()) return;

        AABB box = mc.player.getBoundingBox();
        AABB adjustedBox = box.move(0, -0.5, 0).inflate(-edgeDistance.get(), 0, -edgeDistance.get());

        Stream<VoxelShape> blockCollisions = Streams.stream(mc.level.getBlockCollisions(mc.player, adjustedBox));

        if (blockCollisions.findAny().isPresent()) return;

        mc.player.jumpFromGround();
    }
}
