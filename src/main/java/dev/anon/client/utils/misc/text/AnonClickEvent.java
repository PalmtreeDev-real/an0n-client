/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.misc.text;

import dev.anon.client.mixin.ScreenMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class does nothing except ensure that {@link ClickEvent}'s containing AN0N Client commands can only be executed if they come from the client.
 *
 * @see ScreenMixin#onDefaultHandleClickEvent(ClickEvent, Minecraft, Screen, CallbackInfo)
 */
public class AnonClickEvent implements ClickEvent {
    public final String value;

    public AnonClickEvent(String value) {
        this.value = value;
    }

    @Override
    public Action action() {
        return Action.RUN_COMMAND;
    }
}
