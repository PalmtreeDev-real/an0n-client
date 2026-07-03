/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.authlib.GameProfile;
import dev.anon.client.mixininterface.IChatListener;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin implements IChatListener {
    @Unique
    private GameProfile sender;

    @Inject(method = "showMessageToPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addPlayerMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", shift = At.Shift.BEFORE))
    private void onShowMessageToPlayer_beforeAddMessage(ChatType.Bound boundChatType, PlayerChatMessage message, Component decoratedMessage, GameProfile sender, boolean onlyShowSecure, Instant received, CallbackInfoReturnable<Boolean> cir) {
        this.sender = sender;
    }

    @Inject(method = "showMessageToPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addPlayerMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", shift = At.Shift.AFTER))
    private void onShowMessageToPlayer_afterAddMessage(ChatType.Bound boundChatType, PlayerChatMessage message, Component decoratedMessage, GameProfile sender, boolean onlyShowSecure, Instant received, CallbackInfoReturnable<Boolean> cir) {
        this.sender = null;
    }

    @Override
    public GameProfile anon$getSender() {
        return sender;
    }
}
