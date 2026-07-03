/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.player;

import dev.anon.client.AnonClient;
import dev.anon.client.addons.AddonManager;
import dev.anon.client.addons.GithubRepo;
import dev.anon.client.addons.AnonAddon;
import dev.anon.client.gui.GuiThemes;
import dev.anon.client.gui.screens.CommitsScreen;
import dev.anon.client.mixininterface.IComponent;
import dev.anon.client.utils.network.Http;
import dev.anon.client.utils.network.AnonExecutor;
import dev.anon.client.utils.render.AnonToast;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.anon.client.AnonClient.mc;

public class TitleScreenCredits {
    private static final List<Credit> credits = new ArrayList<>();

    private TitleScreenCredits() {
    }

    private static void init() {
        // Add addons
        for (AnonAddon addon : AddonManager.ADDONS) add(addon);

        // Sort by width (AN0N always first)
        credits.sort(Comparator.comparingInt(value -> value.addon == AnonClient.ADDON ? Integer.MIN_VALUE : -mc.font.width(value.text)));

        // Check for latest commits
        AnonExecutor.execute(() -> {
            for (Credit credit : credits) {
                if (credit.addon.getRepo() == null || credit.addon.getCommit() == null) continue;

                GithubRepo repo = credit.addon.getRepo();
                Http.Request request = Http.get("https://api.github.com/repos/%s/branches/%s".formatted(repo.getOwnerName(), repo.branch()));
                request.exceptionHandler(e -> AnonClient.LOG.error("Could not fetch repository information for addon '{}'.", credit.addon.name, e));
                repo.authenticate(request);
                HttpResponse<Response> res = request.sendJsonResponse(Response.class);

                switch (res.statusCode()) {
                    case Http.UNAUTHORIZED -> {
                        String message = "Invalid authentication token for repository '%s'".formatted(repo.getOwnerName());
                        AnonToast toast = new AnonToast.Builder("GitHub: Unauthorized").icon(Items.BARRIER).text(message).build();
                        mc.getToastManager().addToast(toast);
                        AnonClient.LOG.warn(message);
                        if (System.getenv("anon.github.authorization") == null) {
                            AnonClient.LOG.info("Consider setting an authorization " +
                                "token with the 'anon.github.authorization' environment variable.");
                            AnonClient.LOG.info("See: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens");
                        }
                    }
                    case Http.FORBIDDEN ->
                        AnonClient.LOG.warn("Could not fetch updates for addon '{}': Rate-limited by GitHub.", credit.addon.name);
                    case Http.NOT_FOUND ->
                        AnonClient.LOG.warn("Could not fetch updates for addon '{}': GitHub repository '{}' not found.", credit.addon.name, repo.getOwnerName());
                    case Http.SUCCESS -> {
                        if (!credit.addon.getCommit().equals(res.body().commit.sha)) {
                            synchronized (credit.text) {
                                credit.text.append(Component.literal("*").withStyle(ChatFormatting.RED));
                                ((IComponent) ((Component) credit.text)).anon$invalidateCache(); // ???
                            }
                        }
                    }
                }
            }
        });
    }

    private static void add(AnonAddon addon) {
        Credit credit = new Credit(addon);

        credit.text.append(Component.literal(addon.name).withStyle(style -> style.withColor(addon.color.getPacked())));
        credit.text.append(Component.literal(" by ").withStyle(ChatFormatting.GRAY));

        boolean hasAtlasDev = false;
        boolean hasPalmtree = false;
        for (String author : addon.authors) {
            if (author.equals("AtlasDevMC")) hasAtlasDev = true;
            if (author.equals("Palmtreedev-real")) hasPalmtree = true;
        }

        if (hasAtlasDev && hasPalmtree) {
            credit.text.append(Component.literal("AtlasDevMC").withStyle(ChatFormatting.WHITE));
            credit.text.append(Component.literal(" (also known as ").withStyle(ChatFormatting.GRAY));
            credit.text.append(Component.literal("Palmtreedev-real").withStyle(ChatFormatting.WHITE));
            credit.text.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
        } else {
            for (int i = 0; i < addon.authors.length; i++) {
                if (i > 0) {
                    credit.text.append(Component.literal(i == addon.authors.length - 1 ? " & " : ", ").withStyle(ChatFormatting.GRAY));
                }

                credit.text.append(Component.literal(addon.authors[i]).withStyle(ChatFormatting.WHITE));
            }
        }

        credits.add(credit);
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (credits.isEmpty()) init();

        int y = 3;

        MutableComponent devNotice = Component.literal("⚠ Development Build - Expect Bugs").withStyle(ChatFormatting.RED);
        int noticeX = mc.screen.width - 3 - mc.font.width(devNotice);
        graphics.text(mc.font, devNotice, noticeX, y, -1);
        y += mc.font.lineHeight + 2;

        MutableComponent reportNotice = Component.literal("Report bugs on GitHub or Discord").withStyle(ChatFormatting.GRAY);
        int reportX = mc.screen.width - 3 - mc.font.width(reportNotice);
        graphics.text(mc.font, reportNotice, reportX, y, -1);
        y += mc.font.lineHeight + 4;

        for (Credit credit : credits) {
            synchronized (credit.text) {
                int x = mc.screen.width - 3 - mc.font.width(credit.text);

                graphics.text(mc.font, credit.text, x, y, -1);
            }

            y += mc.font.lineHeight + 2;
        }
    }

    public static boolean onClicked(double mouseX, double mouseY) {
        int y = 3;
        for (Credit credit : credits) {
            int width;
            synchronized (credit.text) {
                width = mc.font.width(credit.text);
            }

            int x = mc.screen.width - 3 - width;

            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + mc.font.lineHeight + 2) {
                if (credit.addon.getRepo() != null && credit.addon.getCommit() != null) {
                    mc.setScreen(new CommitsScreen(GuiThemes.get(), credit.addon));
                    return true;
                }
            }

            y += mc.font.lineHeight + 2;
        }

        return false;
    }

    private static class Credit {
        public final AnonAddon addon;
        public final MutableComponent text = Component.empty();

        public Credit(AnonAddon addon) {
            this.addon = addon;
        }
    }

    private static class Response {
        public Commit commit;
    }

    private static class Commit {
        public String sha;
    }
}
