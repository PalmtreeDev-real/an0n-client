/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.tooltip;

import dev.anon.client.mixin.GuiGraphicsExtractorAccessor;
import dev.anon.client.utils.render.CustomBannerGuiElementRenderState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import static dev.anon.client.AnonClient.mc;

public class BannerTooltipComponent implements AnonTooltipData, ClientTooltipComponent {
    private final DyeColor color;
    private final BannerPatternLayers patterns;
    private final BannerFlagModel bannerFlag;

    /**
     * Should only be used when the ItemStack is a banner
     */
    public BannerTooltipComponent(ItemStack banner) {
        this.color = ((BannerItem) banner.getItem()).getColor();
        this.patterns = banner.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        ModelPart modelPart = mc.getEntityModels().bakeLayer(ModelLayers.STANDING_BANNER_FLAG);
        this.bannerFlag = new BannerFlagModel(modelPart);
    }

    public BannerTooltipComponent(DyeColor color, BannerPatternLayers patterns) {
        this.color = color;
        this.patterns = patterns;
        ModelPart modelPart = mc.getEntityModels().bakeLayer(ModelLayers.STANDING_BANNER_FLAG);
        this.bannerFlag = new BannerFlagModel(modelPart);
    }

    @Override
    public ClientTooltipComponent getComponent() {
        return this;
    }

    @Override
    public int getHeight(Font textRenderer) {
        return 40 * 2;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return 20 * 2;
    }

    @Override
    public void extractImage(Font textRenderer, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        var centerX = width / 2 - getWidth(null) / 2;

        GuiGraphicsExtractorAccessor contextAccessor = (GuiGraphicsExtractorAccessor) graphics;

        contextAccessor.getGuiRenderState().addPicturesInPictureState(new CustomBannerGuiElementRenderState(
            bannerFlag, color, patterns,
            centerX + x, y,
            centerX + x + getWidth(null), y + getHeight(null),
            contextAccessor.getScissorStack().peek(),
            16 * 2
        ));
    }
}
