package com.hbm.ntm.client.screen;

import com.hbm.ntm.blockentity.CableDiodeBlockEntity;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.menu.CableDiodeMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CableDiodeScreen extends AbstractContainerScreen<CableDiodeMenu> {
    private EditBox throughputField;
    private Button priorityButton;
    private int priorityOrdinal;

    public CableDiodeScreen(CableDiodeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 220;
        imageHeight = 126;
        titleLabelY = 10;
        inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        priorityOrdinal = menu.getPriorityOrdinal();
        throughputField = addRenderableWidget(LegacyGuiElements.createLegacyTextField(font, leftPos + 18, topPos + 53,
                120, 20, 11, Long.toString(menu.getThroughputLimit()), 0xFFFFFF, 0xAAAAAA));
        throughputField.setBordered(true);
        priorityButton = addRenderableWidget(Button.builder(priorityLabel(), button -> cyclePriority())
                .bounds(leftPos + 18, topPos + 90, 120, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Apply"), button -> sendConfiguration())
                .bounds(leftPos + 146, topPos + 90, 56, 20)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xEE202020);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xEE363636);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = title.getString();
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, titleLabelY, 0xFFFFFF, false);
        graphics.drawString(font, "Throughput:", 18, 34, 0xE0E0E0, false);
        graphics.drawString(font, "(max. 10,000,000,000 HE)", 18, 44, 0xA0A0A0, false);
        graphics.drawString(font, "Priority:", 18, 74, 0xE0E0E0, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void onClose() {
        sendConfiguration();
        super.onClose();
    }

    private void cyclePriority() {
        priorityOrdinal = (priorityOrdinal + 1) % HbmEnergyReceiver.ConnectionPriority.values().length;
        priorityButton.setMessage(priorityLabel());
    }

    private void sendConfiguration() {
        if (throughputField == null) {
            return;
        }
        long limit = menu.getThroughputLimit();
        try {
            limit = Long.parseLong(throughputField.getValue());
        } catch (NumberFormatException ignored) {
            // Matches the legacy GUI: an invalid field leaves the stored throughput unchanged.
        }
        CompoundTag tag = new CompoundTag();
        tag.putLong(CableDiodeBlockEntity.CONTROL_LIMIT, limit);
        tag.putInt(CableDiodeBlockEntity.CONTROL_PRIORITY, priorityOrdinal);
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
    }

    private Component priorityLabel() {
        HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
        int safeOrdinal = priorityOrdinal >= 0 && priorityOrdinal < values.length ? priorityOrdinal : 0;
        return Component.literal(values[safeOrdinal].name());
    }
}
