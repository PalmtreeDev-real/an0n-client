/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.gui;

import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import dev.anon.client.AnonClient;
import dev.anon.client.gui.renderer.GuiDebugRenderer;
import dev.anon.client.gui.renderer.GuiRenderer;
import dev.anon.client.gui.tabs.TabScreen;
import dev.anon.client.gui.utils.Cell;
import dev.anon.client.gui.widgets.WRoot;
import dev.anon.client.gui.widgets.WWidget;
import dev.anon.client.gui.widgets.containers.WContainer;
import dev.anon.client.gui.widgets.input.WTextBox;
import dev.anon.client.renderer.Renderer2D;
import dev.anon.client.renderer.Texture;
import dev.anon.client.utils.Utils;
import dev.anon.client.utils.misc.CursorStyle;
import dev.anon.client.utils.misc.input.Input;
import dev.anon.client.utils.network.Http;
import dev.anon.client.utils.render.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static dev.anon.client.AnonClient.mc;
import static dev.anon.client.utils.Utils.getWindowHeight;
import static dev.anon.client.utils.Utils.getWindowWidth;
import static org.lwjgl.glfw.GLFW.*;

public abstract class WidgetScreen extends Screen {
    private static final GuiRenderer RENDERER = new GuiRenderer();
    private static final GuiDebugRenderer DEBUG_RENDERER = new GuiDebugRenderer();
    private static Texture backgroundTexture;
    private static boolean backgroundLoaded;
    private static boolean backgroundLoading;
    private static final Renderer2D BG_RENDERER = new Renderer2D(true);

