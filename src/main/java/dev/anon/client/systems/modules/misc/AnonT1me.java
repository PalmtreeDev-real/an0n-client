/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.misc;

import dev.anon.client.events.packets.PacketEvent;
import dev.anon.client.settings.EnumSetting;
import dev.anon.client.settings.Setting;
import dev.anon.client.settings.SettingGroup;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;

public class AnonT1me extends Module {
    public enum ProtocolVersion {
        v1_10("1.10", 210),
        v1_11("1.11", 315),
        v1_11_1("1.11.1", 316),
        v1_12("1.12", 335),
        v1_12_1("1.12.1", 338),
        v1_12_2("1.12.2", 340),
        v1_13("1.13", 393),
        v1_13_1("1.13.1", 401),
        v1_13_2("1.13.2", 404),
        v1_14("1.14", 477),
        v1_14_1("1.14.1", 480),
        v1_14_2("1.14.2", 485),
        v1_14_3("1.14.3", 490),
        v1_14_4("1.14.4", 498),
        v1_15("1.15", 573),
        v1_15_1("1.15.1", 575),
        v1_15_2("1.15.2", 578),
        v1_16("1.16", 735),
        v1_16_1("1.16.1", 736),
        v1_16_2("1.16.2", 751),
        v1_16_3("1.16.3", 753),
        v1_16_4("1.16.4", 754),
        v1_17("1.17", 755),
        v1_17_1("1.17.1", 756),
        v1_18("1.18", 757),
        v1_18_2("1.18.2", 758),
        v1_19("1.19", 759),
        v1_19_1("1.19.1", 760),
        v1_19_3("1.19.3", 761),
        v1_19_4("1.19.4", 762),
        v1_20("1.20", 763),
        v1_20_2("1.20.2", 764),
        v1_20_4("1.20.4", 765),
        v1_20_5("1.20.5", 766),
        v1_21("1.21", 767),
        v1_21_2("1.21.2", 768),
        v26_2("26.2", 770);

        private final String displayName;
        public final int protocol;

        ProtocolVersion(String displayName, int protocol) {
            this.displayName = displayName;
            this.protocol = protocol;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<ProtocolVersion> protocolVersion = sgGeneral.add(new EnumSetting.Builder<ProtocolVersion>()
        .name("version")
        .description("The Minecraft version to spoof.")
        .defaultValue(ProtocolVersion.v1_21)
        .build()
    );

    public AnonT1me() {
        super(Categories.Misc, "an0n-t1me", "Spoofs your client version in the handshake to connect to servers on different versions. Use with ViaFabricPlus for full protocol translation.");

        runInMainMenu = true;
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!isActive()) return;
        if (!(event.packet instanceof ClientIntentionPacket packet)) return;

        @SuppressWarnings("deprecation")
        ClientIntentionPacket spoofed = new ClientIntentionPacket(
            protocolVersion.get().protocol,
            packet.hostName(),
            packet.port(),
            packet.intention()
        );

        event.sendSilently(spoofed);
        event.cancel();
    }
}
