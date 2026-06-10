package com.hbm.ntm.client.screen;

import com.hbm.ntm.util.HbmMathUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Legacy seven-segment GUI number display, ported from 1.7.10
 * {@code com.hbm.module.NumberDisplay}.
 */
public class LegacyNumberDisplay {
    private final int displayX;
    private final int displayY;
    private int color;
    private int padding = 3;
    private boolean blink = false;
    private float maxNum;
    private float minNum;
    private boolean customBounds = false;
    private boolean isFloat = false;
    private int floatPad = 1;
    private boolean pads = false;
    private int digitLength = 3;
    private Number numIn = 0;
    private char[] toDisp = { '0', '0', '0' };
    private int dispOffset = 0;
    private int verticalLength = 5;
    private int horizontalLength = 4;
    private int thickness = 1;

    public LegacyNumberDisplay(int x, int y, ChatFormatting color) {
        this(x, y);
        setColor(enumToColor(color));
    }

    public LegacyNumberDisplay(int x, int y, int color) {
        this(x, y);
        setColor(color);
    }

    public LegacyNumberDisplay(int x, int y) {
        this.displayX = x;
        this.displayY = y;
        setColor(0xFFFF55);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void drawNumber(GuiGraphics graphics, int guiLeft, int guiTop, char[] number) {
        drawNumber(graphics, guiLeft, guiTop, number, 0.0F);
    }

    public void drawNumber(GuiGraphics graphics, int guiLeft, int guiTop, char[] number, float layer) {
        if (blink && !HbmMathUtil.getBlink()) {
            return;
        }

        int gap = digitLength - number.length;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, layer);
        for (int i = 0; i < number.length; i++) {
            if (number[i] == '.') {
                gap--;
            }
            dispOffset = (padding + horizontalLength + 2 * thickness) * (i + gap);
            drawChar(graphics, guiLeft, guiTop, number[i]);
        }
        if (pads) {
            padOut(graphics, guiLeft, guiTop, gap);
        }
        graphics.pose().popPose();
    }

    public void drawNumber(GuiGraphics graphics, int guiLeft, int guiTop) {
        drawNumber(graphics, guiLeft, guiTop, 0.0F);
    }

    public void drawNumber(GuiGraphics graphics, int guiLeft, int guiTop, float layer) {
        if (isFloat) {
            formatForFloat();
        }
        drawNumber(graphics, guiLeft, guiTop, toDisp, layer);
    }

    public void drawNumber(GuiGraphics graphics, int guiLeft, int guiTop, Number number) {
        setNumber(number);
        drawNumber(graphics, guiLeft, guiTop);
    }

    public void drawNumber(GuiGraphics graphics, int guiLeft, int guiTop, Number number, float layer) {
        setNumber(number);
        drawNumber(graphics, guiLeft, guiTop, layer);
    }

    private void padOut(GuiGraphics graphics, int guiLeft, int guiTop, int gap) {
        if (gap == 0) {
            return;
        }
        for (int i = 0; i < gap; i++) {
            dispOffset = (padding + horizontalLength + 2 * thickness) * i;
            drawChar(graphics, guiLeft, guiTop, '0');
        }
    }

