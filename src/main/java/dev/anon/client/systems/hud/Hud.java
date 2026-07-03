/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.hud;

import dev.anon.client.events.anon.CustomFontChangedEvent;
import dev.anon.client.events.render.Render2DEvent;
import dev.anon.client.events.world.TickEvent;
import dev.anon.client.gui.WidgetScreen;
import dev.anon.client.settings.*;
import dev.anon.client.systems.System;
import dev.anon.client.systems.Systems;
import dev.anon.client.systems.hud.elements.*;
import dev.anon.client.systems.hud.elements.keyboard.KeyboardHud;
import dev.anon.client.systems.hud.screens.HudEditorScreen;
import dev.anon.client.utils.Utils;
import dev.anon.client.utils.misc.Keybind;
import dev.anon.client.utils.misc.NbtUtils;
import dev.anon.client.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static dev.anon.client.AnonClient.mc;

public class Hud extends System<Hud> implements Iterable<HudElement> {
    public static final HudGroup GROUP = new HudGroup("AN0N");

    public boolean active;
    public Settings settings = new Settings();

    public final Map<String, HudElementInfo<?>> infos = new TreeMap<>();
    private final List<HudElement> elements = new ArrayList<>();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgEditor = settings.createGroup("Editor");
    private final SettingGroup sgKeybind = settings.createGroup("Bind");

    // General

    private final Setting<Boolean> customFont = sgGeneral.add(new BoolSetting.Builder()
        .name("custom-font")
        .description("Text will use custom font.")
        .defaultValue(true)
        .onChanged(_ -> {
            for (HudElement element : elements) element.onFontChanged();
        })
        .build()
    );

    private final Setting<Boolean> hideInMenus = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-in-menus")
        .description("Hides the an0n hud when in inventory screens or game menus.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> textScale = sgGeneral.add(new DoubleSetting.Builder()
        .name("text-scale")
        .description("Scale of text if not overridden by the element.")
        .defaultValue(1)
        .min(0.5)
        .sliderRange(0.5, 3)
        .build()
    );

    public final Setting<List<SettingColor>> textColors = sgGeneral.add(new ColorListSetting.Builder()
        .name("text-colors")
        .description("Colors used for the Text element.")
        .defaultValue(List.of(new SettingColor(), new SettingColor(175, 175, 175), new SettingColor(25, 225, 25), new SettingColor(225, 25, 25)))
        .build()
    );

    // Editor

    public final Setting<Integer> border = sgEditor.add(new IntSetting.Builder()
        .name("border")
        .description("Space around the edges of the screen.")
        .defaultValue(4)
        .sliderMax(20)
        .build()
    );

    public final Setting<Integer> snappingRange = sgEditor.add(new IntSetting.Builder()
        .name("snapping-range")
        .description("Snapping range in editor.")
        .defaultValue(10)
        .sliderMax(20)
        .build()
    );

    // Keybindings
    @SuppressWarnings("unused")
    private final Setting<Keybind> keybind = sgKeybind.add(new KeybindSetting.Builder()
        .name("bind")
        .defaultValue(Keybind.none())
        .action(() -> active = !active)
        .build()
    );

    private boolean resetToDefaultElements;

    public Hud() {
        super("hud");
    }

    public static Hud get() {
        return Systems.get(Hud.class);
    }

    @Override
    public void init() {
        settings.registerColorSettings(null);

        register(AnonTextHud.INFO);
        register(ItemHud.INFO);
        register(InventoryHud.INFO);
        register(CompassHud.INFO);
        register(ArmorHud.INFO);
        register(HoleHud.INFO);
        register(PlayerModelHud.INFO);
        register(ActiveModulesHud.INFO);
        register(LagNotifierHud.INFO);
        register(PlayerRadarHud.INFO);
        register(ModuleInfosHud.INFO);
        register(PotionTimersHud.INFO);
        register(CombatHud.INFO);
        register(MapHud.INFO);
        register(KeyboardHud.INFO);
        register(CalculatorHud.INFO);

        // Default config
        if (isFirstInit) resetToDefaultElements();
    }

    public void register(HudElementInfo<?> info) {
        infos.put(info.name, info);
    }

    private void add(HudElement element, int x, int y, XAnchor xAnchor, YAnchor yAnchor) {
        element.box.setPos(x, y);

        if (xAnchor == null || yAnchor == null) element.box.updateAnchors();
        else {
            element.box.xAnchor = xAnchor;
            element.box.yAnchor = yAnchor;
        }

        element.settings.registerColorSettings(null);

        elements.add(element);
    }

    public void add(HudElementInfo<?> info, int x, int y, XAnchor xAnchor, YAnchor yAnchor) {
        add(info.create(), x, y, xAnchor, yAnchor);
    }

