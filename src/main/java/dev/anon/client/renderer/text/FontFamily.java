/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.renderer.text;

import java.util.ArrayList;
import java.util.List;

public class FontFamily {
    private final String name;
    private final List<FontFace> fonts = new ArrayList<>();

    public FontFamily(String name) {
        this.name = name;
    }

    public boolean addFont(FontFace font) {
        return fonts.add(font);
    }

    public boolean hasType(FontInfo.Type type) {
        return get(type) != null;
    }

    public FontFace get(FontInfo.Type type) {
        if (type == null) return null;

        for (FontFace font : fonts) {
            if (font.info.type().equals(type)) {
                return font;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }
}
