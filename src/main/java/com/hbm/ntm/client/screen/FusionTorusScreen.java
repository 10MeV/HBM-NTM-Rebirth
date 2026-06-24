package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.FusionTorusBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.FusionTorusMenu;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeExtraData;
import com.hbm.ntm.util.BobMathUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FusionTorusScreen extends AbstractContainerScreen<FusionTorusMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_fusion_torus.png");

    public FusionTorusScreen(FusionTorusMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 230;
        imageHeight = 244;
        inventoryLabelX = 35;
        inventoryLabelY = imageHeight - 93;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = FusionTorusBlockEntity.MAX_POWER <= 0L ? 0
                : (int) (menu.getPower() * 62L / FusionTorusBlockEntity.MAX_POWER);
        graphics.blit(TEXTURE, leftPos + 8, topPos + 80 - power, 230, 62 - power, 16, power);
        int progress = (int) Math.ceil(70.0D * menu.getProgress());
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 98, topPos + 81, 0, 244, progress, 6);
        }
        int bonus = (int) Math.min(Math.ceil(70.0D * menu.getBonus()), 70.0D);
        if (bonus > 0) {
            graphics.blit(TEXTURE, leftPos + 98, topPos + 91, 0, 250, bonus, 6);
        }
        GenericMachineRecipe recipe = selectedRecipe();
        boolean didProcess = menu.getBlockEntity().didProcess();
        if (recipe != null && menu.getPower() >= recipe.getPower()) {
            graphics.blit(TEXTURE, leftPos + 160, topPos + 115, 246, 14, 8, 8);
        }
        int heat = (int) Math.ceil(menu.getTemperature());
        if (heat <= FusionTorusBlockEntity.TEMPERATURE_TARGET) {
            graphics.blit(TEXTURE, leftPos + 170, topPos + 115, 246, 14, 8, 8);
        }
        if (didProcess) {
            graphics.blit(TEXTURE, leftPos + 180, topPos + 115, 246, 14, 8, 8);
            graphics.blit(TEXTURE, leftPos + 87, topPos + 76, 249, 0, 3, 6);
            graphics.blit(TEXTURE, leftPos + 92, topPos + 76, 249, 0, 3, 6);
        } else if (recipe != null) {
            graphics.blit(TEXTURE, leftPos + 87, topPos + 76, 246, 0, 3, 6);
            graphics.blit(TEXTURE, leftPos + 92, topPos + 76, 246, 0, 3, 6);
        }
        FusionStats stats = fusionStats(recipe);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 52, topPos + 124,
                stats.ignitionTemp() <= 0L ? 0.0D : Math.min(menu.getKlystronEnergy() / (double) stats.ignitionTemp(), 1.5D) / 1.5D,
                5, 2, 1, 0xA00000);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 88, topPos + 124,
                stats.outputTemp() <= 0L ? 0.0D : Math.min(menu.getPlasmaEnergy() / (double) stats.outputTemp(), 1.0D),
                5, 2, 1, 0xA00000);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 124, topPos + 124,
                menu.getFuelConsumption(), 5, 2, 1, 0xA00000);
        graphics.renderItem(recipe == null ? LegacyGuiElements.templateFolderStack() : recipe.getIcon(),
                leftPos + 44, topPos + 81);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 44, topPos + 70, 16, 52, menu.getRecipeTank(0));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 62, topPos + 70, 16, 52, menu.getRecipeTank(1));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 80, topPos + 70, 16, 52, menu.getRecipeTank(2));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 152, topPos + 70, 16, 52, menu.getRecipeTank(3));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 188, topPos + 98, 16, 52, menu.getCoolantTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 206, topPos + 98, 16, 52, menu.getHotCoolantTank());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiElements.drawCenteredLabel(graphics, font, title, 106, 6, 160, 0x404040);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        graphics.drawString(font, Component.literal("/123K").withStyle(ChatFormatting.AQUA), 190, 32, 0x404040, false);
        int heatValue = (int) Math.ceil(menu.getTemperature());
        Component heat = Component.literal(heatValue + "K")
                .withStyle(heatValue > FusionTorusBlockEntity.TEMPERATURE_TARGET
                        ? ChatFormatting.RED : ChatFormatting.AQUA);
        graphics.drawString(font, heat, 220 - font.width(heat), 22, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTankTooltip(graphics, mouseX, mouseY, menu.getRecipeTank(0), 44, 18, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, menu.getRecipeTank(1), 62, 18, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, menu.getRecipeTank(2), 80, 18, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, menu.getRecipeTank(3), 152, 18, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, menu.getCoolantTank(), 188, 46, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, menu.getHotCoolantTank(), 206, 46, 16, 52);
        if (isHovering(8, 18, 16, 62, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 8, topPos + 18, 16, 62, menu.getPower(), FusionTorusBlockEntity.MAX_POWER);
        }
        GenericMachineRecipe recipe = selectedRecipe();
        FusionStats stats = fusionStats(recipe);
        if (isHovering(43, 80, 18, 18, mouseX, mouseY)) {
            if (recipe != null) {
                LegacyGuiElements.renderRecipeTooltip(graphics, font, recipe.getDisplayLines(), mouseX, mouseY);
            } else {
                graphics.renderTooltip(font,
                        Component.translatableWithFallback("gui.recipe.setRecipe", "Select recipe")
                                .withStyle(ChatFormatting.YELLOW), mouseX, mouseY);
            }
        } else if (isHovering(43, 115, 18, 18, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font, List.of(Component.literal("-> "
                    + shortNumber(menu.getKlystronEnergy()) + "KyU / " + shortNumber(stats.ignitionTemp()) + "KyU")), mouseX, mouseY);
        } else if (isHovering(79, 115, 18, 18, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font, List.of(Component.literal("<- "
                    + shortNumber(menu.getPlasmaEnergy()) + "TU / " + shortNumber(stats.outputTemp()) + "TU")), mouseX, mouseY);
        } else if (isHovering(115, 115, 18, 18, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font, fuelLines(recipe), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 43, 80, 18, 18)) {
            LegacyGuiElements.playClickSound();
            minecraft.setScreen(new FusionTorusRecipeSelectorScreen(this));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY, HbmFluidGuiHelper.TankData tank,
            int x, int y, int width, int height) {
        if (!isHovering(x, y, width, height, mouseX, mouseY)) return false;
        graphics.renderComponentTooltip(font, tank.tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()), mouseX, mouseY);
        return true;
    }

    private GenericMachineRecipe selectedRecipe() {
        return menu.getBlockEntity().getSelectedRecipeDefinition();
    }

    private static FusionStats fusionStats(GenericMachineRecipe recipe) {
        if (recipe == null) {
            return FusionStats.EMPTY;
        }
        return recipe.getExtraData().fusion()
                .map(data -> new FusionStats(data.ignitionTemp(), data.outputTemp()))
                .orElse(FusionStats.EMPTY);
    }

    private List<Component> fuelLines(GenericMachineRecipe recipe) {
        if (recipe == null) {
            return List.of(Component.literal("0mB/t"));
        }
        List<Component> lines = recipe.getFluidInputs().stream()
                .<Component>map(input -> Component.literal("-> "
                        + (int) Math.ceil(input.amount() * menu.getFuelConsumption()) + "mB/t ")
                        .append(input.type().getDisplayName()))
                .toList();
        return lines.isEmpty() ? List.of(Component.literal("0mB/t")) : lines;
    }

    private static String shortNumber(long value) {
        return BobMathUtil.getShortNumber(value);
    }

    private record FusionStats(long ignitionTemp, long outputTemp) {
        private static final FusionStats EMPTY = new FusionStats(0L, 0L);
    }
}
