package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public final class LegacyGuiElements {
    private static final ResourceLocation GUI_UTILITY = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/gui_utility.png");

    public static final int STANDARD_COLOR_BACKGROUND = -0xFEFFFF0;
    public static final int STANDARD_COLOR_LINE0 = 0x505000FF;
    public static final int STANDARD_COLOR_LINE1 = (STANDARD_COLOR_LINE0 & 0xFEFEFE) >> 1
            | STANDARD_COLOR_LINE0 & -0xFEFEFE;
    public static final int RECIPE_COLOR_LINE0 = 0xFFFF8000;
    public static final int RECIPE_COLOR_LINE1 = 0xFFFFFF00;
    public static final int PNEUMO_COLOR_LINE0 = 0xD57C4F;
    public static final int PNEUMO_COLOR_LINE1 = 0xAB4223;
    public static final int STANDARD_HEADER_OFFSET = 2;
    public static final int STANDARD_LINE_DIST = 10;

    private LegacyGuiElements() {
    }

    public static void drawSmoothGauge(GuiGraphics graphics, int x, int y, double progress, double tipLength,
            double backLength, double backSide, int color) {
        drawSmoothGauge(graphics, x, y, progress, tipLength, backLength, backSide, color, 0x000000);
    }

    public static void drawSmoothGauge(GuiGraphics graphics, int x, int y, double progress, double tipLength,
            double backLength, double backSide, int color, int colorOuter) {
        double clamped = Mth.clamp(progress, 0.0D, 1.0D);
        double angle = Math.toRadians(-clamped * 270.0D - 45.0D);
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        double tipX = -tipLength * sin;
        double tipY = tipLength * cos;
        double leftX = backSide * cos + backLength * sin;
        double leftY = backSide * sin - backLength * cos;
        double rightX = -backSide * cos + backLength * sin;
        double rightY = -backSide * sin - backLength * cos;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f pose = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        vertex(builder, pose, x + tipX * 1.5D, y + tipY * 1.5D, colorOuter);
        vertex(builder, pose, x + leftX * 1.5D, y + leftY * 1.5D, colorOuter);
        vertex(builder, pose, x + rightX * 1.5D, y + rightY * 1.5D, colorOuter);
        vertex(builder, pose, x + tipX, y + tipY, color);
        vertex(builder, pose, x + leftX, y + leftY, color);
        vertex(builder, pose, x + rightX, y + rightY, color);
        tesselator.end();
        RenderSystem.disableBlend();
    }

    public static void renderTooltip(GuiGraphics graphics, Font font, List<Component> lines, int mouseX, int mouseY) {
        renderTooltip(graphics, font, lines, mouseX, mouseY, STANDARD_HEADER_OFFSET, STANDARD_LINE_DIST,
                STANDARD_COLOR_BACKGROUND, STANDARD_COLOR_BACKGROUND, STANDARD_COLOR_LINE0, STANDARD_COLOR_LINE1);
    }

    public static void renderRecipeTooltip(GuiGraphics graphics, Font font, List<Component> lines, int mouseX,
            int mouseY) {
        renderTooltip(graphics, font, lines, mouseX, mouseY, 6, STANDARD_LINE_DIST, STANDARD_COLOR_BACKGROUND,
                STANDARD_COLOR_BACKGROUND, RECIPE_COLOR_LINE0, RECIPE_COLOR_LINE1);
    }

    public static void renderFluidTooltip(GuiGraphics graphics, Font font, HbmFluidGuiHelper.TankData tank,
            List<Component> lines, int mouseX, int mouseY) {
        renderFluidTooltip(graphics, font, tank == null ? null : tank.type(), lines, mouseX, mouseY);
    }

    public static void renderFluidTooltip(GuiGraphics graphics, Font font, FluidType type, List<Component> lines,
            int mouseX, int mouseY) {
        int color0 = type == null ? 0xFFFFFFFF : type.getColor();
        int red = (color0 >> 16) & 0xFF;
        int green = (color0 >> 8) & 0xFF;
        int blue = color0 & 0xFF;
        int add = (red + green + blue) / 3 > 0x80 ? -0x40 : 0x40;
        int color1 = color(Mth.clamp(red + add, 0, 255), Mth.clamp(green + add, 0, 255),
                Mth.clamp(blue + add, 0, 255));
        renderTooltip(graphics, font, lines, mouseX, mouseY, 6, STANDARD_LINE_DIST, STANDARD_COLOR_BACKGROUND,
                STANDARD_COLOR_BACKGROUND, color0 | 0xFF000000, color1 | 0xFF000000);
    }

    public static void renderPneumoTooltip(GuiGraphics graphics, Font font, List<Component> lines, int mouseX,
            int mouseY) {
        renderTooltip(graphics, font, lines, mouseX, mouseY, STANDARD_HEADER_OFFSET, STANDARD_LINE_DIST,
                STANDARD_COLOR_BACKGROUND, STANDARD_COLOR_BACKGROUND, PNEUMO_COLOR_LINE0, PNEUMO_COLOR_LINE1);
    }

    public static boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY;
    }

    public static boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY;
    }

    public static boolean checkClick(int mouseX, int mouseY, int guiLeft, int guiTop, int left, int top, int width,
            int height) {
        return isMouseOver(mouseX, mouseY, guiLeft + left, guiTop + top, width, height);
    }

    public static boolean checkClick(double mouseX, double mouseY, int guiLeft, int guiTop, int left, int top,
            int width, int height) {
        return isMouseOver(mouseX, mouseY, guiLeft + left, guiTop + top, width, height);
    }

    public static void renderInfoTooltip(GuiGraphics graphics, Font font, int mouseX, int mouseY, int x, int y,
            int width, int height, List<Component> lines) {
        if (isMouseOver(mouseX, mouseY, x, y, width, height)) {
            renderTooltip(graphics, font, lines, mouseX, mouseY);
        }
    }

    public static void renderInfoTooltip(GuiGraphics graphics, Font font, int mouseX, int mouseY, int x, int y,
            int width, int height, Component... lines) {
        renderInfoTooltip(graphics, font, mouseX, mouseY, x, y, width, height, List.of(lines));
    }

    public static void renderElectricityTooltip(GuiGraphics graphics, Font font, int mouseX, int mouseY, int x, int y,
            int width, int height, long power, long maxPower) {
        renderInfoTooltip(graphics, font, mouseX, mouseY, x, y, width, height,
                Component.literal(power + "/" + maxPower + "HE"));
    }

    public static void renderCustomInfoTooltip(GuiGraphics graphics, Font font, int mouseX, int mouseY, int x, int y,
            int width, int height, int tooltipX, int tooltipY, List<Component> lines) {
        if (isMouseOver(mouseX, mouseY, x, y, width, height)) {
            renderTooltip(graphics, font, lines, tooltipX, tooltipY);
        }
    }

    public static void renderCustomInfoTooltip(GuiGraphics graphics, Font font, int mouseX, int mouseY, int x, int y,
            int width, int height, int tooltipX, int tooltipY, Component... lines) {
        renderCustomInfoTooltip(graphics, font, mouseX, mouseY, x, y, width, height, tooltipX, tooltipY,
                List.of(lines));
    }

    public static void renderCustomInfoStat(GuiGraphics graphics, Font font, int mouseX, int mouseY, int x, int y,
            int width, int height, int tooltipX, int tooltipY, List<Component> lines) {
        renderCustomInfoTooltip(graphics, font, mouseX, mouseY, x, y, width, height, tooltipX, tooltipY, lines);
    }

    public static void renderCustomInfoStat(GuiGraphics graphics, Font font, int mouseX, int mouseY, int x, int y,
            int width, int height, int tooltipX, int tooltipY, Component... lines) {
        renderCustomInfoTooltip(graphics, font, mouseX, mouseY, x, y, width, height, tooltipX, tooltipY, lines);
    }

    public static void renderInfoPanel(GuiGraphics graphics, int x, int y, int type) {
        int u;
        int v;
        int width;
        int height;
        switch (type) {
            case 0 -> {
                u = 0;
                v = 0;
                width = 8;
                height = 8;
            }
            case 1 -> {
                u = 0;
                v = 8;
                width = 8;
                height = 8;
            }
            case 2 -> {
                u = 8;
                v = 0;
                width = 16;
                height = 16;
            }
            case 3 -> {
                u = 24;
                v = 0;
                width = 16;
                height = 16;
            }
            case 4 -> {
                u = 0;
                v = 16;
                width = 8;
                height = 8;
            }
            case 5 -> {
                u = 0;
                v = 24;
                width = 8;
                height = 8;
            }
            case 6 -> {
                u = 8;
                v = 16;
                width = 16;
                height = 16;
            }
            case 7 -> {
                u = 24;
                v = 16;
                width = 16;
                height = 16;
            }
            case 8 -> {
                u = 0;
                v = 32;
                width = 8;
                height = 8;
            }
            case 9 -> {
                u = 0;
                v = 40;
                width = 8;
                height = 8;
            }
            case 10 -> {
                u = 8;
                v = 32;
                width = 16;
                height = 16;
            }
            case 11 -> {
                u = 24;
                v = 32;
                width = 16;
                height = 16;
            }
            default -> {
                return;
            }
        }
        graphics.blit(GUI_UTILITY, x, y, u, v, width, height, 40, 48);
    }

    public static void renderItem(GuiGraphics graphics, ItemStack stack, int guiLeft, int guiTop, int x, int y) {
        renderItem(graphics, stack, guiLeft + x, guiTop + y);
    }

    public static void renderItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) {
            graphics.renderItem(stack, x, y);
        }
    }

    public static boolean isMouseOverSlot(Slot slot, int guiLeft, int guiTop, int mouseX, int mouseY) {
        return slot != null && slot.isActive()
                && checkClick(mouseX, mouseY, guiLeft, guiTop, slot.x, slot.y, 16, 16);
    }

    public static Slot getSlotAtPosition(List<? extends Slot> slots, int guiLeft, int guiTop, int mouseX, int mouseY) {
        if (slots == null) {
            return null;
        }
        for (Slot slot : slots) {
            if (isMouseOverSlot(slot, guiLeft, guiTop, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    public static void renderStackText(GuiGraphics graphics, Font font, List<List<StackTextPart>> lines, int mouseX,
            int mouseY) {
        if (lines == null || lines.isEmpty()) {
            return;
        }

        int tooltipHeight = 0;
        int tooltipWidth = 0;
        for (List<StackTextPart> line : lines) {
            int lineWidth = 0;
            boolean hasStack = false;
            for (StackTextPart part : line) {
                if (part.item()) {
                    lineWidth += 18;
                    hasStack = true;
                } else {
                    lineWidth += font.width(part.text().getVisualOrderText());
                }
            }
            tooltipWidth = Math.max(tooltipWidth, lineWidth);
            tooltipHeight += hasStack ? 18 : 10;
        }

        int boundX = mouseX + 12;
        int boundY = mouseY - 12;
        if (boundX + tooltipWidth > graphics.guiWidth()) {
            boundX -= 28 + tooltipWidth;
        }
        if (boundY + tooltipHeight + 6 > graphics.guiHeight()) {
            boundY = graphics.guiHeight() - tooltipHeight - 6;
        }
        if (boundX < 4) {
            boundX = 4;
        }
        if (boundY < 4) {
            boundY = 4;
        }

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, 400.0F);
        drawTooltipFrame(graphics, boundX, boundY, tooltipWidth, tooltipHeight, STANDARD_COLOR_BACKGROUND,
                STANDARD_COLOR_BACKGROUND, STANDARD_COLOR_LINE0, STANDARD_COLOR_LINE1);

        int lineY = boundY;
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            List<StackTextPart> line = lines.get(lineIndex);
            boolean hasStack = line.stream().anyMatch(StackTextPart::item);
            int indent = 0;
            for (StackTextPart part : line) {
                if (part.item()) {
                    ItemStack stack = part.stack();
                    if (stack.isEmpty() || stack.getCount() == 0) {
                        graphics.fillGradient(boundX + indent - 1, lineY - 1, boundX + indent + 17, lineY + 17,
                                0xFFFF0000, 0xFFFF0000);
                        graphics.fillGradient(boundX + indent, lineY, boundX + indent + 16, lineY + 16,
                                0xFFB0B0B0, 0xFFB0B0B0);
                    }
                    if (!stack.isEmpty()) {
                        graphics.renderItem(stack, boundX + indent, lineY);
                        graphics.renderItemDecorations(font, stack, boundX + indent, lineY);
                    }
                    indent += 18;
                } else {
                    FormattedCharSequence text = part.text().getVisualOrderText();
                    graphics.drawString(font, text, boundX + indent, lineY + (hasStack ? 4 : 0), 0xFFFFFFFF, true);
                    indent += font.width(text) + 2;
                }
            }
            if (lineIndex == 0) {
                lineY += 2;
            }
            lineY += hasStack ? 18 : 10;
        }
        pose.popPose();
    }

    public static void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public static EditBox createLegacyTextField(Font font, int x, int y, int width, int height, int maxLength,
            String value) {
        return createLegacyTextField(font, x, y, width, height, maxLength, value, 0x00FF00, 0x00FF00);
    }

    public static EditBox createLegacyTextField(Font font, int x, int y, int width, int height, int maxLength,
            String value, int textColor) {
        return createLegacyTextField(font, x, y, width, height, maxLength, value, textColor, textColor);
    }

    public static EditBox createLegacyTextField(Font font, int x, int y, int width, int height, int maxLength,
            String value, int textColor, int disabledTextColor) {
        EditBox field = new EditBox(font, x, y, width, height, Component.empty());
        configureLegacyTextField(field, maxLength, value, textColor, disabledTextColor);
        return field;
    }

    public static EditBox configureLegacyTextField(EditBox field, int maxLength, String value) {
        return configureLegacyTextField(field, maxLength, value, 0x00FF00, 0x00FF00);
    }

    public static EditBox configureLegacyTextField(EditBox field, int maxLength, String value, int textColor,
            int disabledTextColor) {
        field.setTextColor(textColor);
        field.setTextColorUneditable(disabledTextColor);
        field.setBordered(false);
        field.setMaxLength(maxLength);
        field.setValue(value == null ? "" : value);
        return field;
    }

    public static int applyLegacyPageScroll(int page, int maxPage, double delta) {
        if (delta > 0.0D) {
            return Math.max(0, page - 1);
        }
        if (delta < 0.0D) {
            return Math.min(Math.max(0, maxPage), page + 1);
        }
        return Mth.clamp(page, 0, Math.max(0, maxPage));
    }

    public static void renderTooltip(GuiGraphics graphics, Font font, List<Component> lines, int mouseX, int mouseY,
            int headerOffset, int lineDist, int background0, int background1, int line0, int line1) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        List<FormattedCharSequence> visualLines = lines.stream().map(Component::getVisualOrderText).toList();
        int tooltipWidth = 0;
        for (FormattedCharSequence line : visualLines) {
            tooltipWidth = Math.max(tooltipWidth, font.width(line));
        }

        int boundX = mouseX + 12;
        int boundY = mouseY - 12;
        int tooltipHeight = 6 + headerOffset;
        if (visualLines.size() > 1) {
            tooltipHeight += 2 + (visualLines.size() - 1) * lineDist;
        }

        if (boundX + tooltipWidth + 4 > graphics.guiWidth()) {
            boundX -= 28 + tooltipWidth;
        }
        if (boundY + tooltipHeight + 6 > graphics.guiHeight()) {
            boundY = graphics.guiHeight() - tooltipHeight - 6;
        }
        if (boundX < 4) {
            boundX = 4;
        }
        if (boundY < 4) {
            boundY = 4;
        }

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, 400.0F);
        drawTooltipFrame(graphics, boundX, boundY, tooltipWidth, tooltipHeight, background0, background1, line0,
                line1);

        int textY = boundY;
        for (int i = 0; i < visualLines.size(); i++) {
            graphics.drawString(font, visualLines.get(i), boundX, textY, 0xFFFFFFFF, true);
            if (i == 0) {
                textY += headerOffset;
            }
            textY += lineDist;
        }
        pose.popPose();
    }

    private static void drawTooltipFrame(GuiGraphics graphics, int x, int y, int width, int height, int background0,
            int background1, int line0, int line1) {
        graphics.fillGradient(x - 3, y - 4, x + width + 3, y - 3, background0, background0);
        graphics.fillGradient(x - 3, y + height + 3, x + width + 3, y + height + 4, background1, background1);
        graphics.fillGradient(x - 3, y - 3, x + width + 3, y + height + 3, background0, background1);
        graphics.fillGradient(x - 4, y - 3, x - 3, y + height + 3, background0, background1);
        graphics.fillGradient(x + width + 3, y - 3, x + width + 4, y + height + 3, background0, background1);

        graphics.fillGradient(x - 3, y - 2, x - 2, y + height + 2, line0, line1);
        graphics.fillGradient(x + width + 2, y - 2, x + width + 3, y + height + 2, line0, line1);
        graphics.fillGradient(x - 3, y - 3, x + width + 3, y - 2, line0, line0);
        graphics.fillGradient(x - 3, y + height + 2, x + width + 3, y + height + 3, line1, line1);
    }

    private static void vertex(BufferBuilder builder, Matrix4f pose, double x, double y, int color) {
        builder.vertex(pose, (float) x, (float) y, 0.0F)
                .color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, 0xFF)
                .endVertex();
    }

    private static int color(int red, int green, int blue) {
        return (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    public record StackTextPart(Component text, ItemStack stack, boolean item) {
        public static StackTextPart text(String text) {
            return text(Component.literal(text));
        }

        public static StackTextPart text(Component text) {
            return new StackTextPart(text, ItemStack.EMPTY, false);
        }

        public static StackTextPart stack(ItemStack stack) {
            return new StackTextPart(Component.empty(), stack == null ? ItemStack.EMPTY : stack, true);
        }
    }
}
