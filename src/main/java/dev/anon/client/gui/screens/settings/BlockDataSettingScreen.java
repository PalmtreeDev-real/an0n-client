/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.screens.settings;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.renderer.GuiRenderer;
import dev.anon.client.gui.screens.settings.base.CollectionMapSettingScreen;
import dev.anon.client.gui.widgets.WWidget;
import dev.anon.client.gui.widgets.pressable.WButton;
import dev.anon.client.settings.BlockDataSetting;
import dev.anon.client.settings.IBlockData;
import dev.anon.client.utils.misc.IChangeable;
import dev.anon.client.utils.render.DisplayItemUtils;
import dev.anon.client.utils.misc.ICopyable;
import dev.anon.client.utils.misc.ISerializable;
import dev.anon.client.utils.misc.Names;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import static dev.anon.client.AnonClient.mc;

public class BlockDataSettingScreen<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> extends CollectionMapSettingScreen<Block, T> {
    private final BlockDataSetting<T> setting;
    private boolean invalidate;

    public BlockDataSettingScreen(GuiTheme theme, BlockDataSetting<T> setting) {
        super(theme, "Configure Blocks", setting, setting.get(), BuiltInRegistries.BLOCK);

        this.setting = setting;
    }

    @Override
    protected boolean includeValue(Block value) {
        return value != Blocks.AIR;
    }

    @Override
    protected WWidget getValueWidget(Block block) {
        return theme.itemWithLabel(DisplayItemUtils.toStack(block), Names.get(block));
    }

    @Override
    protected WWidget getDataWidget(Block block, @Nullable T blockData) {
        WButton edit = theme.button(GuiRenderer.EDIT);
        edit.action = () -> {
            T data = blockData;
            if (data == null) data = setting.defaultData.get().copy();

            mc.setScreen(data.createScreen(theme, block, setting));
            invalidate = true;
        };
        return edit;
    }

    @Override
    protected void onRenderBefore(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (invalidate) {
            this.invalidateTable();
            invalidate = false;
        }
    }

    @Override
    protected String[] getValueNames(Block block) {
        return new String[]{
            Names.get(block),
            BuiltInRegistries.BLOCK.getKey(block).toString()
        };
    }
}
