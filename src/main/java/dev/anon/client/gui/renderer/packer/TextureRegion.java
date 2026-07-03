/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.renderer.packer;

public class TextureRegion {
    public double x1, y1;
    public double x2, y2;

    public double diagonal;

    public TextureRegion(double width, double height) {
        diagonal = Math.sqrt(width * width + height * height);
    }
}
