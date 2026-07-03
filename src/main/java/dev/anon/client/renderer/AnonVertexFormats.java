/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.renderer;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public abstract class AnonVertexFormats {
    public static final VertexFormat POS2 = VertexFormat.builder()
        .add("Position", AnonVertexFormatElements.POS2)
        .build();

    public static final VertexFormat POS2_COLOR = VertexFormat.builder()
        .add("Position", AnonVertexFormatElements.POS2)
        .add("Color", VertexFormatElement.COLOR)
        .build();

    public static final VertexFormat POS2_TEXTURE_COLOR = VertexFormat.builder()
        .add("Position", AnonVertexFormatElements.POS2)
        .add("Texture", VertexFormatElement.UV)
        .add("Color", VertexFormatElement.COLOR)
        .build();

    private AnonVertexFormats() {}
}
