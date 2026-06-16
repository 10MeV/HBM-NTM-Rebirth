package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.MixerMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.recipe.MixerRecipe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class MixerScreen extends AbstractContainerScreen<MixerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_mixer.png");

    public MixerScreen(MixerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 204;
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(53);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 23, topPos + 75 - power, 176, 52 - power, 16, power);
        }
        int progress = menu.getProgressWidth(53);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 62, topPos + 36, 192, 0, progress, 44);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 43, topPos + 75, 7, 52, menu.tank(0));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 52, topPos + 75, 7, 52, menu.tank(1));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 117, topPos + 75, 16, 52, menu.tank(2));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(23, 23, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 23, topPos + 23, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(43, 23, 7, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.tank(0),
                    menu.tankTooltip(0, hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(52, 23, 7, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.tank(1),
                    menu.tankTooltip(1, hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(117, 23, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.tank(2),
                    menu.tankTooltip(2, hasShiftDown()), mouseX, mouseY);
        } else if (hasMultipleRecipes() && isHovering(62, 22, 12, 12, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, recipeTooltip(), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hasMultipleRecipes()
                && isHovering(62, 22, 12, 12, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity(), 0, com.hbm.ntm.blockentity.MixerBlockEntity.CONTROL_NEXT_RECIPE);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean hasMultipleRecipes() {
        return menu.getBlockEntity().getRecipesForOutput().size() > 1;
    }

    private List<Component> recipeTooltip() {
        List<MixerRecipe> recipes = menu.getBlockEntity().getRecipesForOutput();
        if (recipes.isEmpty()) {
            return List.of();
        }
        MixerRecipe recipe = recipes.get(Math.floorMod(menu.getRecipeIndex(), recipes.size()));
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("Current recipe (" + (Math.floorMod(menu.getRecipeIndex(), recipes.size()) + 1)
                + "/" + recipes.size() + "):").withStyle(ChatFormatting.YELLOW));
        recipe.input1().ifPresent(input -> tooltip.add(input.type().getDisplayName().copy().append(
                Component.literal(" " + input.amount() + "mB"))));
        recipe.input2().ifPresent(input -> tooltip.add(input.type().getDisplayName().copy().append(
                Component.literal(" " + input.amount() + "mB"))));
        recipe.solidInput().ifPresent(input -> {
            List<ItemStack> stacks = input.displayStacks();
            if (!stacks.isEmpty()) {
                tooltip.add(stacks.get((int) (System.currentTimeMillis() / 1000L % stacks.size())).getHoverName());
            }
        });
        tooltip.add(Component.literal("Click to change!").withStyle(ChatFormatting.RED));
        return tooltip;
    }
}