    public void drawChar(GuiGraphics graphics, int guiLeft, int guiTop, char number) {
        switch (number) {
            case '1' -> {
                drawVertical(graphics, guiLeft, guiTop, 1, 0);
                drawVertical(graphics, guiLeft, guiTop, 1, 1);
            }
            case '2' -> {
                drawHorizontal(graphics, guiLeft, guiTop, 0);
                drawVertical(graphics, guiLeft, guiTop, 1, 0);
                drawHorizontal(graphics, guiLeft, guiTop, 1);
                drawVertical(graphics, guiLeft, guiTop, 0, 1);
                drawHorizontal(graphics, guiLeft, guiTop, 2);
            }
            case '3' -> {
                drawHorizontal(graphics, guiLeft, guiTop, 0);
                drawHorizontal(graphics, guiLeft, guiTop, 1);
                drawHorizontal(graphics, guiLeft, guiTop, 2);
                drawVertical(graphics, guiLeft, guiTop, 1, 0);
                drawVertical(graphics, guiLeft, guiTop, 1, 1);
            }
            case '4' -> {
                drawVertical(graphics, guiLeft, guiTop, 0, 0);
                drawVertical(graphics, guiLeft, guiTop, 1, 0);
                drawVertical(graphics, guiLeft, guiTop, 1, 1);
                drawHorizontal(graphics, guiLeft, guiTop, 1);
            }
            case '5' -> {
                drawHorizontal(graphics, guiLeft, guiTop, 0);
                drawHorizontal(graphics, guiLeft, guiTop, 1);
                drawHorizontal(graphics, guiLeft, guiTop, 2);
                drawVertical(graphics, guiLeft, guiTop, 0, 0);
                drawVertical(graphics, guiLeft, guiTop, 1, 1);
            }
            case '6' -> {
                drawHorizontal(graphics, guiLeft, guiTop, 0);
                drawHorizontal(graphics, guiLeft, guiTop, 1);
                drawHorizontal(graphics, guiLeft, guiTop, 2);
                drawVertical(graphics, guiLeft, guiTop, 0, 0);
                drawVertical(graphics, guiLeft, guiTop, 0, 1);
                drawVertical(graphics, guiLeft, guiTop, 1, 1);
            }
            case '7' -> {
                drawHorizontal(graphics, guiLeft, guiTop, 0);
                drawVertical(graphics, guiLeft, guiTop, 1, 0);
                drawVertical(graphics, guiLeft, guiTop, 1, 1);
            }
            case '8' -> {
                drawHorizontal(graphics, guiLeft, guiTop, 0);
                drawHorizontal(graphics, guiLeft, guiTop, 1);
                drawHorizontal(graphics, guiLeft, guiTop, 2);
                drawVertical(graphics, guiLeft, guiTop, 0, 0);
                drawVertical(graphics, guiLeft, guiTop, 1, 0);
                drawVertical(graphics, guiLeft, guiTop, 0, 1);
                drawVertical(graphics, guiLeft, guiTop, 1, 1);
            }
            case '9' -> {
                drawHorizontal(graphics, guiLeft, guiTop, 0);
                drawHorizontal(graphics, guiLeft, guiTop, 1);
                drawHorizontal(graphics, guiLeft, guiTop, 2);
                drawVertical(graphics, guiLeft, guiTop, 0, 0);
                drawVertical(graphics, guiLeft, guiTop, 1, 0);
                drawVertical(graphics, guiLeft, guiTop, 1, 1);
            }
            case '0' -> {
                drawHorizontal(graphics, guiLeft, guiTop, 0);
                drawHorizontal(graphics, guiLeft, guiTop, 2);
                drawVertical(graphics, guiLeft, guiTop, 0, 0);
                drawVertical(graphics, guiLeft, guiTop, 0, 1);
                drawVertical(graphics, guiLeft, guiTop, 1, 0);
                drawVertical(graphics, guiLeft, guiTop, 1, 1);
            }
            case '-' -> drawHorizontal(graphics, guiLeft, guiTop, 1);
            case '.' -> drawPeriod(graphics, guiLeft, guiTop);
            case 'E' -> drawError(graphics, guiLeft, guiTop);
            default -> drawError(graphics, guiLeft, guiTop);
        }
    }

    private void drawError(GuiGraphics graphics, int guiLeft, int guiTop) {
        drawHorizontal(graphics, guiLeft, guiTop, 0);
        drawHorizontal(graphics, guiLeft, guiTop, 1);
        drawHorizontal(graphics, guiLeft, guiTop, 2);
        drawVertical(graphics, guiLeft, guiTop, 0, 0);
        drawVertical(graphics, guiLeft, guiTop, 0, 1);
    }

    private void drawHorizontal(GuiGraphics graphics, int guiLeft, int guiTop, int pos) {
        int offset = pos * (verticalLength + thickness);
        renderSegment(graphics, guiLeft + displayX + dispOffset + thickness, guiTop + displayY + offset,
                horizontalLength, thickness);
    }

