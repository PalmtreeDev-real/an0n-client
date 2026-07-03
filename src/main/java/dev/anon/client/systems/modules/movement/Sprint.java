/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.movement;

import dev.anon.client.events.packets.PacketEvent;
import dev.anon.client.events.world.TickEvent;
import dev.anon.client.settings.BoolSetting;
import dev.anon.client.settings.EnumSetting;
import dev.anon.client.settings.Setting;
import dev.anon.client.settings.SettingGroup;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class Sprint extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public enum Mode {
        Strict,
        Rage
    }

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("sprint-mode")
        .description("What mode of sprinting.")
        .defaultValue(Mode.Strict)
        .build()
    );

    private final Setting<Boolean> keepSprint = sgGeneral.add(new BoolSetting.Builder()
        .name("keep-sprint")
        .description("Whether to keep sprinting after attacking.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> unsprintOnHit = sgGeneral.add(new BoolSetting.Builder()
        .name("unsprint-on-hit")
        .description("Whether to stop sprinting before attacking, to ensure you get crits and sweep attacks.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> unsprintInWater = sgGeneral.add(new BoolSetting.Builder()
        .name("unsprint-in-water")
        .description("Whether to stop sprinting when in water.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Rage)
        .build()
    );

    private final Setting<Boolean> permaSprint = sgGeneral.add(new BoolSetting.Builder()
        .name("sprint-while-stationary")
        .description("Sprint even when not moving.")
        .defaultValue(false)
        .visible(() -> mode.get() == Mode.Rage)
        .build()
    );

    public Sprint() {
        super(Categories.Movement, "sprint", "Automatically sprints.");
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTickMovement(TickEvent.Post event) {
        if (unsprintInWater.get() && mc.player.isInWater()) return;

        mc.player.setSprinting(shouldSprint());
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPacketSend(PacketEvent.Send event) {
        if (!unsprintOnHit.get()) return;
        if (!(event.packet instanceof ServerboundAttackPacket)) return;

        mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
        mc.player.setSprinting(false);
    }

    @EventHandler
    private void onPacketSent(PacketEvent.Sent event) {
        if (!unsprintOnHit.get() || !keepSprint.get()) return;
        if (!(event.packet instanceof ServerboundAttackPacket)) return;
        if (!shouldSprint() || mc.player.isSprinting()) return;

        mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
        mc.player.setSprinting(true);
    }

    public boolean shouldSprint() {
        if (mc.screen != null && !Modules.get().get(GUIMove.class).sprint.get()) return false;

        float movement = mode.get() == Mode.Rage
            ? (Math.abs(mc.player.zza) + Math.abs(mc.player.xxa))
            : mc.player.zza;

        if (movement <= (mc.player.isUnderWater() ? 1.0E-5F : 0.8)) {
            if (mode.get() == Mode.Strict || !permaSprint.get()) return false;
        }

        boolean strictSprint = !(mc.player.isInShallowWater())
            && !mc.player.isMobilityRestricted()
            && mc.player.isPassenger() ? (mc.player.getVehicle().canSprint() && mc.player.getVehicle().isLocalInstanceAuthoritative()) : mc.player.getFoodData().hasEnoughFood()
                                                                                                                                         && (!mc.player.horizontalCollision || mc.player.minorHorizontalCollision);

        return isActive() && (mode.get() == Mode.Rage || strictSprint);
    }

    public boolean rageSprint() {
        return isActive() && mode.get() == Mode.Rage;
    }

    public boolean unsprintInWater() {
        return isActive() && unsprintInWater.get();
    }

    public boolean stopSprinting() {
        return !isActive() || !keepSprint.get();
    }
}
