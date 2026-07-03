/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.authlib.GameProfile;
import dev.anon.client.mixininterface.IGuiMessageVisible;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.Line.class)
public abstract class GuiMessageVisibleMixin implements IGuiMessageVisible {
    @Shadow
    @Final
    private FormattedCharSequence content;
    @Unique
    private int id;
    @Unique
    private GameProfile sender;
    @Unique
    private boolean startOfEntry;

    @Override
    public String anon$getText() {
        StringBuilder sb = new StringBuilder();

        content.accept((_, _, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });

        return sb.toString();
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

    @Override
    public boolean anon$isStartOfEntry() {
        return startOfEntry;
    }

    @Override
    public void anon$setStartOfEntry(boolean start) {
        startOfEntry = start;
    }
}
