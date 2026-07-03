/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.misc;

//Created by squidoodly

import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import dev.anon.client.AnonClient;
import dev.anon.client.events.game.OpenScreenEvent;
import dev.anon.client.events.world.TickEvent;
import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.WidgetScreen;
import dev.anon.client.gui.utils.StarscriptTextBoxRenderer;
import dev.anon.client.gui.widgets.WWidget;
import dev.anon.client.gui.widgets.pressable.WButton;
import dev.anon.client.settings.*;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.utils.Utils;
import dev.anon.client.utils.misc.AnonStarscript;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.*;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Tuple;
import net.minecraft.util.Util;
import org.meteordev.starscript.Script;

import java.util.ArrayList;
import java.util.List;

public class DiscordPresence extends Module {
    public enum SelectMode {
        Random,
        Sequential
    }

    private final SettingGroup sgLine1 = settings.createGroup("Line 1");
    private final SettingGroup sgLine2 = settings.createGroup("Line 2");

    // Line 1

    private final Setting<List<String>> line1Strings = sgLine1.add(new StringListSetting.Builder()
        .name("line-1-messages")
        .description("Messages used for the first line.")
        .defaultValue("{player}", "{server}")
        .onChanged(_ -> recompileLine1())
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    private final Setting<Integer> line1UpdateDelay = sgLine1.add(new IntSetting.Builder()
        .name("line-1-update-delay")
        .description("How fast to update the first line in ticks.")
        .defaultValue(200)
        .min(10)
        .sliderRange(10, 200)
        .build()
    );

    private final Setting<SelectMode> line1SelectMode = sgLine1.add(new EnumSetting.Builder<SelectMode>()
        .name("line-1-select-mode")
        .description("How to select messages for the first line.")
        .defaultValue(SelectMode.Sequential)
        .build()
    );

    // Line 2

    private final Setting<List<String>> line2Strings = sgLine2.add(new StringListSetting.Builder()
        .name("line-2-messages")
        .description("Messages used for the second line.")
        .defaultValue("AN0N on Crack!", "{round(server.tps, 1)} TPS", "Playing on {server.difficulty} difficulty.", "{server.player_count} Players online")
        .onChanged(_ -> recompileLine2())
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    private final Setting<Integer> line2UpdateDelay = sgLine2.add(new IntSetting.Builder()
        .name("line-2-update-delay")
        .description("How fast to update the second line in ticks.")
        .defaultValue(60)
        .min(10)
        .sliderRange(10, 200)
        .build()
    );

    private final Setting<SelectMode> line2SelectMode = sgLine2.add(new EnumSetting.Builder<SelectMode>()
        .name("line-2-select-mode")
        .description("How to select messages for the second line.")
        .defaultValue(SelectMode.Sequential)
        .build()
    );

    private static final RichPresence rpc = new RichPresence();
    private int ticks;
    private boolean forceUpdate, lastWasInMainMenu;

    private final List<Script> line1Scripts = new ArrayList<>();
    private int line1Ticks, line1I;

    private final List<Script> line2Scripts = new ArrayList<>();
    private int line2Ticks, line2I;

    public static final List<Tuple<String, String>> customStates = new ArrayList<>();

    static {
        registerCustomState("com.terraformersmc.modmenu.gui", "Browsing mods");
        registerCustomState("me.jellysquid.mods.sodium.client", "Changing options");
    }

    public DiscordPresence() {
        super(Categories.Misc, "discord-presence", "Displays AN0N as your presence on Discord.");

        runInMainMenu = true;
    }

    /**
     * Registers a custom state to be used when the current screen is a class in the specified package.
     */
    public static void registerCustomState(String packageName, String state) {
        for (var pair : customStates) {
            if (pair.getA().equals(packageName)) {
                pair.setB(state);
                return;
            }
        }

        customStates.add(new Tuple<>(packageName, state));
    }

    /**
     * The package name must match exactly to the one provided through {@link #registerCustomState(String, String)}.
     */
    public static void unregisterCustomState(String packageName) {
        customStates.removeIf(pair -> pair.getA().equals(packageName));
    }

    @Override
    public void onActivate() {
        DiscordIPC.setOnError((code, message) ->
            AnonClient.LOG.error("[DiscordRPC] IPC error {}: {}", code, message)
        );

        boolean started = DiscordIPC.start(1519476212899254373L, () ->
            AnonClient.LOG.info("[DiscordRPC] Connected to Discord and ready.")
        );
        AnonClient.LOG.info("[DiscordRPC] IPC start: {} (app={})", started, 1519476212899254373L);
        if (!started) {
            AnonClient.LOG.warn("[DiscordRPC] Failed to connect to Discord. Make sure Discord is running and Discord's activity privacy settings allow it.");
        }

        rpc.setStart(System.currentTimeMillis() / 1000L);

        String largeText = "%s %s".formatted(AnonClient.NAME, AnonClient.VERSION);
        if (!AnonClient.BUILD_NUMBER.isEmpty()) largeText += " Build: " + AnonClient.BUILD_NUMBER;
        rpc.setLargeImage("anon_client", largeText);
        rpc.setSmallImage("anon_client", largeText);

        recompileLine1();
        recompileLine2();

        ticks = 0;
        line1Ticks = 0;
        line2Ticks = 0;
        lastWasInMainMenu = false;

        line1I = 0;
        line2I = 0;
    }

    @Override
    public void onDeactivate() {
        DiscordIPC.stop();
    }

    private void recompile(List<String> messages, List<Script> scripts) {
        scripts.clear();

        for (String message : messages) {
            Script script = AnonStarscript.compile(message);
            if (script != null) scripts.add(script);
        }

        forceUpdate = true;
    }

    private void recompileLine1() {
        recompile(line1Strings.get(), line1Scripts);
    }

    private void recompileLine2() {
        recompile(line2Strings.get(), line2Scripts);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean update = false;

        if (Utils.canUpdate()) {
            // Line 1
            if (line1Ticks >= line1UpdateDelay.get() || forceUpdate) {
                if (!line1Scripts.isEmpty()) {
                    int i = Utils.random(0, line1Scripts.size());
                    if (line1SelectMode.get() == SelectMode.Sequential) {
                        if (line1I >= line1Scripts.size()) line1I = 0;
                        i = line1I++;
                    }

                    String line1 = AnonStarscript.run(line1Scripts.get(i));
                    if (line1 != null) rpc.setDetails(line1);
                }
                update = true;

                line1Ticks = 0;
            } else line1Ticks++;

            // Line 2
            if (line2Ticks >= line2UpdateDelay.get() || forceUpdate) {
                if (!line2Scripts.isEmpty()) {
                    int i = Utils.random(0, line2Scripts.size());
                    if (line2SelectMode.get() == SelectMode.Sequential) {
                        if (line2I >= line2Scripts.size()) line2I = 0;
                        i = line2I++;
                    }

                    String message = AnonStarscript.run(line2Scripts.get(i));
                    if (message != null) rpc.setState(message);
                }
                update = true;

                line2Ticks = 0;
            } else line2Ticks++;
        } else {
            if (!lastWasInMainMenu) {
                rpc.setDetails("AN0N CLIENT " + (AnonClient.BUILD_NUMBER.isEmpty() ? AnonClient.VERSION : AnonClient.VERSION + " " + AnonClient.BUILD_NUMBER));

                if (mc.screen instanceof TitleScreen) rpc.setState("Looking at title screen");
                else if (mc.screen instanceof SelectWorldScreen) rpc.setState("Selecting world");
                else if (mc.screen instanceof CreateWorldScreen || mc.screen instanceof AbstractGameRulesScreen)
                    rpc.setState("Creating world");
                else if (mc.screen instanceof EditWorldScreen) rpc.setState("Editing world");
                else if (mc.screen instanceof LevelLoadingScreen) rpc.setState("Loading world");
                else if (mc.screen instanceof JoinMultiplayerScreen) rpc.setState("Selecting server");
                else if (mc.screen instanceof ManageServerScreen) rpc.setState("Adding server");
                else if (mc.screen instanceof ConnectScreen || mc.screen instanceof DirectJoinServerScreen)
                    rpc.setState("Connecting to server");
                else if (mc.screen instanceof WidgetScreen) rpc.setState("Browsing AN0N's GUI");
                else if (mc.screen instanceof OptionsScreen || mc.screen instanceof SkinCustomizationScreen || mc.screen instanceof SoundOptionsScreen || mc.screen instanceof VideoSettingsScreen || mc.screen instanceof ControlsScreen || mc.screen instanceof LanguageSelectScreen || mc.screen instanceof ChatOptionsScreen || mc.screen instanceof PackSelectionScreen || mc.screen instanceof AccessibilityOptionsScreen)
                    rpc.setState("Changing options");
                else if (mc.screen instanceof WinScreen) rpc.setState("Reading credits");
                else if (mc.screen instanceof RealmsScreen) rpc.setState("Browsing Realms");
                else {
                    boolean setState = false;
                    if (mc.screen != null) {
                        String className = mc.screen.getClass().getName();
                        for (var pair : customStates) {
                            if (className.startsWith(pair.getA())) {
                                rpc.setState(pair.getB());
                                setState = true;
                                break;
                            }
                        }
                    }
                    if (!setState) rpc.setState("In main menu");
                }

                update = true;
            }
        }

        // Update
        if (update) {
            if (DiscordIPC.isConnected()) {
                DiscordIPC.setActivity(rpc);
            } else {
                AnonClient.LOG.warn("[DiscordRPC] Cannot set activity — not connected to Discord.");
            }
        }
        forceUpdate = false;
        lastWasInMainMenu = !Utils.canUpdate();
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!Utils.canUpdate()) lastWasInMainMenu = false;
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WButton help = theme.button("Open documentation.");
        help.action = () -> Util.getPlatform().openUri("https://github.com/Palmtreedev-real/AN0N");

        return help;
    }
}
