/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.tooltip;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class TextTooltipComponent extends ClientTextTooltip implements AnonTooltipData {
    public TextTooltipComponent(FormattedCharSequence text) {
        super(text);
    }

    public TextTooltipComponent(Component text) {
        this(text.getVisualOrderText());
    }

    @Override
    public ClientTextTooltip getComponent() {
        return this;
    }
}
