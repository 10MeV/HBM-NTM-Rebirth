package com.hbm.ntm.client.screen;

import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.menu.FluidPumpMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Locale;

public class FluidPumpScreen extends AbstractContainerScreen<FluidPumpMenu> {
    private EditBox throughputField;
    private Button pressureButton;
    private Button priorityButton;
    private int pressure;
    private int priorityOrdinal;

    public FluidPumpScreen(FluidPumpMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 320;
        imageHeight = 144;
        inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        pressure = menu.getPressure();
        priorityOrdinal = menu.getPriority().ordinal();

        throughputField = addRenderableWidget(new EditBox(font, leftPos + 10, topPos + 55, 90, 20,
                Component.empty()));
        throughputField.setMaxLength(5);
        throughputField.setValue(Integer.toString(menu.getBufferSize()));
        throughputField.setFilter(value -> value.isEmpty() || value.matches("\\d{0,5}"));

        pressureButton = addRenderableWidget(Button.builder(pressureLabel(), button -> cyclePressure())
                .bounds(leftPos + 115, topPos + 55, 90, 20)
                .build());
        priorityButton = addRenderableWidget(Button.builder(priorityLabel(), button -> cyclePriority())
                .bounds(leftPos + 220, topPos + 55, 90, 20)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xC0101010);
        graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF202020);
        graphics.fill(leftPos + 8, topPos + 27, leftPos + imageWidth - 8, topPos + 28, 0xFF606060);
        graphics.fill(leftPos + 8, topPos + 96, leftPos + imageWidth - 8, topPos + 97, 0xFF606060);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 8, 0xE0E0E0, false);
        graphics.drawString(font, "Throughput:", 10, 36, 0xA0A0A0, false);
        graphics.drawString(font, "(max. 10,000mB)", 10, 46, 0xA0A0A0, false);
        graphics.drawString(font, "Pressure:", 115, 36, 0xA0A0A0, false);
        graphics.drawString(font, "Priority:", 220, 36, 0xA0A0A0, false);
        graphics.drawString(font, statusText(), 10, 108, 0xA0A0A0, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(10, 55, 90, 20, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal("0 - 10,000 mB/t")), mouseX, mouseY);
        }
    }

    @Override
    public void onClose() {
        sendSettings();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void cyclePressure() {
        pressure++;
        if (pressure > 5) {
            pressure = 0;
        }
        pressureButton.setMessage(pressureLabel());
        playClickSound();
    }

    private void cyclePriority() {
        priorityOrdinal++;
        if (priorityOrdinal >= HbmEnergyReceiver.ConnectionPriority.values().length) {
            priorityOrdinal = 0;
        }
        priorityButton.setMessage(priorityLabel());
        playClickSound();
    }

    private Component pressureLabel() {
        return Component.literal(pressure + " PU");
    }

    private Component priorityLabel() {
        HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
        HbmEnergyReceiver.ConnectionPriority priority = priorityOrdinal >= 0 && priorityOrdinal < values.length
                ? values[priorityOrdinal]
                : HbmEnergyReceiver.ConnectionPriority.NORMAL;
        return Component.literal(priority.name());
    }

    private String statusText() {
        String state = menu.isRedstoneBlocked() ? "redstone locked" : "active";
        return String.format(Locale.US, "Buffered: %,d mB, %s", menu.getFill(), state);
    }

    private void sendSettings() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("pressure", (byte) pressure);
        tag.putByte("priority", (byte) priorityOrdinal);
        try {
            tag.putInt("capacity", Integer.parseInt(throughputField.getValue()));
        } catch (NumberFormatException ignored) {
        }
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
    }

    private static void playClickSound() {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