    private static synchronized void loadBackground() {
        if (backgroundLoaded) return;
        backgroundLoaded = true;
        new Thread(() -> {
            try {
                InputStream in = Http.get("https://www.hdwallpapers.net/previews/deep-void-973.jpg").sendInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) baos.write(buf, 0, n);
                in.close();
                byte[] data = baos.toByteArray();

                ByteBuffer imgData = MemoryUtil.memAlloc(data.length);
                imgData.put(data).rewind();

                try (MemoryStack stack = MemoryStack.stackPush()) {
                    IntBuffer w = stack.mallocInt(1);
                    IntBuffer h = stack.mallocInt(1);
                    IntBuffer c = stack.mallocInt(1);
                    ByteBuffer pixels = STBImage.stbi_load_from_memory(imgData, w, h, c, 4);
                    if (pixels != null) {
                        Texture tex = new Texture(w.get(0), h.get(0), TextureFormat.RGBA8, FilterMode.LINEAR, FilterMode.LINEAR);
                        tex.upload(pixels);
                        backgroundTexture = tex;
                        STBImage.stbi_image_free(pixels);
                    }
                }
                MemoryUtil.memFree(imgData);
            } catch (Exception ignored) {
            }
        }).start();
    }

    public Runnable taskAfterRender;
    protected Runnable enterAction;

    public Screen parent;
    private final WContainer root;

    protected final GuiTheme theme;

    public boolean locked, lockedAllowClose;
    private boolean closed;
    private boolean onClose;
    private boolean debug;

    private boolean closing;

    private double lastMouseX, lastMouseY;

    public double animProgress;

    private List<Runnable> onClosed;

    protected boolean firstInit = true;

    public WidgetScreen(GuiTheme theme, String title) {
        super(Component.literal(title));

        this.parent = mc.screen;
        this.root = new WFullScreenRoot();
        this.theme = theme;

        root.theme = theme;

        loadBackground();

        if (parent != null) {
            animProgress = 1;

            if (this instanceof TabScreen && parent instanceof TabScreen) {
                parent = ((TabScreen) parent).parent;
            }
        }
    }

    public <W extends WWidget> Cell<W> add(W widget) {
        return root.add(widget);
    }

    public void clear() {
        root.clear();
    }

    public void invalidate() {
        root.invalidate();
    }

    @Override
    protected void init() {
        AnonClient.EVENT_BUS.subscribe(this);

        closed = false;

        if (firstInit) {
            firstInit = false;
            initWidgets();
        }
    }

    public abstract void initWidgets();

    public void reload() {
        clear();
        initWidgets();
    }

    public void onClosed(Runnable action) {
        if (onClosed == null) onClosed = new ArrayList<>(2);
        onClosed.add(action);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (locked) return false;

        double mouseX = click.x();
        double mouseY = click.y();
        double s = mc.getWindow().getGuiScale();

        mouseX *= s;
        mouseY *= s;

        return root.mouseClicked(new MouseButtonEvent(mouseX, mouseY, click.buttonInfo()), doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (locked) return false;

        double mouseX = click.x();
        double mouseY = click.y();
        double s = mc.getWindow().getGuiScale();

        mouseX *= s;
        mouseY *= s;

        if (debug && click.button() == GLFW_MOUSE_BUTTON_RIGHT)
            DEBUG_RENDERER.mouseReleased(root, new MouseButtonEvent(mouseX, mouseY, click.buttonInfo()), 0);

        return root.mouseReleased(new MouseButtonEvent(mouseX, mouseY, click.buttonInfo()));
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (locked) return;

        double s = mc.getWindow().getGuiScale();
        mouseX *= s;
        mouseY *= s;

        root.mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);

        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (locked) return false;

        root.mouseScrolled(verticalAmount);

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyReleased(KeyEvent input) {
        if (locked) return false;

        if ((input.modifiers() == GLFW_MOD_CONTROL || input.modifiers() == GLFW_MOD_SUPER) && input.key() == GLFW_KEY_9) {
            debug = !debug;
            return true;
        }

        if ((input.key() == GLFW_KEY_ENTER || input.key() == GLFW_KEY_KP_ENTER) && enterAction != null) {
            enterAction.run();
            return true;
        }

        return super.keyReleased(input);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (locked) return false;

        boolean shouldReturn = root.keyPressed(input) || super.keyPressed(input);
        if (shouldReturn) return true;

        // Select next text box if TAB was pressed
        if (input.key() == GLFW_KEY_TAB) {
            AtomicReference<WTextBox> firstTextBox = new AtomicReference<>(null);
            AtomicBoolean done = new AtomicBoolean(false);
            AtomicBoolean foundFocused = new AtomicBoolean(false);

            loopWidgets(root, wWidget -> {
                if (done.get() || !(wWidget instanceof WTextBox textBox)) return;

                if (foundFocused.get()) {
                    textBox.setFocused(true);
                    textBox.setCursorMax();

                    done.set(true);
                } else {
                    if (textBox.isFocused()) {
                        textBox.setFocused(false);
                        foundFocused.set(true);
                    }
                }

                if (firstTextBox.get() == null) firstTextBox.set(textBox);
            });

            if (!done.get() && firstTextBox.get() != null) {
                firstTextBox.get().setFocused(true);
                firstTextBox.get().setCursorMax();
            }

            return true;
        }

        boolean control = MacosUtil.IS_MACOS ? input.modifiers() == GLFW_MOD_SUPER : input.modifiers() == GLFW_MOD_CONTROL;

        return (control && input.key() == GLFW_KEY_C && toClipboard())
            || (control && input.key() == GLFW_KEY_V && fromClipboard());
    }

    public void keyRepeated(KeyEvent input) {
        if (locked) return;

        root.keyRepeated(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (locked) return false;

        return root.charTyped(input);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        if (this.minecraft.level == null) {
            this.extractPanorama(graphics, deltaTicks);
        }
    }

    public void renderCustom(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int s = mc.getWindow().getGuiScale();
        mouseX *= s;
        mouseY *= s;

        animProgress += (delta / 20 * 14) * (closing ? -1 : 1);
        animProgress = Mth.clamp(animProgress, 0, 1);

        if (closing && (animProgress == 0 || parent != null)) {
            closeInternal();
        }

        GuiKeyEvents.canUseKeys = true;

        // Apply projection without scaling
        Utils.unscaledProjection();

        // Render wallpaper background
        if (backgroundTexture != null) {
            BG_RENDERER.begin();
            BG_RENDERER.texQuad(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight(), Color.WHITE);
            BG_RENDERER.render(backgroundTexture.getTextureView(), backgroundTexture.getSampler());
        }

        onRenderBefore(graphics, mouseX, mouseY, delta);

        RENDERER.theme = theme;
        theme.beforeRender();

        RENDERER.begin(graphics);
        RENDERER.setAlpha(animProgress);
        root.render(RENDERER, mouseX, mouseY, delta / 20);
        RENDERER.setAlpha(1);
        RENDERER.end();

        boolean tooltip = RENDERER.renderTooltip(graphics, mouseX, mouseY, delta / 20);

        if (debug) {
            DEBUG_RENDERER.render(root);
            if (tooltip) DEBUG_RENDERER.render(RENDERER.tooltipWidget);
        }

        Utils.scaledProjection();

        runAfterRenderTasks();
    }

    protected void runAfterRenderTasks() {
        if (taskAfterRender != null) {
            taskAfterRender.run();
            taskAfterRender = null;
        }
    }

    protected void onRenderBefore(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        root.invalidate();
    }

    @Override
    public void onClose() {
        if (!locked || lockedAllowClose) {
            closing = true;
        }
    }

    @Override
    public void removed() {
        if (!closed || lockedAllowClose) {
            closed = true;
            onClosed();

            Input.setCursorStyle(CursorStyle.Default);

            loopWidgets(root, widget -> {
                if (widget instanceof WTextBox textBox && textBox.isFocused()) textBox.setFocused(false);
            });

            AnonClient.EVENT_BUS.unsubscribe(this);
            GuiKeyEvents.canUseKeys = true;

            if (onClosed != null) {
                for (Runnable action : onClosed) action.run();
            }

            if (onClose) {
                taskAfterRender = () -> {
                    locked = true;
                    mc.setScreen(parent);
                };
            }
        }
    }

    private void closeInternal() {
        boolean preOnClose = onClose;
        onClose = true;

        super.onClose();
        removed();

        onClose = preOnClose;
    }

    private void loopWidgets(WWidget widget, Consumer<WWidget> action) {
        action.accept(widget);

        if (widget instanceof WContainer wContainer) {
            for (Cell<?> cell : wContainer.cells) loopWidgets(cell.widget(), action);
        }
    }

    protected void onClosed() {
    }

    public boolean toClipboard() {
        return false;
    }

    public boolean fromClipboard() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !locked || lockedAllowClose;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class WFullScreenRoot extends WContainer implements WRoot {
        private boolean valid;

        @Override
        public void invalidate() {
            valid = false;
        }

        @Override
        protected void onCalculateSize() {
            width = getWindowWidth();
            height = getWindowHeight();
        }

        @Override
        protected void onCalculateWidgetPositions() {
            for (Cell<?> cell : cells) {
                cell.x = 0;
                cell.y = 0;

                cell.width = width;
                cell.height = height;

                cell.alignWidget();
            }
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (!valid) {
                calculateSize();
                calculateWidgetPositions();

                valid = true;
                mouseMoved(mc.mouseHandler.xpos(), mc.mouseHandler.ypos(), mc.mouseHandler.xpos(), mc.mouseHandler.ypos());
            }

            return super.render(renderer, mouseX, mouseY, delta);
        }
    }
}
