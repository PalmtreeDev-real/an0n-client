/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import dev.anon.client.AnonClient;
import dev.anon.client.commands.Command;
import dev.anon.client.events.world.TickEvent;
import dev.anon.client.mixin.KeyMappingAccessor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.List;

public class InputCommand extends Command {
    private static final List<KeypressHandler> activeHandlers = new ArrayList<>();

    private static final List<Pair<KeyMapping, String>> holdKeys = List.of(
        new Pair<>(mc.options.keyUp, "forwards"),
        new Pair<>(mc.options.keyDown, "backwards"),
        new Pair<>(mc.options.keyLeft, "left"),
        new Pair<>(mc.options.keyRight, "right"),
        new Pair<>(mc.options.keyJump, "jump"),
        new Pair<>(mc.options.keyShift, "sneak"),
        new Pair<>(mc.options.keySprint, "sprint"),
        new Pair<>(mc.options.keyUse, "use"),
        new Pair<>(mc.options.keyAttack, "attack")
    );

    private static final List<Pair<KeyMapping, String>> pressKeys = List.of(
        new Pair<>(mc.options.keySwapOffhand, "swap"),
        new Pair<>(mc.options.keyDrop, "drop")
    );

    public InputCommand() {
        super("input", "Keyboard input simulation.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        for (Pair<KeyMapping, String> keyBinding : holdKeys) {
            builder.then(literal(keyBinding.getSecond())
                .executes(_ -> {
                    activeHandlers.add(new KeypressHandler(keyBinding.getFirst(), 1));
                    return SINGLE_SUCCESS;
                })
                .then(argument("ticks", IntegerArgumentType.integer(1))
                    .executes(context -> {
                        activeHandlers.add(new KeypressHandler(keyBinding.getFirst(), context.getArgument("ticks", Integer.class)));
                        return SINGLE_SUCCESS;
                    })
                )
            );
        }

        for (Pair<KeyMapping, String> keyBinding : pressKeys) {
            builder.then(literal(keyBinding.getSecond())
                .executes(_ -> {
                    press(keyBinding.getFirst());
                    return SINGLE_SUCCESS;
                })
            );
        }

        for (KeyMapping keyBinding : mc.options.keyHotbarSlots) {
            builder.then(literal(keyBinding.getName().substring(4))
                .executes(_ -> {
                    press(keyBinding);
                    return SINGLE_SUCCESS;
                })
            );
        }

        builder.then(literal("clear").executes(_ -> {
            if (activeHandlers.isEmpty()) warning("No active keypress handlers.");
            else {
                info("Cleared all keypress handlers.");
                activeHandlers.forEach(AnonClient.EVENT_BUS::unsubscribe);
                activeHandlers.clear();
            }
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("list").executes(_ -> {
            if (activeHandlers.isEmpty()) warning("No active keypress handlers.");
            else {
                info("Active keypress handlers: ");
                for (int i = 0; i < activeHandlers.size(); i++) {
                    KeypressHandler handler = activeHandlers.get(i);
                    info("(highlight)%d(default) - (highlight)%s %d(default) ticks left out of (highlight)%d(default).", i, I18n.get(handler.key.getName()), handler.ticks, handler.totalTicks);
                }
            }
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("remove").then(argument("index", IntegerArgumentType.integer(0)).executes(ctx -> {
            int index = IntegerArgumentType.getInteger(ctx, "index");
            if (index >= activeHandlers.size()) warning("Index out of range.");
            else {
                info("Removed keypress handler.");
                AnonClient.EVENT_BUS.unsubscribe(activeHandlers.get(index));
                activeHandlers.remove(index);
            }
            return SINGLE_SUCCESS;
        })));
    }

    private static void press(KeyMapping keyBinding) {
        KeyMappingAccessor accessor = (KeyMappingAccessor) keyBinding;
        accessor.anon$setClickCount(accessor.anon$getClickCount() + 1);
    }

    private static class KeypressHandler {
        private final KeyMapping key;
        private final int totalTicks;
        private int ticks;

        public KeypressHandler(KeyMapping key, int ticks) {
            this.key = key;
            this.totalTicks = ticks;
            this.ticks = ticks;

            AnonClient.EVENT_BUS.subscribe(this);
        }

        @EventHandler
        private void onTick(TickEvent.Post event) {
            if (ticks == totalTicks) press(key);

            if (ticks-- > 0) {
                key.setDown(true);
            } else {
                key.setDown(false);
                AnonClient.EVENT_BUS.unsubscribe(this);
                activeHandlers.remove(this);
            }
        }
    }
}
