package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.FusionKlystronBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.FusionKlystronMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import com.hbm.ntm.util.BobMathUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FusionKlystronScreen extends AbstractContainerScreen<FusionKlystronMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_fusion_klystron.png");
    private EditBox targetField;
    private boolean updatingField;

    public FusionKlystronScreen(FusionKlystronMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 194;
        imageHeight = 200;
        inventoryLabelX = 17;
        inventoryLabelY = imageHeight - 93;
    }

    @Override
    protected void init() {
        super.init();
        targetField = LegacyGuiElements.createLegacyTextField(font, leftPos + 84, topPos + 22, 102, 12, 32,
                Long.toString(menu.getOutputTarget()));
        targetField.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        targetField.setResponder(this::targetChanged);
        addRenderableWidget(targetField);
        setInitialFocus(targetField);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (targetField != null && !targetField.isFocused()) {
            String target = Long.toString(menu.getOutputTarget());
            if (!target.equals(targetField.getValue())) {
                updatingField = true;
                targetField.setValue(target);
                updatingField = false;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = (int) (menu.getPower() * 52L / menu.getMaxPower());
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 70 - power, 194, 52 - power, 16, power);
        }
        double outputGauge = menu.getOutputTarget() <= 0L ? 0.0D
                : menu.getOutput() / (double) menu.getOutputTarget();
        double airGauge = menu.getAirTank().capacity() <= 0 ? 0.0D
                : menu.getAirTank().fill() / (double) menu.getAirTank().capacity();
        double powerGauge = getSpeedScaled(menu.getMaxPower(), menu.getPower());
        if (powerGauge >= 0.5D && menu.getOutput() > 0L) {
            graphics.blit(TEXTURE, leftPos + 160, topPos + 71, 210, 8, 8, 8);
        } else if (powerGauge > 0.0D) {
            graphics.blit(TEXTURE, leftPos + 160, topPos + 71, 210, 0, 8, 8);
        }
        if (airGauge >= 0.5D && menu.getOutput() > 0L) {
            graphics.blit(TEXTURE, leftPos + 170, topPos + 71, 210, 8, 8, 8);
        } else if (airGauge > 0.0D) {
            graphics.blit(TEXTURE, leftPos + 170, topPos + 71, 210, 0, 8, 8);
        }
        if (menu.getOutput() >= menu.getOutputTarget() && menu.getOutput() > 0L) {
            graphics.blit(TEXTURE, leftPos + 180, topPos + 71, 210, 8, 8, 8);
        } else if (menu.getOutput() > 0L) {
            graphics.blit(TEXTURE, leftPos + 180, topPos + 71, 210, 0, 8, 8);
        }
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 52, topPos + 80,
                Math.max(0.0D, Math.min(1.0D, outputGauge)), 5, 2, 1, 0xA00000);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 88, topPos + 80,
                Math.max(0.0D, Math.min(1.0D, airGauge)), 5, 2, 1, 0xA00000);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 124, topPos + 80,
                Math.max(0.0D, Math.min(1.0D, powerGauge)), 5, 2, 1, 0xA00000);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 76, topPos + 71, 18, 52, menu.getAirTank());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiElements.drawCenteredLabel(graphics, font, title, 115, 6, 160, 0x404040);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        String result = "= " + shortNumber(menu.getOutputTarget()) + "KyU";
        if (menu.getOutputTarget() == FusionKlystronBlockEntity.MAX_OUTPUT) {
            result += " (max)";
        }
        graphics.drawString(font, result, 183 - font.width(result), 40, 0x00FF00, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(8, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 8, topPos + 18, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(43, 71, 18, 18, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font, java.util.List.of(Component.literal("<- "
                    + shortNumber(menu.getOutput()) + "KyU / " + shortNumber(menu.getOutputTarget()) + "KyU")),
                    mouseX, mouseY);
        } else if (isHovering(76, 71, 18, 18, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, menu.getAirTank().tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()),
                    mouseX, mouseY);
        } else if (isHovering(115, 71, 18, 18, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font, java.util.List.of(Component.literal("-> "
                    + shortNumber(menu.getOutput()) + "HE / " + shortNumber(menu.getOutputTarget()) + "HE")),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void targetChanged(String value) {
        if (updatingField || value == null) {
            return;
        }
        String normalized = value;
        if (normalized.startsWith("0")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isEmpty()) {
            normalized = "0";
        }
        if (!normalized.equals(value) && targetField != null) {
            updatingField = true;
            targetField.setValue(normalized);
            updatingField = false;
        }
        try {
            sendTarget(Long.parseLong(normalized));
        } catch (NumberFormatException ignored) {
        }
    }

    private void sendTarget(long target) {
        long clamped = Math.max(0L, Math.min(FusionKlystronBlockEntity.MAX_OUTPUT, target));
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(),
                FusionKlystronBlockEntity.outputTargetControlTag(clamped)));
    }

    private static double getSpeedScaled(double max, double level) {
        if (max <= 0.0D) {
            return 0.0D;
        }
        if (level >= max * 0.5D) {
            return 1.0D;
        }
        return level / max * 2.0D;
    }

    private static String shortNumber(long value) {
        return BobMathUtil.getShortNumber(value);
    }
}
