/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.render;

import dev.anon.client.events.render.Render3DEvent;
import dev.anon.client.events.world.TickEvent;
import dev.anon.client.renderer.ShapeMode;
import dev.anon.client.settings.ColorSetting;
import dev.anon.client.settings.EnumSetting;
import dev.anon.client.settings.Setting;
import dev.anon.client.settings.SettingGroup;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.utils.entity.EntityUtils;
import dev.anon.client.utils.entity.SortPriority;
import dev.anon.client.utils.entity.TargetUtils;
import dev.anon.client.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class CityESP extends Module {
    private final SettingGroup sgRender = settings.createGroup("Render");

    // Render

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the rendering.")
        .defaultValue(new SettingColor(225, 0, 0, 75))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the rendering.")
        .defaultValue(new SettingColor(225, 0, 0, 255))
        .build()
    );

    private BlockPos target;

    public CityESP() {
        super(Categories.Render, "city-esp", "Displays blocks that can be broken in order to city another player.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Player targetEntity = TargetUtils.getPlayerTarget(mc.player.blockInteractionRange() + 2, SortPriority.LowestDistance);

        if (TargetUtils.isBadTarget(targetEntity, mc.player.blockInteractionRange() + 2)) {
            target = null;
        } else {
            target = EntityUtils.getCityBlock(targetEntity);
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (target == null) return;

        event.renderer.box(target, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }
}
