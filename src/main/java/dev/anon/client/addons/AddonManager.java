/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.addons;

import dev.anon.client.AnonClient;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

import java.util.ArrayList;
import java.util.List;

public class AddonManager {
    public static final List<AnonAddon> ADDONS = new ArrayList<>();

    public static void init() {
        // Anon pseudo addon
        {
            AnonClient.ADDON = new AnonAddon() {
                @Override
                public void onInitialize() {}

                @Override
                public String getPackage() {
                    return "dev.anon.client";
                }

                @Override
                public String getWebsite() {
                    return "https://github.com/PalmtreeDev-real/an0n-client";
                }

                @Override
                public GithubRepo getRepo() {
                    return new GithubRepo("PalmtreeDev-real", "an0n-client");
                }

                @Override
                public String getCommit() {
                    String commit = AnonClient.MOD_META.getCustomValue(AnonClient.MOD_ID + ":commit").getAsString();
                    return commit.isEmpty() ? null : commit;
                }
            };

            ModMetadata metadata = FabricLoader.getInstance().getModContainer(AnonClient.MOD_ID).get().getMetadata();

            AnonClient.ADDON.name = metadata.getName();
            AnonClient.ADDON.authors = new String[metadata.getAuthors().size()];
            if (metadata.containsCustomValue(AnonClient.MOD_ID + ":color")) {
                AnonClient.ADDON.color.parse(metadata.getCustomValue(AnonClient.MOD_ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                AnonClient.ADDON.authors[i++] = author.getName();
            }

            ADDONS.add(AnonClient.ADDON);
        }

        // Addons
        for (EntrypointContainer<AnonAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("anon", AnonAddon.class)) {
            ModMetadata metadata = entrypoint.getProvider().getMetadata();
            AnonAddon addon;
            try {
                addon = entrypoint.getEntrypoint();
            } catch (Throwable throwable) {
                throw new RuntimeException("Exception during addon init \"%s\".".formatted(metadata.getName()), throwable);
            }

            addon.name = metadata.getName();

            if (metadata.getAuthors().isEmpty()) throw new RuntimeException("Addon \"%s\" requires at least 1 author to be defined in it's fabric.mod.json. See https://fabricmc.net/wiki/documentation:fabric_mod_json_spec".formatted(addon.name));
            addon.authors = new String[metadata.getAuthors().size()];

            if (metadata.containsCustomValue(AnonClient.MOD_ID + ":color")) {
                addon.color.parse(metadata.getCustomValue(AnonClient.MOD_ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                addon.authors[i++] = author.getName();
            }

            ADDONS.add(addon);
        }
    }
}
