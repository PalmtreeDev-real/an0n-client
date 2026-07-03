/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.anon.client.AnonClient;
import dev.anon.client.events.render.Render2DEvent;
import dev.anon.client.gui.WidgetScreen;
import dev.anon.client.systems.hud.screens.HudEditorScreen;
import dev.anon.client.utils.Utils;
import dev.anon.client.utils.render.AnonMcGuiRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.util.profiling.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Unique
    private GuiRenderState renderState;

    @Unique
    private AnonMcGuiRenderer guiRenderer;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init$anon(GuiRenderState renderState, MultiBufferSource.BufferSource bufferSource, SubmitNodeCollector submitNodeCollector, FeatureRenderDispatcher featureRenderDispatcher, List<PictureInPictureRenderer<?>> pictureInPictureRenderers, CallbackInfo ci) {
        if ((GuiRenderer) (Object) this instanceof AnonMcGuiRenderer) return;

        this.renderState = new GuiRenderState();

        guiRenderer = new AnonMcGuiRenderer(
            this.renderState,
            bufferSource,
            submitNodeCollector,
            featureRenderDispatcher,
            pictureInPictureRenderers
        );
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void render$preGui(CallbackInfo ci) {
        if ((GuiRenderer) (Object) this instanceof AnonMcGuiRenderer) return;
        var mc = Minecraft.getInstance();

        if (mc.screen == null || mc.screen instanceof WidgetScreen) return;
        anon$render2D(mc);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void render$postGui(CallbackInfo ci) {
        if ((GuiRenderer) (Object) this instanceof AnonMcGuiRenderer) return;
        var mc = Minecraft.getInstance();

        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(mc.getMainRenderTarget().getDepthTexture(), 1.0);

        if (mc.screen == null || mc.screen instanceof WidgetScreen) {
            anon$render2D(mc);
        }

        guiRenderer.endFrame();
    }

    @Unique
    private void anon$render2D(Minecraft mc) {
        var mouseX = (int) mc.mouseHandler.getScaledXPos(mc.getWindow());
        var mouseY = (int) mc.mouseHandler.getScaledYPos(mc.getWindow());
        var fogRenderer = ((GameRendererAccessor) mc.gameRenderer).anon$fogRenderer();

        if (Utils.canUpdate() || HudEditorScreen.isOpen()) {
            Profiler.get().push(AnonClient.MOD_ID + "_render_2d");
            Utils.unscaledProjection();

            var graphics = new GuiGraphicsExtractor(mc, renderState, mouseX, mouseY);
            var tickDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

            AnonClient.EVENT_BUS.post(Render2DEvent.get(graphics, graphics.guiWidth(), graphics.guiHeight(), tickDelta));
            guiRenderer.render(fogRenderer.getBuffer(FogRenderer.FogMode.NONE));

            Utils.scaledProjection();
            Profiler.get().pop();
        }

        if (mc.screen instanceof WidgetScreen widgetScreen) {
            var graphics = new GuiGraphicsExtractor(mc, renderState, mouseX, mouseY);
            var guiDelta = mc.getDeltaTracker().getGameTimeDeltaTicks();

            widgetScreen.renderCustom(graphics, mouseX, mouseY, guiDelta);
            guiRenderer.render(fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
        }
    }
}
