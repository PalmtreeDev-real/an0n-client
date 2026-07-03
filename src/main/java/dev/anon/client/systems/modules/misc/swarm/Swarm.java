/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems.modules.misc.swarm;

import dev.anon.client.events.game.GameJoinedEvent;
import dev.anon.client.events.game.GameLeftEvent;
import dev.anon.client.events.world.TickEvent;
import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.widgets.WWidget;
import dev.anon.client.gui.widgets.containers.WHorizontalList;
import dev.anon.client.gui.widgets.containers.WVerticalList;
import dev.anon.client.gui.widgets.pressable.WButton;
import dev.anon.client.settings.*;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Util;

public class Swarm extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("What type of client to run.")
        .defaultValue(Mode.Host)
        .build()
    );

    private final Setting<String> ipAddress = sgGeneral.add(new StringSetting.Builder()
        .name("ip")
        .description("The IP address of the host server.")
        .defaultValue("localhost")
        .visible(() -> mode.get() == Mode.Worker)
        .build()
    );

    private final Setting<Integer> serverPort = sgGeneral.add(new IntSetting.Builder()
        .name("port")
        .description("The port used for connections.")
        .defaultValue(6969)
        .range(1, 65535)
        .noSlider()
        .build()
    );

    public SwarmHost host;
    public SwarmWorker worker;

    public Swarm() {
        super(Categories.Misc, "swarm", "Allows you to control multiple instances of AN0N from one central host.");
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();

        WHorizontalList b = list.add(theme.horizontalList()).expandX().widget();

        WButton start = b.add(theme.button("Start")).expandX().widget();
        start.action = () -> {
            if (!isActive()) return;

            close();
            if (mode.get() == Mode.Host) host = new SwarmHost(serverPort.get());
            else worker = new SwarmWorker(ipAddress.get(), serverPort.get());
        };

        WButton stop = b.add(theme.button("Stop")).expandX().widget();
        stop.action = this::close;

        WButton guide = list.add(theme.button("Guide")).expandX().widget();
        guide.action = () -> Util.getPlatform().openUri("https://github.com/Palmtreedev-real/AN0N");

        return list;
    }

    @Override
    public String getInfoString() {
        return mode.get().name();
    }

    @Override
    public void onActivate() {
        close();
    }

    @Override
    public void onDeactivate() {
        close();
    }

    public void close() {
        try {
            if (host != null) {
                host.disconnect();
                host = null;
            }
            if (worker != null) {
                worker.disconnect();
                worker = null;
            }
        } catch (Exception _) {
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        toggle();
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        toggle();
    }

    @Override
    public void toggle() {
        close();
        super.toggle();
    }

    public boolean isHost() {
        return mode.get() == Mode.Host && host != null && !host.isInterrupted();
    }

    public boolean isWorker() {
        return mode.get() == Mode.Worker && worker != null && !worker.isInterrupted();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (isWorker()) worker.tick();
    }

    public enum Mode {
        Host,
        Worker
    }
}
