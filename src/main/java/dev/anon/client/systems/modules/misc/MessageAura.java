/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.misc;

import dev.anon.client.events.entity.EntityAddedEvent;
import dev.anon.client.settings.BoolSetting;
import dev.anon.client.settings.Setting;
import dev.anon.client.settings.SettingGroup;
import dev.anon.client.settings.StringSetting;
import dev.anon.client.systems.friends.Friends;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.player.Player;

public class MessageAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> message = sgGeneral.add(new StringSetting.Builder()
        .name("message")
        .description("The specified message sent to the player.")
        .defaultValue("AN0N on Crack!")
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Will not send any messages to people friended.")
        .defaultValue(false)
        .build()
    );

    public MessageAura() {
        super(Categories.Misc, "message-aura", "Sends a specified message to any player that enters render distance.");
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (!(event.entity instanceof Player) || event.entity.getUUID().equals(mc.player.getUUID())) return;

        if (!ignoreFriends.get() || (ignoreFriends.get() && !Friends.get().isFriend((Player) event.entity))) {
            ChatUtils.sendPlayerMsg("/msg " + event.entity.getName().getString() + " " + message.get());
        }
    }
}
