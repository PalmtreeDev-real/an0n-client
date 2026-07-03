/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.systems.config.Config;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Mixin(SplashManager.class)
public abstract class SplashManagerMixin {
    @Unique
    private boolean override = true;
    @Unique
    private static final Random random = new Random();
    @Unique
    private final List<String> anonSplashes = getAnonSplashes();

    @Inject(method = "getSplash", at = @At("HEAD"), cancellable = true)
    private void onApply(CallbackInfoReturnable<SplashRenderer> cir) {
        if (Config.get() == null || !Config.get().titleScreenSplashes.get()) return;

        if (override)
            cir.setReturnValue(new SplashRenderer(Component.literal(anonSplashes.get(random.nextInt(anonSplashes.size())))));
        override = !override;
    }

    @Unique
    private static List<String> getAnonSplashes() {
        return List.of(
            "AN0N on Crack!",
            "Star AN0N Client on GitHub!",
            "Based utility mod.",
            "§6MineGame159 §fbased god",
            "§4anonclient.com",
            "§4AN0N on Crack!",
            "§6AN0N on Crack!"
        );
    }

}
