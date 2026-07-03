/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixininterface;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;

public interface IChatListener {
    /**
     * Only valid inside of {@link net.minecraft.client.gui.components.ChatComponent#addMessage(Component, MessageSignature, GuiMessageSource, GuiMessageTag)} call
     */
    GameProfile anon$getSender();
}
