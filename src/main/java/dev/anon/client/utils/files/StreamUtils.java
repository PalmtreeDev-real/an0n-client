/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.files;

import dev.anon.client.AnonClient;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class StreamUtils {
    private StreamUtils() {
    }

    public static void copy(File from, File to) {
        try (InputStream in = new FileInputStream(from);
             OutputStream out = new FileOutputStream(to)) {
            in.transferTo(out);
        } catch (IOException e) {
            AnonClient.LOG.error("Error copying from file '{}' to file '{}'.", from.getName(), to.getName(), e);
        }
    }

    public static void copy(InputStream in, File to) {
        try (OutputStream out = new FileOutputStream(to)) {
            in.transferTo(out);
        } catch (IOException _) {
            AnonClient.LOG.error("Error writing to file '{}'.", to.getName());
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
