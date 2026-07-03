/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anon.client.AnonClient;
import dev.anon.client.commands.Command;
import dev.anon.client.events.world.TickEvent;
import dev.anon.client.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class SpamCommand extends Command {
    private String message;
    private int timer;
    private boolean running;

    public SpamCommand() {
        super("spam", "Spams a message every second.", "repeat");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(context -> {
            if (running) stop();
            else error("Provide a message to spam.");
            return SINGLE_SUCCESS;
        });

        builder.then(argument("message", StringArgumentType.greedyString()).executes(context -> {
            if (running) stop();
            message = context.getArgument("message", String.class);
            start();
            return SINGLE_SUCCESS;
        }));
    }

    private void start() {
        running = true;
        timer = 0;
        AnonClient.EVENT_BUS.subscribe(this);
        info("Started spamming: " + message);
    }

    private void stop() {
        running = false;
        AnonClient.EVENT_BUS.unsubscribe(this);
        info("Stopped spamming.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) {
            stop();
            return;
        }
        if (timer <= 0) {
            ChatUtils.sendPlayerMsg(message);
            timer = 20;
        } else {
            timer--;
        }
    }
}
