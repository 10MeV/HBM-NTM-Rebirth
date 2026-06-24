package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ReactorControlMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class ReactorControlScreen extends AbstractContainerScreen<ReactorControlMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gui_reactor_control.png");
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
            fields[i] = field(leftPos + 35 + 30 * i, topPos + 38, 26, 9, 3);
            addRenderableWidget(fields[i]);
            fields[i + 2] = field(leftPos + 35 + 30 * i, topPos + 49, 26, 9, 4);
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
        drawDisplay(graphics, 6, 20, menu.isLinked() ? menu.getLevelPercent() : 0, 3);
        drawDisplay(graphics, 66, 20, menu.isLinked() ? menu.getFlux() : 0, 4);
        drawDisplay(graphics, 126, 20, menu.isLinked() ? menu.getTemperature() : 0, 3);
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
        return super.mouseClicked(mouseX, mouseY, button);
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
        if (Character.isDigit(codePoint)) {
            for (EditBox field : fields) {
                if (field.charTyped(codePoint, modifiers)) {
                    return true;
                }
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
        try {
            return Mth.clamp(Integer.parseInt(fields[index].getValue()), 0, max);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private void drawCurve(GuiGraphics graphics) {
        int previousX = leftPos + 128;
        int previousY = curveY(0);
        for (int i = 1; i < 40; i++) {
            int x = leftPos + 128 + i;
            int y = curveY(i * 1250);
            drawSegment(graphics, previousX, previousY, x, y);
            previousX = x;
            previousY = y;
        }
    }

    private int curveY(int heat) {
        double value = switch (menu.getFunction()) {
            case 1 -> curveQuad(heat);
            case 2 -> curveLog(heat);
            default -> curveLinear(heat);
        };
        return topPos + 39 + Mth.clamp((int) (value * 0.01D * 28.0D), 0, 28);
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

    private void drawSegment(GuiGraphics graphics, int x1, int y1, int x2, int y2) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0.0D : i / (double) steps;
            int x = (int) Math.round(Mth.lerp(t, x1, x2));
            int y = (int) Math.round(Mth.lerp(t, y1, y2));
            graphics.fill(x, y, x + 2, y + 2, 0xFF08FF00);
        }
    }

    private void drawDisplay(GuiGraphics graphics, int x, int y, int value, int digits) {
        String text = Integer.toString(Math.max(0, value));
        if (text.length() > digits) {
            text = text.substring(text.length() - digits);
        }
        graphics.drawString(font, text, leftPos + x, topPos + y, 0x08FF00, false);
    }

    private void playClick() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.BLOCK_RBMK_AZ5_COVER.get(), 0.5F));
        }
    }
}
