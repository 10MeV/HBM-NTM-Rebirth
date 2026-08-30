package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ReactorControlMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Matrix4f;

public class ReactorControlScreen extends AbstractContainerScreen<ReactorControlMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gui_reactor_control.png");
    private final LegacyNumberDisplay[] displays = {
            new LegacyNumberDisplay(6, 20, 0x08FF00).setDigitLength(3),
            new LegacyNumberDisplay(66, 20, 0x08FF00).setDigitLength(4),
            new LegacyNumberDisplay(126, 20, 0x08FF00).setDigitLength(3)
    };
    private final EditBox[] fields = new EditBox[4];

    public ReactorControlScreen(ReactorControlMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < 2; i++) {
            fields[i] = field(leftPos + 35 + 30 * i, topPos + 38, 26, 7, 3);
            addRenderableWidget(fields[i]);
            fields[i + 2] = field(leftPos + 35 + 30 * i, topPos + 49, 26, 7, 4);
            addRenderableWidget(fields[i + 2]);
        }
        fields[0].setValue(Integer.toString(menu.getLevelUpper()));
        fields[1].setValue(Integer.toString(menu.getLevelLower()));
        fields[2].setValue(Integer.toString(menu.getHeatUpper()));
        fields[3].setValue(Integer.toString(menu.getHeatLower()));
    }

    private EditBox field(int x, int y, int width, int height, int maxLength) {
        EditBox field = new EditBox(font, x, y, width, height, Component.empty());
        field.setBordered(false);
        field.setTextColor(0x08FF00);
        field.setMaxLength(maxLength);
        return field;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        drawCurve(graphics);
        displays[0].drawNumber(graphics, leftPos, topPos, menu.isLinked() ? menu.getLevelPercent() : 0);
        displays[1].drawNumber(graphics, leftPos, topPos, menu.isLinked() ? menu.getFlux() : 0);
        displays[2].drawNumber(graphics, leftPos, topPos, menu.isLinked() ? menu.getTemperature() : 0);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        for (EditBox field : fields) {
            field.setFocused(field.isMouseOver(mouseX, mouseY));
        }
        if (leftPos + 33 <= mouseX && mouseX < leftPos + 91 && topPos + 59 < mouseY && mouseY <= topPos + 69) {
            sendBounds();
            return true;
        }
        for (int function = 0; function < 3; function++) {
            if (leftPos + 7 <= mouseX && mouseX < leftPos + 29
                    && topPos + 37 + function * 11 < mouseY
                    && mouseY <= topPos + 47 + function * 11) {
                sendFunction(function);
                return true;
            }
        }
        return handled;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (EditBox field : fields) {
            if (field.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (EditBox field : fields) {
            if (field.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void sendBounds() {
        int levelUpper = clampField(0, 100);
        int levelLower = clampField(1, 100);
        int heatUpper = clampField(2, 1000);
        int heatLower = clampField(3, 1000);
        fields[0].setValue(Integer.toString(levelUpper));
        fields[1].setValue(Integer.toString(levelLower));
        fields[2].setValue(Integer.toString(heatUpper));
        fields[3].setValue(Integer.toString(heatLower));

        CompoundTag data = new CompoundTag();
        data.putDouble("levelUpper", levelUpper);
        data.putDouble("levelLower", levelLower);
        data.putDouble("heatUpper", heatUpper * 50.0D);
        data.putDouble("heatLower", heatLower * 50.0D);
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), data));
        playClick();
    }

    private void sendFunction(int function) {
        CompoundTag data = new CompoundTag();
        data.putInt("function", function);
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), data));
        playClick();
    }

    private int clampField(int index, int max) {
        String value = fields[index].getValue();
        if (!isDigits(value)) {
            return 0;
        }
        try {
            return Mth.clamp(Integer.parseInt(value), 0, max);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static boolean isDigits(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void drawCurve(GuiGraphics graphics) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(3.0F);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < 40; i++) {
            buffer.vertex(matrix, leftPos + 128.0F + i, curveY(i * 1250), 0.0F)
                    .color(8, 255, 0, 255)
                    .endVertex();
        }
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.lineWidth(1.0F);
    }

    private float curveY(int heat) {
        double value = switch (menu.getFunction()) {
            case 1 -> curveQuad(heat);
            case 2 -> curveLog(heat);
            default -> curveLinear(heat);
        };
        return topPos + 39.0F + (float) Mth.clamp(value * 0.01D * 28.0D, 0.0D, 28.0D);
    }

    private double curveLinear(int heat) {
        double heatRange = heatRange();
        if (heatRange == 0.0D) {
            return menu.getLevelLower();
        }
        return (heat - menu.getHeatLower() * 50.0D)
                * ((menu.getLevelUpper() - menu.getLevelLower()) / heatRange)
                + menu.getLevelLower();
    }

    private double curveQuad(int heat) {
        double heatRange = heatRange();
        if (heatRange == 0.0D) {
            return menu.getLevelLower();
        }
        return Math.pow((heat - menu.getHeatLower() * 50.0D) / heatRange, 2)
                * (menu.getLevelUpper() - menu.getLevelLower()) + menu.getLevelLower();
    }

    private double curveLog(int heat) {
        double heatRange = heatRange();
        if (heatRange == 0.0D) {
            return menu.getLevelUpper();
        }
        return Math.pow((heat - menu.getHeatUpper() * 50.0D) / -heatRange, 2)
                * (menu.getLevelLower() - menu.getLevelUpper()) + menu.getLevelUpper();
    }

    private double heatRange() {
        return (menu.getHeatUpper() - menu.getHeatLower()) * 50.0D;
    }

    private void playClick() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }
}
