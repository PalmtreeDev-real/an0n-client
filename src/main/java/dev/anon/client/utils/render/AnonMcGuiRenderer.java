/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.render;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

import java.util.List;

public class AnonMcGuiRenderer extends GuiRenderer {
    public AnonMcGuiRenderer(GuiRenderState renderState, MultiBufferSource.BufferSource bufferSource, SubmitNodeCollector submitNodeCollector, FeatureRenderDispatcher featureRenderDispatcher, List<PictureInPictureRenderer<?>> pictureInPictureRenderers) {
        super(renderState, bufferSource, submitNodeCollector, featureRenderDispatcher, pictureInPictureRenderers);
    }
}
