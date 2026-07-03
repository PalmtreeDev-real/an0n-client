/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anon.client.commands.Command;
import dev.anon.client.commands.arguments.ModuleArgumentType;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.systems.modules.Modules;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "Binds a specified module to the next pressed key.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(argument("module", ModuleArgumentType.create()).executes(context -> {
            Module module = context.getArgument("module", Module.class);
            Modules.get().setModuleToBind(module);
            Modules.get().awaitKeyRelease();
            module.info("Press a key to bind the module to.");
            return SINGLE_SUCCESS;
        }));
    }
}
