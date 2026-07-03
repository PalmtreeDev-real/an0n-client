/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.macros;

import dev.anon.client.AnonClient;
import dev.anon.client.events.anon.KeyInputEvent;
import dev.anon.client.events.anon.MouseClickEvent;
import dev.anon.client.systems.System;
import dev.anon.client.systems.Systems;
import dev.anon.client.utils.misc.NbtUtils;
import dev.anon.client.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Macros extends System<Macros> implements Iterable<Macro> {
    private List<Macro> macros = new ArrayList<>();

    public Macros() {
        super("macros");
    }

    public static Macros get() {
        return Systems.get(Macros.class);
    }

    public void add(Macro macro) {
        macros.add(macro);
        AnonClient.EVENT_BUS.subscribe(macro);
        save();
    }

    public Macro get(String name) {
        for (Macro macro : macros) {
            if (macro.name.get().equalsIgnoreCase(name)) return macro;
        }

        return null;
    }

    public List<Macro> getAll() {
        return macros;
    }

    public void remove(Macro macro) {
        if (macros.remove(macro)) {
            AnonClient.EVENT_BUS.unsubscribe(macro);
            save();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onKey(KeyInputEvent event) {
        if (event.action == KeyAction.Release) return;

        for (Macro macro : macros) {
            if (macro.onAction(true, event.key(), event.modifiers())) return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onMouse(MouseClickEvent event) {
        if (event.action == KeyAction.Release) return;

        for (Macro macro : macros) {
            if (macro.onAction(false, event.button(), 0)) return;
        }
    }

    public boolean isEmpty() {
        return macros.isEmpty();
    }

    @Override
    public @NotNull Iterator<Macro> iterator() {
        return macros.iterator();
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("macros", NbtUtils.listToTag(macros));
        return tag;
    }

    @Override
    public Macros fromTag(CompoundTag tag) {
        for (Macro macro : macros) AnonClient.EVENT_BUS.unsubscribe(macro);

        macros = NbtUtils.listFromTag(tag.getListOrEmpty("macros"), Macro::new);

        for (Macro macro : macros) AnonClient.EVENT_BUS.subscribe(macro);
        return this;
    }
}
