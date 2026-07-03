/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.render;

import dev.anon.client.events.packets.PacketEvent;
import dev.anon.client.events.world.TickEvent;
import dev.anon.client.settings.DoubleSetting;
import dev.anon.client.settings.Setting;
import dev.anon.client.settings.SettingGroup;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

public class TimeChanger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> time = sgGeneral.add(new DoubleSetting.Builder()
        .name("time")
        .description("The specified time to be set.")
        .defaultValue(0)
        .sliderRange(-20000, 20000)
        .build()
    );

    long oldTime;

    public TimeChanger() {
        super(Categories.Render, "time-changer", "Makes you able to set a custom time.");
    }

    @Override
    public void onActivate() {
        oldTime = mc.level.getGameTime();
    }

    @Override
    public void onDeactivate() {
        mc.level.getLevelData().setGameTime(oldTime);
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof ClientboundSetTimePacket packet) {
            oldTime = packet.gameTime();
            event.cancel();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        mc.level.getLevelData().setGameTime(time.get().longValue());
    }
}
