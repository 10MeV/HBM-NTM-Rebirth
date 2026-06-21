package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.ParticleAcceleratorBlock;
import com.hbm.ntm.blockentity.PADipoleBlockEntity;
import com.hbm.ntm.blockentity.PASourceBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.ParticleAcceleratorMenu;
import com.hbm.ntm.network.ModMessages;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Locale;

public class ParticleAcceleratorScreen extends AbstractContainerScreen<ParticleAcceleratorMenu> {
    private static final ResourceLocation SOURCE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/particleaccelerator/gui_source.png");
    private static final ResourceLocation DETECTOR =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/particleaccelerator/gui_detector.png");
    private static final ResourceLocation RFC =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/particleaccelerator/gui_rfc.png");
    private static final ResourceLocation QUADRUPOLE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/particleaccelerator/gui_quadrupole.png");
    private static final ResourceLocation DIPOLE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/particleaccelerator/gui_dipole.png");

    private EditBox threshold;

    public ParticleAcceleratorScreen(ParticleAcceleratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 204;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();
        if (menu.getVariant() == ParticleAcceleratorBlock.Variant.DIPOLE) {
            threshold = new EditBox(font, leftPos + 47, topPos + 77, 66, 8, Component.empty());
            threshold.setTextColor(0x00ff00);
            threshold.setBordered(false);
            threshold.setMaxLength(9);
            threshold.setValue(Integer.toString(menu.getThreshold()));
            addRenderableWidget(threshold);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture(), leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderPower(graphics);
        renderStatusLights(graphics);
        renderTanks(graphics);
        if (menu.getVariant() == ParticleAcceleratorBlock.Variant.SOURCE) {
            renderSourceState(graphics);
        } else if (menu.getVariant() == ParticleAcceleratorBlock.Variant.DIPOLE) {
            renderDipoleDirections(graphics);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.getVariant() != ParticleAcceleratorBlock.Variant.RFC) {
            String name = title.getString();
            graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2 - 9,
                    menu.getVariant() == ParticleAcceleratorBlock.Variant.SOURCE ? 4 : 6, 0x404040, false);
        }
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        TankCoords tanks = tankCoords();
        graphics.drawString(font, Component.literal("/123K").withStyle(net.minecraft.ChatFormatting.AQUA),
                tanks.coldX + 2, 22, 0x404040, false);
        int heat = menu.getTemperatureKelvin();
        Component label = Component.literal(heat + "K").withStyle(heat > 123
                ? net.minecraft.ChatFormatting.RED : net.minecraft.ChatFormatting.AQUA);
        graphics.drawString(font, label, tanks.hotX + 14 - font.width(label), 12, 0x404040, false);
        if (menu.getVariant() == ParticleAcceleratorBlock.Variant.SOURCE) {
            PASourceBlockEntity.PAState state = PASourceBlockEntity.PAState.byOrdinal(menu.getStateOrdinal());
            Component stateLabel = Component.translatable("pa." + state.key());
            graphics.drawString(font, stateLabel, 79 - font.width(stateLabel) / 2, 76, state.color(), false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(powerX(), 18, 16, 52, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal(menu.getPower() + " / "
                    + menu.getMaxPower() + " HE")), mouseX, mouseY);
        }
        TankCoords tanks = tankCoords();
        if (isHovering(tanks.coldX, 36, 16, 52, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, menu.getColdCoolant().tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()),
                    mouseX, mouseY);
        } else if (isHovering(tanks.hotX, 36, 16, 52, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, menu.getHotCoolant().tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()),
                    mouseX, mouseY);
        } else if (menu.getVariant() == ParticleAcceleratorBlock.Variant.SOURCE
                && isHovering(105, 18, 10, 10, mouseX, mouseY)) {
            PASourceBlockEntity.PAState state = PASourceBlockEntity.PAState.byOrdinal(menu.getStateOrdinal());
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal(String.format(Locale.US, "Last momentum: %,d", menu.getLastSpeed())),
                    Component.translatable("pa." + state.key() + ".desc")), mouseX, mouseY);
        } else if (menu.getVariant() == ParticleAcceleratorBlock.Variant.SOURCE
                && isHovering(105, 30, 10, 10, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal("Cancel operation")), mouseX, mouseY);
        } else if (menu.getVariant() == ParticleAcceleratorBlock.Variant.DIPOLE) {
            renderDipoleTooltip(graphics, mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.getVariant() == ParticleAcceleratorBlock.Variant.SOURCE
                && inside(mouseX, mouseY, 105, 30, 10, 10)) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("cancel", true);
            send(tag);
            playClick();
            return true;
        }
        if (menu.getVariant() == ParticleAcceleratorBlock.Variant.DIPOLE) {
            if (dipoleButton(mouseX, mouseY, 29, "lower")
                    || dipoleButton(mouseX, mouseY, 43, "upper")
                    || dipoleButton(mouseX, mouseY, 57, "redstone")) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        syncThreshold();
        return handled;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean handled = super.charTyped(codePoint, modifiers);
        syncThreshold();
        return handled;
    }

    private void renderPower(GuiGraphics graphics) {
        if (menu.getMaxPower() <= 0L) {
            return;
        }
        int fill = (int) Math.min(52L, Math.max(0L, menu.getPower() * 52L / menu.getMaxPower()));
        graphics.blit(texture(), leftPos + powerX(), topPos + 70 - fill, 184, 52 - fill, 16, fill);
    }

    private void renderTanks(GuiGraphics graphics) {
        TankCoords tanks = tankCoords();
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + tanks.coldX, topPos + 88, 16, 52,
                menu.getColdCoolant());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + tanks.hotX, topPos + 88, 16, 52,
                menu.getHotCoolant());
    }

    private void renderStatusLights(GuiGraphics graphics) {
        int heat = menu.getTemperatureKelvin();
        switch (menu.getVariant()) {
            case SOURCE, DETECTOR -> {
                if (heat <= 123) graphics.blit(texture(), leftPos + 44, topPos + 18, 176, 8, 8, 8);
                if (menu.getPower() >= menu.getUsageLow()) graphics.blit(texture(), leftPos + 44, topPos + 43, 176, 8, 8, 8);
            }
            case QUADRUPOLE -> {
                if (heat <= 123) graphics.blit(texture(), leftPos + 75, topPos + 64, 176, 8, 8, 8);
                if (!menu.getBlockEntity().getItems().getStackInSlot(1).isEmpty()) {
                    graphics.blit(texture(), leftPos + 85, topPos + 64, 176, 8, 8, 8);
                }
                if (menu.getPower() >= menu.getUsageLow()) graphics.blit(texture(), leftPos + 65, topPos + 64, 176, 8, 8, 8);
            }
            case DIPOLE -> {
                if (heat <= 123) graphics.blit(texture(), leftPos + 93, topPos + 54, 176, 8, 8, 8);
                if (!menu.getBlockEntity().getItems().getStackInSlot(1).isEmpty()) {
                    graphics.blit(texture(), leftPos + 103, topPos + 54, 176, 8, 8, 8);
                }
                if (menu.getPower() >= menu.getUsageLow()) graphics.blit(texture(), leftPos + 83, topPos + 54, 176, 8, 8, 8);
            }
            default -> {
            }
        }
    }

    private void renderSourceState(GuiGraphics graphics) {
        PASourceBlockEntity.PAState state = PASourceBlockEntity.PAState.byOrdinal(menu.getStateOrdinal());
        int color = state.color();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F, 1.0F);
        graphics.blit(texture(), leftPos + 45, topPos + 73, 176, 52, 68, 14);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private void renderDipoleDirections(GuiGraphics graphics) {
        drawDirection(graphics, 68, 35, menu.getDirLower());
        drawDirection(graphics, 68, 49, menu.getDirUpper());
        drawDirection(graphics, 68, 63, menu.getDirRedstone());
    }

    private void drawDirection(GuiGraphics graphics, int x, int y, int dir) {
        graphics.fill(leftPos + x - 6, topPos + y, leftPos + x + 7, topPos + y + 1, 0xFF8080FF);
        int dx = switch (dir & 3) {
            case 1 -> 6;
            case 3 -> -6;
            default -> 0;
        };
        int dy = switch (dir & 3) {
            case 2 -> 6;
            case 0 -> -6;
            default -> 0;
        };
        int x1 = Math.min(leftPos + x, leftPos + x + dx);
        int x2 = Math.max(leftPos + x + 1, leftPos + x + dx + 1);
        int y1 = Math.min(topPos + y, topPos + y + dy);
        int y2 = Math.max(topPos + y + 1, topPos + y + dy + 1);
        graphics.fill(x1, y1, x2, y2, 0xFFFF0000);
    }

    private void renderDipoleTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isHovering(62, 29, 12, 12, mouseX, mouseY)) {
            dipoleTooltip(graphics, mouseX, mouseY, menu.getDirLower());
        } else if (isHovering(62, 43, 12, 12, mouseX, mouseY)) {
            dipoleTooltip(graphics, mouseX, mouseY, menu.getDirUpper());
        } else if (isHovering(62, 57, 12, 12, mouseX, mouseY)) {
            dipoleTooltip(graphics, mouseX, mouseY, menu.getDirRedstone());
        }
    }

    private void dipoleTooltip(GuiGraphics graphics, int mouseX, int mouseY, int dir) {
        graphics.renderComponentTooltip(font, List.of(
                Component.literal("Player orientation"),
                Component.literal("Output orientation:"),
                Component.literal(PADipoleBlockEntity.ditToDirection(dir).getName())), mouseX, mouseY);
    }

    private boolean dipoleButton(double mouseX, double mouseY, int y, String key) {
        if (!inside(mouseX, mouseY, 62, y, 12, 12)) {
            return false;
        }
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, true);
        send(tag);
        playClick();
        return true;
    }

    private void syncThreshold() {
        if (threshold == null || !threshold.isFocused()) {
            return;
        }
        String text = threshold.getValue();
        if (text.isEmpty()) {
            text = "0";
        }
        if (!text.chars().allMatch(Character::isDigit)) {
            threshold.setValue(Integer.toString(menu.getThreshold()));
            return;
        }
        int value = Math.min(Integer.parseInt(text), 999_999_999);
        CompoundTag tag = new CompoundTag();
        tag.putInt("threshold", value);
        send(tag);
    }

    private void send(CompoundTag tag) {
        ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
    }

    private void playClick() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.5F, 1.0F);
        }
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return leftPos + x <= mouseX && mouseX < leftPos + x + width
                && topPos + y <= mouseY && mouseY < topPos + y + height;
    }

    private ResourceLocation texture() {
        return switch (menu.getVariant()) {
            case SOURCE -> SOURCE;
            case DETECTOR -> DETECTOR;
            case RFC -> RFC;
            case QUADRUPOLE -> QUADRUPOLE;
            case DIPOLE -> DIPOLE;
            case BEAMLINE -> SOURCE;
        };
    }

    private int powerX() {
        return switch (menu.getVariant()) {
            case RFC -> 53;
            case QUADRUPOLE -> 26;
            default -> 8;
        };
    }

    private TankCoords tankCoords() {
        return switch (menu.getVariant()) {
            case RFC -> new TankCoords(89, 107);
            case QUADRUPOLE -> new TankCoords(116, 134);
            default -> new TankCoords(134, 152);
        };
    }

    private record TankCoords(int coldX, int hotX) {
    }
}
