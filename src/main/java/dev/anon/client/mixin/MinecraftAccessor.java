/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.ResourceLoadStateTracker;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.server.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("fps")
    static int anon$getFps() {
        return 0;
    }

    @Mutable
    @Accessor("user")
    void anon$setUser(User session);

    @Accessor("reloadStateTracker")
    ResourceLoadStateTracker anon$getReloadStateTracker();

    @Accessor("missTime")
    int anon$getMissTime();

    @Accessor("missTime")
    void anon$setMissTime(int attackCooldown);

    @Invoker("startAttack")
    boolean anon$leftClick();

    @Mutable
    @Accessor("profileKeyPairManager")
    void anon$setProfileKeyPairManager(ProfileKeyPairManager keys);

    @Mutable
    @Accessor("userApiService")
    void anon$setUserApiService(UserApiService apiService);

    @Mutable
    @Accessor("skinManager")
    void anon$setSkinManager(SkinManager skinProvider);

    @Mutable
    @Accessor("playerSocialManager")
    void anon$setPlayerSocialManager(PlayerSocialManager socialInteractionsManager);

    @Mutable
    @Accessor("reportingContext")
    void anon$setReportingContext(ReportingContext abuseReportContext);

    @Mutable
    @Accessor("profileFuture")
    void anon$setProfileFuture(CompletableFuture<ProfileResult> future);

    @Mutable
    @Accessor("services")
    void anon$setServices(Services apiServices);

    @Invoker("handleKeybinds")
    void anon$handleInputEvents();
}
