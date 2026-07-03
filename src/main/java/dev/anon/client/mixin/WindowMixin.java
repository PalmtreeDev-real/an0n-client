package dev.anon.client.mixin;

import com.mojang.blaze3d.platform.Window;
import dev.anon.client.AnonClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

@Mixin(Window.class)
public class WindowMixin {
    @Shadow
    private long handle;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        setWindowIcon("/assets/anon-client/appicon.png");
    }

    private void setWindowIcon(String path) {
        try (InputStream stream = WindowMixin.class.getResourceAsStream(path)) {
            if (stream == null) {
                AnonClient.LOG.warn("[WindowMixin] Icon '{}' not found", path);
                return;
            }

            BufferedImage image = ImageIO.read(stream);
            if (image == null) return;

            // Scale large images down to a reasonable window icon size
            int size = Math.min(image.getWidth(), image.getHeight());
            if (size > 128) size = 128;

            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(image, 0, 0, size, size, null);
            g.dispose();

            int[] pixels = new int[size * size];
            scaled.getRGB(0, 0, size, size, pixels, 0, size);

            ByteBuffer buffer = MemoryUtil.memAlloc(pixels.length * 4);
            for (int pixel : pixels) {
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
            buffer.flip();

            GLFWImage.Buffer iconBuffer = GLFWImage.malloc(1);
            GLFWImage iconImage = GLFWImage.malloc();
            iconImage.set(size, size, buffer);
            iconBuffer.put(0, iconImage);
            GLFW.glfwSetWindowIcon(handle, iconBuffer);
            iconImage.free();
            iconBuffer.free();
            MemoryUtil.memFree(buffer);
        } catch (Exception e) {
            AnonClient.LOG.warn("[WindowMixin] Failed to set icon: {}", e.getMessage());
        }
    }
}
