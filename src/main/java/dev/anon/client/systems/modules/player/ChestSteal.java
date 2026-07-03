/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.player;

import dev.anon.client.events.game.OpenScreenEvent;
import dev.anon.client.settings.BoolSetting;
import dev.anon.client.settings.IntSetting;
import dev.anon.client.settings.Setting;
import dev.anon.client.settings.SettingGroup;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.utils.player.InvUtils;
import dev.anon.client.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ChestSteal extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay in milliseconds between moving each stack.")
        .defaultValue(20)
        .min(0)
        .sliderMax(500)
        .build()
    );

    private final Setting<Boolean> autoClose = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-close")
        .description("Automatically close the chest after stealing everything.")
        .defaultValue(true)
        .build()
    );

    public ChestSteal() {
        super(Categories.Player, "chest-steal", "Steals every item from a chest at once.");
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!(event.screen instanceof AbstractContainerScreen<?>)) return;

        new Thread(() -> {
            while (isActive() && mc.screen != null) {
                AbstractContainerMenu menu = mc.player.containerMenu;
                int containerEnd = SlotUtils.indexToId(SlotUtils.MAIN_START);
                if (containerEnd == -1) break;

                boolean moved = false;

                for (int i = 0; i < containerEnd; i++) {
                    if (!isActive() || mc.screen == null || mc.player.containerMenu != menu) return;
                    if (!menu.getSlot(i).hasItem()) continue;

                    if (delay.get() > 0) {
                        try {
                            Thread.sleep(delay.get());
                        } catch (InterruptedException e) {
                            return;
                        }
                    }

                    if (!isActive() || mc.screen == null || mc.player.containerMenu != menu) return;

                    InvUtils.shiftClick().slotId(i);
                    moved = true;
                }

                if (!moved) break;
            }

            if (autoClose.get() && isActive() && mc.screen != null) {
                mc.player.closeContainer();
            }
        }).start();
    }
}
