/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.misc;

import dev.anon.client.events.world.PlaySoundEvent;
import dev.anon.client.settings.Setting;
import dev.anon.client.settings.SettingGroup;
import dev.anon.client.settings.SoundEventListSetting;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

import java.util.List;

public class SoundBlocker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<SoundEvent>> sounds = sgGeneral.add(new SoundEventListSetting.Builder()
        .name("sounds")
        .description("Sounds to block.")
        .build()
    );

    public SoundBlocker() {
        super(Categories.Misc, "sound-blocker", "Cancels out selected sounds.");
    }

    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        for (SoundEvent sound : sounds.get()) {
            if (sound.location().equals(event.sound.getIdentifier())) {
                event.cancel();
                break;
            }
        }
    }

    public boolean shouldBlock(SoundInstance soundInstance) {
        return isActive() && sounds.get().contains(Setting.parseId(BuiltInRegistries.SOUND_EVENT, soundInstance.getIdentifier().getPath()));
    }
}
