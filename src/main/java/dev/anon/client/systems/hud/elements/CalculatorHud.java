package dev.anon.client.systems.hud.elements;

import dev.anon.client.settings.*;
import dev.anon.client.systems.hud.Hud;
import dev.anon.client.systems.hud.HudElement;
import dev.anon.client.systems.hud.HudElementInfo;
import dev.anon.client.systems.hud.HudRenderer;
import dev.anon.client.utils.render.color.Color;
import dev.anon.client.utils.render.color.SettingColor;

import java.util.ArrayList;
import java.util.List;

public class CalculatorHud extends HudElement {
    public static final HudElementInfo<CalculatorHud> INFO = new HudElementInfo<>(Hud.GROUP, "calculator", "A fully interactive calculator.", CalculatorHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAppearance = settings.createGroup("Appearance");

    private static final String[][] BUTTONS = {
        {"7", "8", "9", "/", "C"},
        {"4", "5", "6", "*", "("},
        {"1", "2", "3", "-", ")"},
        {"0", ".", "^", "+", "="}
    };

    private static final int COLS = 5;
    private static final int ROWS = 4;
    private static final int BTN_W = 22;
    private static final int BTN_H = 18;
    private static final int GAP = 2;

    private final StringListSetting expressionSetting;
    private final Setting<Double> scale;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> bgColor;
    private final Setting<SettingColor> btnColor;
    private final Setting<SettingColor> opColor;
    private final Setting<SettingColor> eqColor;
    private final Setting<SettingColor> clrColor;
    private final Setting<SettingColor> textColor;

    private final StringBuilder expr = new StringBuilder();
    private String result = "0";
    private boolean justEvaluated;

    public CalculatorHud() {
        super(INFO);

        expressionSetting = sgGeneral.add(new StringListSetting.Builder()
            .name("expression")
            .description("Current calculator expression.")
            .defaultValue(List.of())
            .build()
        );

        scale = sgAppearance.add(new DoubleSetting.Builder()
            .name("scale")
            .description("Scale of the calculator.")
            .defaultValue(1)
            .min(0.5)
            .sliderRange(0.5, 2)
            .onChanged(_ -> calculateSize())
            .build()
        );

        background = sgAppearance.add(new BoolSetting.Builder()
            .name("background")
            .description("Displays background.")
            .defaultValue(true)
            .build()
        );

        bgColor = sgAppearance.add(new ColorSetting.Builder()
            .name("background-color")
            .description("Background color.")
            .visible(background::get)
            .defaultValue(new SettingColor(0, 0, 0, 120))
            .build()
        );

        btnColor = sgAppearance.add(new ColorSetting.Builder()
            .name("button-color")
            .description("Color of number buttons.")
            .defaultValue(new SettingColor(40, 40, 40, 200))
            .build()
        );

        opColor = sgAppearance.add(new ColorSetting.Builder()
            .name("operator-color")
            .description("Color of operator buttons.")
            .defaultValue(new SettingColor(60, 60, 60, 200))
            .build()
        );

        eqColor = sgAppearance.add(new ColorSetting.Builder()
            .name("equals-color")
            .description("Color of the equals button.")
            .defaultValue(new SettingColor(0, 100, 200, 200))
            .build()
        );

        clrColor = sgAppearance.add(new ColorSetting.Builder()
            .name("clear-color")
            .description("Color of the clear button.")
            .defaultValue(new SettingColor(180, 40, 40, 200))
            .build()
        );

        textColor = sgAppearance.add(new ColorSetting.Builder()
            .name("text-color")
            .description("Color of button text.")
            .defaultValue(new SettingColor(220, 220, 220))
            .build()
        );

        restoreExpression();
        calculateSize();
    }

    private void restoreExpression() {
        expr.setLength(0);
        List<String> parts = expressionSetting.get();
        for (String part : parts) expr.append(part);
        if (expr.isEmpty()) expr.append("0");
    }

    private void saveExpression() {
        List<String> parts = new ArrayList<>();
        parts.add(expr.toString());
        expressionSetting.set(parts);
    }

    private void calculateSize() {
        double s = scale.get();
        int w = COLS * BTN_W + (COLS - 1) * GAP + 4;
        int h = ROWS * BTN_H + (ROWS - 1) * GAP + 4 + 20;
        setSize(w * s, h * s);
    }

    @Override
    public void tick(HudRenderer renderer) {
        calculateSize();
    }

    private int hitTest(double mx, double my) {
        double s = scale.get();
        double startX = x + 2 * s;
        double startY = y + 22 * s;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                double bx = startX + col * (BTN_W + GAP) * s;
                double by = startY + row * (BTN_H + GAP) * s;
                double bw = BTN_W * s;
                double bh = BTN_H * s;
                if (mx >= bx && mx <= bx + bw && my >= by && my <= by + bh) {
                    return row * COLS + col;
                }
            }
        }
        return -1;
    }

    public boolean handleClick(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        int idx = hitTest(mouseX, mouseY);
        if (idx < 0) return false;

        int row = idx / COLS;
        int col = idx % COLS;
        String val = BUTTONS[row][col];
        processInput(val);
        return true;
    }

    private void processInput(String val) {
        switch (val) {
            case "C" -> {
                expr.setLength(0);
                expr.append("0");
                result = "0";
                justEvaluated = false;
            }
            case "=" -> {
                result = evaluate(expr.toString());
                justEvaluated = true;
            }
            default -> {
                if (justEvaluated) {
                    expr.setLength(0);
                    justEvaluated = false;
                }
                if (expr.length() == 1 && expr.charAt(0) == '0' && !val.equals(".")) {
                    expr.setLength(0);
                }
                expr.append(val);
                result = evaluate(expr.toString());
            }
        }
        saveExpression();
    }

    @Override
    public void render(HudRenderer renderer) {
        double s = scale.get();

        if (background.get()) {
            renderer.quad(x, y, getWidth(), getHeight(), bgColor.get());
        }

        double startX = x + 2 * s;
        double displayY = y + 2 * s;
        String display = expr + " = " + result;
        renderer.text(display, startX, displayY, new Color(textColor.get().r, textColor.get().g, textColor.get().b, 255), true, s);

        double btnStartY = y + 22 * s;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                String label = BUTTONS[row][col];
                double bx = startX + col * (BTN_W + GAP) * s;
                double by = btnStartY + row * (BTN_H + GAP) * s;
                double bw = BTN_W * s;
                double bh = BTN_H * s;
                Color c = switch (label) {
                    case "=" -> eqColor.get();
                    case "C" -> clrColor.get();
                    case "+", "-", "*", "/" -> opColor.get();
                    default -> btnColor.get();
                };
                renderer.quad(bx, by, bw, bh, c);
                double tx = bx + (bw - renderer.textWidth(label, false, s)) / 2;
                double ty = by + (bh - renderer.textHeight(false, s)) / 2;
                renderer.text(label, tx, ty, textColor.get(), true, s);
            }
        }
    }

    private String evaluate(String expression) {
        try {
            String sanitized = expression.replace('^', '^').replace('×', '*').replace('÷', '/');
            double val = parseExpression(sanitized);
            if (Double.isNaN(val) || Double.isInfinite(val)) return "Error";
            if (val == (long) val) return String.valueOf((long) val);
            return String.format("%.2f", val);
        } catch (Exception e) {
            return "Error";
        }
    }

    private int pos;
    private String input;

    private double parseExpression() {
        double val = parseTerm();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '+') { pos++; val += parseTerm(); }
            else if (c == '-') { pos++; val -= parseTerm(); }
            else break;
        }
        return val;
    }

    private double parseExpression(String str) {
        input = str;
        pos = 0;
        return parseExpression();
    }

    private double parseTerm() {
        double val = parsePower();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '*') { pos++; val *= parsePower(); }
            else if (c == '/') { pos++; double den = parsePower(); if (den == 0) return Double.NaN; val /= den; }
            else break;
        }
        return val;
    }

    private double parsePower() {
        double val = parseAtom();
        while (pos < input.length() && input.charAt(pos) == '^') {
            pos++;
            double exp = parseAtom();
            val = Math.pow(val, exp);
        }
        return val;
    }

    private double parseAtom() {
        while (pos < input.length() && input.charAt(pos) == ' ') pos++;
        if (pos >= input.length()) return 0;
        char c = input.charAt(pos);
        if (c == '(') {
            pos++;
            double val = parseExpression();
            if (pos < input.length() && input.charAt(pos) == ')') pos++;
            return val;
        }
        if (c == '-') {
            pos++;
            return -parseAtom();
        }
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
            sb.append(input.charAt(pos++));
        }
        if (sb.isEmpty()) return 0;
        return Double.parseDouble(sb.toString());
    }
}
