/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.authlib.GameProfile;
import dev.anon.client.mixininterface.IGuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.class)
public abstract class GuiMessageMixin implements IGuiMessage {
    @Shadow
    @Final
    private Component content;
    @Unique
    private int id;
    @Unique
    private GameProfile sender;

    @Override
    public String anon$getText() {
        return content.getString();
    }

    @Override
    public int anon$getId() {
        return id;
    }

    @Override
    public void anon$setId(int id) {
        this.id = id;
    }

    @Override
    public GameProfile anon$getSender() {
        return sender;
    }

    @Override
    public void anon$setSender(GameProfile profile) {
        sender = profile;
    }
}
