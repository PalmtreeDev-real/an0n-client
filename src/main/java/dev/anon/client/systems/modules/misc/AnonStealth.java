package dev.anon.client.systems.modules.misc;

import dev.anon.client.AnonClient;
import dev.anon.client.events.anon.KeyInputEvent;
import dev.anon.client.events.game.OpenScreenEvent;
import dev.anon.client.events.render.Render2DEvent;
import dev.anon.client.gui.WidgetScreen;
import dev.anon.client.settings.*;
import dev.anon.client.systems.hud.Hud;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;

public class AnonStealth extends Module {
    public enum DisguiseMode {
        Vanilla,
        Lunar,
        Feather,
        Custom
    }

    private static final String HIDDEN_DIR = ".anon_data";
    private static final long CTRL_TIMEOUT = 2000;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<DisguiseMode> disguise = sgGeneral.add(new EnumSetting.Builder<DisguiseMode>()
        .name("disguise")
        .description("Disguise as a legitimate client.")
        .defaultValue(DisguiseMode.Vanilla)
        .build()
    );

    private final Setting<Boolean> hideHud = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-hud")
        .description("Completely hide the HUD when enabled.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> hideModList = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-mod-list")
        .description("Hide the mod from the F3 mod list.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> hideFromServers = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-from-servers")
        .description("Attempt to hide mod from server mod list.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> fakeModName = sgGeneral.add(new StringSetting.Builder()
        .name("fake-name")
        .description("Fake mod name shown in mod list.")
        .defaultValue("Minecraft Coder Pack")
        .visible(() -> disguise.get() == DisguiseMode.Custom)
        .build()
    );

    private final Setting<Boolean> obfuscateFolder = sgGeneral.add(new BoolSetting.Builder()
        .name("obfuscate-folder")
        .description("Use a hidden config folder name and hide mod files.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> panicMode = sgGeneral.add(new BoolSetting.Builder()
        .name("panic-mode")
        .description("Completely hides the client GUI on activation. Press Ctrl 5 times to restore.")
        .defaultValue(true)
        .build()
    );

    private boolean hiding;
    private Path hiddenFolderPath;
    private boolean panicActive;
    private int ctrlPresses;
    private long lastCtrlPressTime;

    public AnonStealth() {
        super(Categories.Misc, "an0n-stealth", "Hides the client and HUD for privacy.");
    }

    public String getModName() {
        return switch (disguise.get()) {
            case Vanilla -> "Minecraft";
            case Lunar -> "Lunar Client";
            case Feather -> "Feather Client";
            case Custom -> fakeModName.get();
        };
    }

    public boolean shouldHideModList() {
        return isActive() && hideModList.get();
    }

    @Override
    public void onActivate() {
        hiding = false;
        ctrlPresses = 0;
        panicActive = false;

        if (obfuscateFolder.get()) {
            obfuscateFiles();
        }

        if (panicMode.get()) {
            enterPanic();
        }
    }

    @Override
    public void onDeactivate() {
        if (obfuscateFolder.get()) {
            deobfuscateFiles();
        }
        hiding = false;
        exitPanic();
    }

    private void enterPanic() {
        panicActive = true;
        ctrlPresses = 0;

        mc.setScreen(null);
        Hud hud = Hud.get();
        if (hud != null) hud.active = false;
    }

    private void exitPanic() {
        if (!panicActive) return;
        panicActive = false;
        ctrlPresses = 0;

        Hud hud = Hud.get();
        if (hud != null) hud.active = true;
    }

    @EventHandler
    private void onKey(KeyInputEvent event) {
        if (!isActive() || !panicActive || !panicMode.get()) return;
        if (event.action != KeyAction.Press) return;

        int key = event.key();
        if (key != GLFW.GLFW_KEY_LEFT_CONTROL && key != GLFW.GLFW_KEY_RIGHT_CONTROL) return;

        long now = System.currentTimeMillis();
        if (now - lastCtrlPressTime > CTRL_TIMEOUT) {
            ctrlPresses = 0;
        }
        lastCtrlPressTime = now;
        ctrlPresses++;

        if (ctrlPresses >= 5) {
            exitPanic();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onOpenScreen(OpenScreenEvent event) {
        if (!isActive() || !panicActive || !panicMode.get()) return;

        if (event.screen instanceof WidgetScreen) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render2DEvent event) {
        if (!isActive()) return;

        if (panicActive && panicMode.get()) {
            Hud hud = Hud.get();
            if (hud != null) hud.active = false;
            return;
        }

        if (hideHud.get()) {
            Hud hud = Hud.get();
            if (hud != null) hud.active = false;
        }
    }

    public boolean isHidden() {
        return isActive() && hiding;
    }

    public boolean isPanicActive() {
        return isActive() && panicActive;
    }

    private void obfuscateFiles() {
        try {
            File hidden = new File(AnonClient.FOLDER.getParentFile(), HIDDEN_DIR);

            if (AnonClient.FOLDER.exists()) {
                AnonClient.FOLDER.renameTo(hidden);
            }

            hiddenFolderPath = hidden.toPath();

            if (hidden.exists()) {
                setHiddenAttribute(hiddenFolderPath, true);
            }

            Path jarPath = findModJar();
            if (jarPath != null) {
                setHiddenAttribute(jarPath, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deobfuscateFiles() {
        try {
            if (hiddenFolderPath != null) {
                File hidden = hiddenFolderPath.toFile();
                if (hidden.exists() && !AnonClient.FOLDER.exists()) {
                    setHiddenAttribute(hiddenFolderPath, false);
                    hidden.renameTo(AnonClient.FOLDER);
                }
            } else {
                File hidden = new File(AnonClient.FOLDER.getParentFile(), HIDDEN_DIR);
                if (hidden.exists() && !AnonClient.FOLDER.exists()) {
                    setHiddenAttribute(hidden.toPath(), false);
                    hidden.renameTo(AnonClient.FOLDER);
                }
            }

            Path jarPath = findModJar();
            if (jarPath != null) {
                setHiddenAttribute(jarPath, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Path findModJar() {
        try {
            var location = getClass().getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                Path path = Path.of(location.toURI());
                if (path.toString().endsWith(".jar") && path.toFile().exists()) {
                    return path;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void setHiddenAttribute(Path path, boolean hidden) {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Files.setAttribute(path, "dos:hidden", hidden);
            } else if (hidden) {
                String name = path.getFileName().toString();
                if (!name.startsWith(".")) {
                    Path parent = path.getParent();
                    Path renamed = parent.resolve("." + name);
                    Files.move(path, renamed);
                }
            }
        } catch (IOException ignored) {
        }
    }
}
