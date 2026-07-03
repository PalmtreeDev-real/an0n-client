/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui.screens;

import dev.anon.client.AnonClient;
import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.WindowScreen;
import dev.anon.client.gui.widgets.containers.WTable;
import dev.anon.client.gui.widgets.input.WTextBox;
import dev.anon.client.gui.widgets.pressable.WButton;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.misc.Notebot;
import dev.anon.client.utils.Utils;
import dev.anon.client.utils.notebot.decoder.SongDecoders;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class NotebotSongsScreen extends WindowScreen {
    private static final Notebot notebot = Modules.get().get(Notebot.class);

    private WTextBox filter;
    private String filterText = "";

    private WTable table;

    public NotebotSongsScreen(GuiTheme theme) {
        super(theme, "Notebot Songs");
    }

    @Override
    public void initWidgets() {
        // Random Song
        WButton randomSong = add(theme.button("Random Song")).minWidth(400).expandX().widget();
        randomSong.action = notebot::playRandomSong;

        // Filter
        filter = add(theme.textBox("", "Search for the songs...")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initSongsTable();
        };

        table = add(theme.table()).widget();

        initSongsTable();
    }

    private void initSongsTable() {
        AtomicBoolean noSongsFound = new AtomicBoolean(true);
        try {
            Files.list(AnonClient.FOLDER.toPath().resolve("notebot")).forEach(path -> {
                if (SongDecoders.hasDecoder(path)) {
                    String name = path.getFileName().toString();

                    if (Utils.searchTextDefault(name, filterText, false)) {
                        addPath(path);
                        noSongsFound.set(false);
                    }
                }
            });
        } catch (IOException _) {
            table.add(theme.label("Missing anon-client/notebot folder.")).expandCellX();
            table.row();
        }

        if (noSongsFound.get()) {
            table.add(theme.label("No songs found.")).expandCellX().center();
        }
    }

    private void addPath(Path path) {
        table.add(theme.horizontalSeparator()).expandX().minWidth(400);
        table.row();

        table.add(theme.label(FilenameUtils.getBaseName(path.getFileName().toString()))).expandCellX();
        WButton load = table.add(theme.button("Load")).right().widget();
        load.action = () -> notebot.loadSong(path.toFile());
        WButton preview = table.add(theme.button("Preview")).right().widget();
        preview.action = () -> notebot.previewSong(path.toFile());

        table.row();
    }
}
