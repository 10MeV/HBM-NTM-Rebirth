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
        throughputField = addRenderableWidget(LegacyGuiElements.createLegacyTextField(font, width / 2 - 150, 100,
                90, 20, 11, Long.toString(menu.getThroughputLimit()), 0xFFFFFF, 0xAAAAAA));
        throughputField.setBordered(true);
        priorityButton = addRenderableWidget(Button.builder(priorityLabel(), button -> cyclePriority())
                .bounds(width / 2 + 20, 100, 90, 20)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Legacy GUIDiode has no panel texture; drawDefaultBackground is supplied by renderBackground().
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, "Throughput:", width / 2 - 150 - leftPos, 80 - topPos, 0xA0A0A0, false);
        graphics.drawString(font, "(max. 10,000,000,000 HE)", width / 2 - 150 - leftPos, 90 - topPos,
                0xA0A0A0, false);
        graphics.drawString(font, "Priority:", width / 2 + 20 - leftPos, 80 - topPos, 0xA0A0A0, false);
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