    public void add(HudElementInfo<?> info, int x, int y) {
        add(info, x, y, null, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void add(@NotNull HudElementInfo.Preset preset, int x, int y, XAnchor xAnchor, YAnchor yAnchor) {
        HudElement element = preset.info.create();
        preset.callback.accept(element);
        add(element, x, y, xAnchor, yAnchor);
    }

    public void add(@NotNull HudElementInfo<?>.Preset preset, int x, int y) {
        add(preset, x, y, null, null);
    }

    void remove(HudElement element) {
        element.settings.unregisterColorSettings();
        elements.remove(element);
    }

    public void clear() {
        elements.clear();
    }

    public void resetToDefaultElements() {
        resetToDefaultElements = true;
    }

    private void resetToDefaultElementsImpl() {
        elements.clear();

        int h = (int) Math.ceil(HudRenderer.INSTANCE.textHeight(true));

        // Top Left
        add(AnonTextHud.WATERMARK, 4, 4, XAnchor.Left, YAnchor.Top);
        add(AnonTextHud.FPS, 4, 4 + h, XAnchor.Left, YAnchor.Top);
        add(AnonTextHud.TPS, 4, 4 + h * 2, XAnchor.Left, YAnchor.Top);
        add(AnonTextHud.PING, 4, 4 + h * 3, XAnchor.Left, YAnchor.Top);
        add(AnonTextHud.SPEED, 4, 4 + h * 4, XAnchor.Left, YAnchor.Top);

        // Top Right
        add(ArmorHud.INFO, -4, 4 + h, XAnchor.Right, YAnchor.Top);
        add(ActiveModulesHud.INFO, -4, 4, XAnchor.Right, YAnchor.Top);

        // Bottom Right
        add(AnonTextHud.POSITION, -4, -4, XAnchor.Right, YAnchor.Bottom);
        add(AnonTextHud.OPPOSITE_POSITION, -4, -4 - h, XAnchor.Right, YAnchor.Bottom);
        add(AnonTextHud.ROTATION, -4, -4 - h * 2, XAnchor.Right, YAnchor.Bottom);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (Utils.isLoading()) return;

        if (resetToDefaultElements) {
            resetToDefaultElementsImpl();
            resetToDefaultElements = false;
        }

        if (!(active || HudEditorScreen.isOpen())) return;

        for (HudElement element : elements) {
            if (element.isActive() || element.isInEditor()) {
                element.tick(HudRenderer.INSTANCE);
            }
        }
    }

    @EventHandler
    private void onRender(Render2DEvent event) {
        if (Utils.isLoading()) return;

        if (!(active || HudEditorScreen.isOpen()) || shouldHideHud()) return;
        if ((mc.options.hideGui || mc.debugEntries.isOverlayVisible()) && !HudEditorScreen.isOpen()) return;

        HudRenderer.INSTANCE.begin(event.graphics);

        for (HudElement element : elements) {
            element.updatePos();

            if (element.isActive() || element.isInEditor()) {
                element.render(HudRenderer.INSTANCE);
            }
        }

        HudRenderer.INSTANCE.end();
    }

    private boolean shouldHideHud() {
        return hideInMenus.get() && mc.screen != null && !(mc.screen instanceof WidgetScreen);
    }

    @EventHandler
    private void onCustomFontChanged(CustomFontChangedEvent event) {
        if (customFont.get()) {
            for (HudElement element : elements) element.onFontChanged();
        }
    }

    public boolean hasCustomFont() {
        return customFont.get();
    }

    public double getTextScale() {
        return textScale.get();
    }

    @NotNull
    @Override
    public Iterator<HudElement> iterator() {
        return elements.iterator();
    }

    // Serialization

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("__version__", 1);

        tag.putBoolean("active", active);
        tag.put("settings", settings.toTag());
        tag.put("elements", NbtUtils.listToTag(elements));

        return tag;
    }

    @Override
    public Hud fromTag(CompoundTag tag) {
        if (!tag.contains("__version__")) {
            resetToDefaultElements();
            return this;
        }

        tag.getBoolean("active").ifPresent(active1 -> active = active1);
        settings.fromTag(tag.getCompoundOrEmpty("settings"));

        // Elements
        elements.clear();

        for (Tag e : tag.getListOrEmpty("elements")) {
            CompoundTag c = (CompoundTag) e;
            if (c.getString("name").isEmpty()) continue;

            HudElementInfo<?> info = infos.get(c.getString("name").get());
            if (info != null) {
                HudElement element = info.create();
                element.fromTag(c);
                element.settings.registerColorSettings(null);
                elements.add(element);
            }
        }

        return this;
    }
}