    private void drawPeriod(GuiGraphics graphics, int guiLeft, int guiTop) {
        renderSegment(graphics,
                guiLeft + displayX + dispOffset + padding - (int) Math.ceil(padding / 2) + horizontalLength
                        + thickness,
                guiTop + displayY + 2 * (verticalLength + thickness), thickness, thickness);
    }

    private void drawVertical(GuiGraphics graphics, int guiLeft, int guiTop, int posX, int posY) {
        int offsetX = posX * (horizontalLength + thickness);
        int offsetY = posY * (verticalLength + thickness);
        renderSegment(graphics, guiLeft + displayX + offsetX + dispOffset, guiTop + displayY + offsetY + thickness,
                thickness, verticalLength);
    }

    private void renderSegment(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFF000000 | (color & 0xFFFFFF));
    }

    public void setNumber(Number number) {
        numIn = number;
        if (customBounds) {
            numIn = Mth.clamp(number.doubleValue(), minNum, maxNum);
        }
        if (isFloat) {
            formatForFloat();
        } else {
            toDisp = Long.toString(Math.round(numIn.doubleValue())).toCharArray();
            toDisp = truncOrExpand(toDisp);
        }
    }

    public Number getNumber() {
        return numIn;
    }

    public char[] getDispNumber() {
        return toDisp.clone();
    }

    public LegacyNumberDisplay setBlinks(boolean doesBlink) {
        blink = doesBlink;
        return this;
    }

    public LegacyNumberDisplay setPadding(int padding) {
        this.padding = padding;
        return this;
    }

    public LegacyNumberDisplay setDigitLength(int length) {
        digitLength = length;
        toDisp = truncOrExpand(toDisp);
        return this;
    }

    public LegacyNumberDisplay setSegmentSize(int vertical, int horizontal, int thickness) {
        verticalLength = vertical;
        horizontalLength = horizontal;
        this.thickness = thickness;
        return this;
    }

    public LegacyNumberDisplay setMaxMin(float max, float min) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum value is larger than maximum value!");
        }
        maxNum = max;
        minNum = min;
        customBounds = true;
        return this;
    }

    public LegacyNumberDisplay setPadNumber() {
        pads = true;
        return this;
    }

    public LegacyNumberDisplay setFloat() {
        return setFloat(1);
    }

    public LegacyNumberDisplay setFloat(int pad) {
        floatPad = pad;
        isFloat = true;
        formatForFloat();
        return this;
    }

    private void formatForFloat() {
        BigDecimal decimal = new BigDecimal(numIn.toString()).setScale(floatPad, RoundingMode.HALF_UP);
        char[] rounded = decimal.toString().toCharArray();
        rounded = Double.toString(HbmMathUtil.roundDecimal(numIn.doubleValue(), floatPad)).toCharArray();

        if (rounded.length == digitLength) {
            toDisp = rounded;
        } else {
            toDisp = truncOrExpand(rounded);
        }
    }

    private char[] truncOrExpand(char[] source) {
        if (isFloat) {
            char[] out = Arrays.copyOf(source, digitLength);
            for (int i = 0; i < digitLength; i++) {
                if (out[i] == '\u0000') {
                    out[i] = '0';
                }
            }
            return out.clone();
        }
        return source;
    }

    private static int enumToColor(ChatFormatting color) {
        if (color != null && color.isColor()) {
            return switch (color) {
                case AQUA -> 0x55FFFF;
                case BLACK -> 0x000000;
                case BLUE -> 0x5555FF;
                case DARK_AQUA -> 0x00AAAA;
                case DARK_BLUE -> 0x0000AA;
                case DARK_GRAY -> 0x555555;
                case DARK_GREEN -> 0x00AA00;
                case DARK_PURPLE -> 0xAA00AA;
                case DARK_RED -> 0xAA0000;
                case GOLD -> 0xFFAA00;
                case GRAY -> 0xAAAAAA;
                case GREEN -> 0x55FF55;
                case LIGHT_PURPLE -> 0xFF55FF;
                case RED -> 0xFF5555;
                case WHITE -> 0xFFFFFF;
                case YELLOW -> 0xFFFF55;
                default -> 0xFFFF55;
            };
        }
        return 0xFFFF55;
    }
}
