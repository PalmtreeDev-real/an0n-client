/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.anon.client.utils.PreInit;

public class FullScreenRenderer {
    public static GpuBuffer vbo;
    public static GpuBuffer ibo;

    /**
     * Deprecated for performance reasons, use {@link MeshRenderer#fullscreen()} or the {@link FullScreenRenderer#vbo}
     * and {@link FullScreenRenderer#ibo} buffer objects instead.
     */
    @Deprecated(forRemoval = true)
    public static MeshBuilder mesh;

    private FullScreenRenderer() {
    }

    @PreInit
    public static void init() {
        mesh = new MeshBuilder(AnonVertexFormats.POS2, VertexFormat.Mode.TRIANGLES, 4, 6);

        mesh.begin();

        mesh.quad(
            mesh.vec2(-1, -1).next(),
            mesh.vec2(-1, 1).next(),
            mesh.vec2(1, 1).next(),
            mesh.vec2(1, -1).next()
        );

        mesh.end();

        vbo = mesh.getVertexBuffer();
        ibo = mesh.getIndexBuffer();
    }
}
