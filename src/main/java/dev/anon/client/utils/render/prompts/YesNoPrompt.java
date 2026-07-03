/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.utils.render.prompts;

import dev.anon.client.gui.GuiTheme;
import dev.anon.client.gui.GuiThemes;
import dev.anon.client.gui.widgets.pressable.WButton;
import net.minecraft.client.gui.screens.Screen;

import static dev.anon.client.AnonClient.mc;

public class YesNoPrompt extends Prompt<YesNoPrompt> {
    private Runnable onYes = () -> {
    };
    private Runnable onNo = () -> {
    };

    private YesNoPrompt(GuiTheme theme, Screen parent) {
        super(theme, parent);
    }

    public static YesNoPrompt create() {
        return new YesNoPrompt(GuiThemes.get(), mc.screen);
    }

    public static YesNoPrompt create(GuiTheme theme, Screen parent) {
        return new YesNoPrompt(theme, parent);
    }

    public YesNoPrompt onYes(Runnable action) {
        this.onYes = action;
        return this;
    }

    public YesNoPrompt onNo(Runnable action) {
        this.onNo = action;
        return this;
    }

    @Override
    protected void initialiseWidgets(PromptScreen screen) {
        WButton yesButton = screen.list.add(theme.button("Yes")).expandX().widget();
        yesButton.action = () -> {
            dontShowAgain(screen);
            onYes.run();
            screen.onClose();
        };

        WButton noButton = screen.list.add(theme.button("No")).expandX().widget();
        noButton.action = () -> {
            dontShowAgain(screen);
            onNo.run();
            screen.onClose();
        };
    }
}
