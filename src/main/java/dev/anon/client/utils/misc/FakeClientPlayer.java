/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.misc;

import com.mojang.authlib.GameProfile;
import dev.anon.client.AnonClient;
import dev.anon.client.utils.PreInit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.Difficulty;

import java.util.UUID;

import static dev.anon.client.AnonClient.mc;

public class FakeClientPlayer {
    private static ClientLevel world;
    private static RemotePlayer player;
    private static PlayerInfo playerListEntry;

    private static UUID lastId;
    private static boolean needsNewEntry;

    private FakeClientPlayer() {
    }

    @PreInit
    public static void init() {
        AnonClient.EVENT_BUS.subscribe(FakeClientPlayer.class);
    }

    public static RemotePlayer getPlayer() {
        UUID id = mc.getUser().getProfileId();

        if (player == null || (!id.equals(lastId))) {
            if (world == null) {
                world = new ClientLevel(
                    new ClientPacketListener(mc, new Connection(PacketFlow.CLIENTBOUND), new CommonListenerCookie(
                        null,
                        new GameProfile(mc.getUser().getProfileId(), mc.getUser().getName()),
                        null,
                        null,
                        null,
                        null,
                        mc.getCurrentServer(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false)
                    ),
                    new ClientLevel.ClientLevelData(Difficulty.NORMAL, false, false),
                    world.dimension(),
                    world.dimensionTypeRegistration(),
                    1,
                    1,
                    null,
                    false,
                    0,
                    world.getSeaLevel()
                );
            }

            player = new RemotePlayer(world, new GameProfile(id, mc.getUser().getName()));

            lastId = id;
            needsNewEntry = true;
        }

        return player;
    }

    public static PlayerInfo getPlayerListEntry() {
        if (playerListEntry == null || needsNewEntry) {
            playerListEntry = new PlayerInfo(new GameProfile(lastId, mc.getUser().getName()), false);
            needsNewEntry = false;
        }

        return playerListEntry;
    }
}
