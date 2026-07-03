/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.net.URI;

@Mixin(CreditsAndAttributionScreen.class)
public class CreditsAndAttributionScreenMixin {
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addToFooter(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onInit(CallbackInfo ci, LinearLayout contents) {
        Screen screen = (Screen)(Object)this;

        contents.addChild(new StringWidget(Component.literal(""), Minecraft.getInstance().font));
        contents.addChild(new StringWidget(Component.literal("AN0N"), Minecraft.getInstance().font));
        contents.addChild(Button.builder(
            Component.literal("AN0N Credits"),
            ConfirmLinkScreen.confirmLink(screen, URI.create("https://github.com/PalmtreeDev-real/an0n-client"))
        ).width(210).build());
        contents.addChild(Button.builder(
            Component.literal("AN0N License"),
            ConfirmLinkScreen.confirmLink(screen, URI.create("https://www.apache.org/licenses/LICENSE-2.0"))
        ).width(210).build());
    }
}
