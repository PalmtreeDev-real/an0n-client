/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.world;

public enum Dimension {
    Overworld,
    Nether,
    End;

    public Dimension opposite() {
        return switch (this) {
            case Overworld -> Nether;
            case Nether -> Overworld;
            default -> this;
        };
    }

    public String toString() {
        return switch (this) {
            case Nether -> "minecraft:the_nether";
            case End -> "minecraft:the_end";
            default -> "minecraft:overworld";
        };
    }
}
