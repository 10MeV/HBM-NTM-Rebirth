package com.hbm.ntm.client.screen;

import com.hbm.ntm.menu.TurretMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import com.hbm.ntm.turret.TurretArtyBlockEntity;
import com.hbm.ntm.turret.TurretBlockEntityBase;
import com.hbm.ntm.turret.TurretHimarsBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TurretScreen extends AbstractContainerScreen<TurretMenu> {
    private EditBox nameField;
    private int whitelistIndex;

    public TurretScreen(TurretMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 222;
        inventoryLabelY = 128;
    }

    @Override
    protected void init() {
        super.init();
        nameField = new EditBox(font, leftPos + 10, topPos + 65, 50, 14, Component.empty());
        nameField.setMaxLength(25);
        nameField.setTextColor(0x00ff00);
        nameField.setBordered(false);
        addWidget(nameField);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        var texture = menu.getBlockEntity().getGuiTexture();
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int powerHeight = menu.getPowerBarHeight(53);
        if (powerHeight > 0) {
            graphics.blit(texture, leftPos + 152, topPos + 97 - powerHeight, 194, 52 - powerHeight, 16, powerHeight);
        }
        if (menu.isOn()) {
            graphics.blit(texture, leftPos + 115, topPos + 26, 176, 40, 18, 18);
        }
        if (menu.targetPlayers()) {
            graphics.blit(texture, leftPos + 8, topPos + 30, 176, 0, 10, 10);
        }
        if (menu.targetFriendly()) {
            graphics.blit(texture, leftPos + 22, topPos + 30, 176, 10, 10, 10);
        }
        if (menu.targetHostile()) {
            graphics.blit(texture, leftPos + 36, topPos + 30, 176, 20, 10, 10);
        }
        if (menu.targetMachines()) {
            graphics.blit(texture, leftPos + 50, topPos + 30, 176, 30, 10, 10);
        }
        if (menu.hasFluidTank()) {
            LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 134, topPos + 115,
                    7, 52, menu.getTankData());
        }
        drawArtilleryMode(graphics);
        drawHoverButtons(graphics, mouseX, mouseY);
        drawStattrak(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = title.getString();
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);

        List<String> whitelist = menu.getBlockEntity().getWhitelist();
        while (whitelistIndex >= whitelist.size() && whitelistIndex > 0) {
            whitelistIndex--;
        }
        String selectedName = whitelist.isEmpty()
                ? Component.translatable("container.hbm_ntm_rebirth.turret.none").getString()
                : whitelist.get(whitelistIndex);
        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.drawString(font, selectedName, 24, 102, 0x00ff00, false);
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        nameField.render(graphics, mouseX, mouseY, partialTick);
        renderFluidTooltip(graphics, mouseX, mouseY);
        renderButtonTooltip(graphics, mouseX, mouseY);
        renderAmmoTooltip(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(115, 26, 18, 18, mouseX, mouseY)) {
            send(TurretBlockEntityBase.controlTag("toggle_power"));
            return true;
        }
        if (menu.hasArtilleryMode() && isHovering(151, 16, 18, 18, mouseX, mouseY)) {
            send(TurretBlockEntityBase.controlTag("cycle_artillery_mode"));
            return true;
        }
        if (isHovering(8, 30, 10, 10, mouseX, mouseY)) {
            send(TurretBlockEntityBase.targetControlTag("players"));
            return true;
        }
        if (isHovering(22, 30, 10, 10, mouseX, mouseY)) {
            send(TurretBlockEntityBase.targetControlTag("friendly"));
            return true;
        }
        if (isHovering(36, 30, 10, 10, mouseX, mouseY)) {
            send(TurretBlockEntityBase.targetControlTag("hostile"));
            return true;
        }
        if (isHovering(50, 30, 10, 10, mouseX, mouseY)) {
            send(TurretBlockEntityBase.targetControlTag("machine"));
            return true;
        }
        if (isHovering(7, 80, 18, 18, mouseX, mouseY)) {
            int count = menu.getBlockEntity().getWhitelist().size();
            if (count > 0) {
                whitelistIndex = (whitelistIndex + count - 1) % count;
            }
            return true;
        }
        if (isHovering(43, 80, 18, 18, mouseX, mouseY)) {
            int count = menu.getBlockEntity().getWhitelist().size();
            if (count > 0) {
                whitelistIndex = (whitelistIndex + 1) % count;
            }
            return true;
        }
        if (isHovering(7, 98, 18, 18, mouseX, mouseY)) {
            String text = nameField.getValue();
            if (!text.isBlank()) {
                send(TurretBlockEntityBase.addWhitelistTag(text));
                nameField.setValue("");
            }
            return true;
        }
        if (isHovering(43, 98, 18, 18, mouseX, mouseY)) {
            send(TurretBlockEntityBase.removeWhitelistTag(whitelistIndex));
            return true;
        }
        return nameField.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return nameField.keyPressed(keyCode, scanCode, modifiers) || nameField.canConsumeInput()
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return nameField.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    private void send(net.minecraft.nbt.CompoundTag tag) {
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
    }

    private void drawHoverButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        var texture = menu.getBlockEntity().getGuiTexture();
        if (isHovering(7, 80, 18, 18, mouseX, mouseY)) {
            graphics.blit(texture, leftPos + 7, topPos + 80, 176, 58, 18, 18);
        }
        if (isHovering(43, 80, 18, 18, mouseX, mouseY)) {
            graphics.blit(texture, leftPos + 43, topPos + 80, 194, 58, 18, 18);
        }
        if (isHovering(7, 98, 18, 18, mouseX, mouseY)) {
            graphics.blit(texture, leftPos + 7, topPos + 98, 176, 76, 18, 18);
        }
        if (isHovering(43, 98, 18, 18, mouseX, mouseY)) {
            graphics.blit(texture, leftPos + 43, topPos + 98, 194, 76, 18, 18);
        }
    }

    private void drawStattrak(GuiGraphics graphics) {
        var texture = menu.getBlockEntity().getGuiTexture();
        int tallies = menu.getStattrak();
        if (tallies >= 36) {
            graphics.blit(texture, leftPos + 77, topPos + 50, 176, 120, 63, 6);
            return;
        }
        int steps = (int) Math.ceil(tallies / 5.0D);
        for (int step = 0; step < steps; step++) {
            int partial = tallies % 5;
            if (step < steps - 1 || partial == 0) {
                graphics.blit(texture, leftPos + 77 + 9 * step, topPos + 50, 194, 94, 9, 6);
            } else {
                graphics.blit(texture, leftPos + 77 + 9 * step, topPos + 50, 176, 94, partial * 2, 6);
            }
        }
    }

    private void drawArtilleryMode(GuiGraphics graphics) {
        if (!menu.hasArtilleryMode()) {
            return;
        }
        var texture = menu.getBlockEntity().getGuiTexture();
        if (menu.getBlockEntity() instanceof TurretArtyBlockEntity) {
            if (menu.getArtilleryMode() == TurretArtyBlockEntity.MODE_CANNON) {
                graphics.blit(texture, leftPos + 151, topPos + 16, 210, 0, 18, 18);
            } else if (menu.getArtilleryMode() == TurretArtyBlockEntity.MODE_MANUAL) {
                graphics.blit(texture, leftPos + 151, topPos + 16, 210, 18, 18, 18);
            }
        } else if (menu.getBlockEntity() instanceof TurretHimarsBlockEntity
                && menu.getArtilleryMode() == TurretHimarsBlockEntity.MODE_MANUAL) {
            graphics.blit(texture, leftPos + 151, topPos + 16, 210, 0, 18, 18);
        }
    }

    private void renderButtonTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (isHovering(152, 45, 16, 52, mouseX, mouseY)) {
            tooltip.add(Component.literal(menu.getPower() + " / " + menu.getMaxPower() + " HE"));
        } else if (menu.hasArtilleryMode() && isHovering(151, 16, 18, 18, mouseX, mouseY)) {
            tooltip.add(artilleryModeTooltip());
        } else if (isHovering(8, 30, 10, 10, mouseX, mouseY)) {
            tooltip.add(status("container.hbm_ntm_rebirth.turret.players", menu.targetPlayers()));
        } else if (isHovering(22, 30, 10, 10, mouseX, mouseY)) {
            tooltip.add(status("container.hbm_ntm_rebirth.turret.friendly", menu.targetFriendly()));
        } else if (isHovering(36, 30, 10, 10, mouseX, mouseY)) {
            tooltip.add(status("container.hbm_ntm_rebirth.turret.hostile", menu.targetHostile()));
        } else if (isHovering(50, 30, 10, 10, mouseX, mouseY)) {
            tooltip.add(status("container.hbm_ntm_rebirth.turret.machine", menu.targetMachines()));
        }
        if (!tooltip.isEmpty()) {
            List<FormattedCharSequence> lines = tooltip.stream().map(Component::getVisualOrderText).toList();
            graphics.renderTooltip(font, lines, mouseX, mouseY);
        }
    }

    private void renderFluidTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.hasFluidTank() || !isHovering(134, 63, 7, 52, mouseX, mouseY)) {
            return;
        }
        LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTankData(),
                menu.getTankTooltip(hasShiftDown()), mouseX, mouseY);
    }

    private void renderAmmoTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.getCarried().isEmpty() || !isHovering(79, 62, 54, 54, mouseX, mouseY)
                || hoveringOccupiedAmmoSlot(mouseX, mouseY)) {
            return;
        }
        List<ItemStack> ammo = new ArrayList<>(menu.getBlockEntity().getAmmoTypesForDisplay().stream()
                .filter(stack -> !stack.isEmpty())
                .toList());
        if (ammo.isEmpty()) {
            return;
        }
        int selectedIndex = 0;
        Component selectedName = ammo.get(selectedIndex).getHoverName();
        if (ammo.size() > 1) {
            selectedIndex = (int) ((System.currentTimeMillis() % (1000L * ammo.size())) / 1000L);
            selectedName = ammo.get(selectedIndex).getHoverName();
            ItemStack selectedMarker = ammo.get(selectedIndex).copy();
            selectedMarker.setCount(0);
            ammo.set(selectedIndex, selectedMarker);
        }

        List<List<LegacyGuiElements.StackTextPart>> lines = new ArrayList<>();
        if (ammo.size() < 10) {
            lines.add(stackLine(ammo));
        } else if (ammo.size() < 24) {
            lines.add(stackLine(ammo.subList(0, ammo.size() / 2)));
            lines.add(stackLine(ammo.subList(ammo.size() / 2, ammo.size())));
        } else {
            int bound0 = (int) Math.ceil(ammo.size() / 3.0D);
            int bound1 = (int) Math.ceil(ammo.size() / 3.0D * 2.0D);
            lines.add(stackLine(ammo.subList(0, bound0)));
            lines.add(stackLine(ammo.subList(bound0, bound1)));
            lines.add(stackLine(ammo.subList(bound1, ammo.size())));
        }
        lines.add(List.of(LegacyGuiElements.StackTextPart.text(selectedName)));
        LegacyGuiElements.renderStackText(graphics, font, lines, mouseX, mouseY);
    }

    private boolean hoveringOccupiedAmmoSlot(int mouseX, int mouseY) {
        for (int slot = TurretBlockEntityBase.SLOT_AMMO_START; slot <= TurretBlockEntityBase.SLOT_AMMO_END; slot++) {
            var menuSlot = menu.getSlot(slot);
            if (menuSlot.hasItem() && isHovering(menuSlot.x, menuSlot.y, 16, 16, mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private static List<LegacyGuiElements.StackTextPart> stackLine(List<ItemStack> stacks) {
        List<LegacyGuiElements.StackTextPart> parts = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            parts.add(LegacyGuiElements.StackTextPart.stack(stack));
        }
        return parts;
    }

    private static Component status(String key, boolean enabled) {
        return Component.translatable(key).append(": ").append(Component.translatable(enabled
                        ? "container.hbm_ntm_rebirth.turret.on"
                        : "container.hbm_ntm_rebirth.turret.off")
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private Component artilleryModeTooltip() {
        String name;
        if (menu.getBlockEntity() instanceof TurretArtyBlockEntity) {
            name = switch (menu.getArtilleryMode()) {
                case TurretArtyBlockEntity.MODE_CANNON -> "cannon";
                case TurretArtyBlockEntity.MODE_MANUAL -> "manual";
                default -> "artillery";
            };
        } else {
            name = menu.getArtilleryMode() == TurretHimarsBlockEntity.MODE_MANUAL ? "manual_rocket" : "artillery_rocket";
        }
        return Component.translatableWithFallback("turret.arty." + name, "Mode: " + name);
    }
}
