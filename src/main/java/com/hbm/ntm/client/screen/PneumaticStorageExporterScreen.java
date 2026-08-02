package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.PneumaticStorageExporterBlockEntity;
import com.hbm.ntm.menu.PneumaticStorageExporterMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class PneumaticStorageExporterScreen extends AbstractContainerScreen<PneumaticStorageExporterMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/storage/gui_pneumatic_exporter.png");
    public PneumaticStorageExporterScreen(PneumaticStorageExporterMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageWidth = 176; imageHeight = 185; inventoryLabelY = 91; }
    @Override protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) { graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight); if (menu.isRorConfigured()) { graphics.blit(TEXTURE, leftPos + 142, topPos + 52, 176, 18, 18, 18); graphics.blit(TEXTURE, leftPos + 14, topPos + 14, 77, 14, 58, 58); } if (!menu.isContinuous()) graphics.blit(TEXTURE, leftPos + 142, topPos + 16, 176, 0, 18, 18); if (menu.getRequestMode() == 1) graphics.blit(TEXTURE, leftPos + 142, topPos + 34, 194, 0, 18, 18); if (menu.getRequestMode() == 2) graphics.blit(TEXTURE, leftPos + 142, topPos + 34, 194, 18, 18, 18); }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) { graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 5, 0x404040, false); graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false); }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { boolean handled = super.mouseClicked(mouseX, mouseY, button); if (isHovering(142, 16, 18, 18, mouseX, mouseY)) { send(PneumaticStorageExporterBlockEntity.CONTROL_CONTINUOUS); return true; } if (isHovering(142, 34, 18, 18, mouseX, mouseY)) { send(PneumaticStorageExporterBlockEntity.CONTROL_REQUEST_MODE); return true; } if (isHovering(142, 52, 18, 18, mouseX, mouseY)) { send(PneumaticStorageExporterBlockEntity.CONTROL_ROR_FILTERS); return true; } return handled; }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) { renderBackground(graphics); super.render(graphics, mouseX, mouseY, partial); if (isHovering(142, 16, 18, 18, mouseX, mouseY)) graphics.renderTooltip(font, Component.literal(menu.isContinuous() ? "Request mode: Continuous" : "Request mode: By request"), mouseX, mouseY); else if (isHovering(142, 34, 18, 18, mouseX, mouseY)) graphics.renderTooltip(font, Component.literal(switch (menu.getRequestMode()) { case 1 -> "Request type: Only full stacks"; case 2 -> "Request type: Only full requests"; default -> "Request type: As much as possible"; }), mouseX, mouseY); else if (isHovering(142, 52, 18, 18, mouseX, mouseY)) graphics.renderTooltip(font, rorTooltip().stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY); renderTooltip(graphics, mouseX, mouseY); }
    private List<Component> rorTooltip() { List<Component> lines = new ArrayList<>(); lines.add(Component.literal("Filter type: " + (menu.isRorConfigured() ? "RoR configured" : "Manually configured"))); if (menu.isRorConfigured()) for (int i = 0; i < PneumaticStorageExporterBlockEntity.FILTER_SLOT_COUNT; i++) { var filter = menu.getBlockEntity().getRorFilter(i); lines.add(Component.literal("Slot " + (i + 1) + ": " + (filter.isEmpty() ? "None" : ("Item #" + net.minecraft.core.registries.BuiltInRegistries.ITEM.getId(filter.getItem()) + " with Meta " + filter.getDamageValue() + " x" + filter.getCount())))); } return lines; }
    private void send(int control) { ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), PneumaticStorageExporterBlockEntity.controlTag(control))); }
}
