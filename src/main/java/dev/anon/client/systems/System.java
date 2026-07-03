/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.systems;

import dev.anon.client.AnonClient;
import dev.anon.client.utils.files.StreamUtils;
import dev.anon.client.utils.misc.ISerializable;
import net.minecraft.ReportedException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public abstract class System<T> implements ISerializable<T> {
    private final String name;
    private File file;

    protected boolean isFirstInit;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);

    public System(String name) {
        this.name = name;

        if (name != null) {
            this.file = new File(AnonClient.FOLDER, name + ".nbt");
            this.isFirstInit = !file.exists();
        }
    }

    public void init() {
    }

    public void save(File folder) {
        File file = getFile();
        if (file == null) return;

        CompoundTag tag = toTag();
        if (tag == null) return;

        try {
            File tempFile = File.createTempFile(AnonClient.MOD_ID, file.getName());
            NbtIo.write(tag, tempFile.toPath());

            if (folder != null) file = new File(folder, file.getName());

            file.getParentFile().mkdirs();

            try {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException _) {
                StreamUtils.copy(tempFile, file);
            }

            tempFile.delete();
        } catch (IOException e) {
            AnonClient.LOG.error("Error saving {}. Possibly corrupted?", this.name, e);
        }
    }

    public void save() {
        save(null);
    }

    public void load(File folder) {
        File file = getFile();
        if (file == null) return;

        try {
            if (folder != null) file = new File(folder, file.getName());

            if (file.exists()) {
                try {
                    fromTag(NbtIo.read(file.toPath()));
                } catch (ReportedException e) {
                    String backupName = FilenameUtils.removeExtension(file.getName()) + "-" + ZonedDateTime.now().format(DATE_TIME_FORMATTER) + ".backup.nbt";
                    File backup = new File(file.getParentFile(), backupName);

                    try {
                        Files.move(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    } catch (AtomicMoveNotSupportedException _) {
                        StreamUtils.copy(file, backup);
                    }

                    AnonClient.LOG.error("Error loading {}. Possibly corrupted?", this.name, e);
                    AnonClient.LOG.info("Saved settings backup to '{}'.", backup);
                }
            }
        } catch (IOException e) {
            AnonClient.LOG.error("Error loading {}. Possibly corrupted?", this.name, e);
        }
    }

    public void load() {
        load(null);
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    @Override
    public CompoundTag toTag() {
        return null;
    }

    @Override
    public T fromTag(CompoundTag tag) {
        return null;
    }
}
