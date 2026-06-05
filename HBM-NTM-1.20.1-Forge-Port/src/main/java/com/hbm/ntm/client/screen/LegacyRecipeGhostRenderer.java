package com.hbm.ntm.client.screen;

import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class LegacyRecipeGhostRenderer {
    private static final int CYCLE_TICKS = 20;

    private LegacyRecipeGhostRenderer() {
    }

    static void renderItemInputGhosts(GuiGraphics graphics, Minecraft minecraft, AbstractContainerMenu menu,
            ResourceLocation guiTexture, int leftPos, int topPos, GenericMachineRecipe recipe, int[] menuSlotIndexes) {
        if (recipe == null || recipe.getItemInputs().isEmpty()) {
            return;
        }
        List<HbmIngredient> inputs = recipe.getItemInputs();
        List<GhostEntry> ghosts = new ArrayList<>();
        int count = Math.min(inputs.size(), menuSlotIndexes.length);
        for (int i = 0; i < count; i++) {
            int menuSlotIndex = menuSlotIndexes[i];
            if (menuSlotIndex < 0 || menuSlotIndex >= menu.slots.size()) {
                continue;
            }
            Slot slot = menu.slots.get(menuSlotIndex);
            if (slot.hasItem()) {
                continue;
            }
            ItemStack display = cyclingDisplayStack(inputs.get(i), minecraft);
            if (display.isEmpty()) {
                continue;
            }
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            ghosts.add(new GhostEntry(display, x, y, slot.x, slot.y));
        }
        if (ghosts.isEmpty()) {
            return;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 10.0F);
        for (GhostEntry ghost : ghosts) {
            graphics.renderItem(ghost.stack(), ghost.x(), ghost.y());
        }
        graphics.pose().popPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 300.0F);
        for (GhostEntry ghost : ghosts) {
            graphics.blit(guiTexture, ghost.x(), ghost.y(), ghost.u(), ghost.v(), 16, 16);
        }
        graphics.pose().popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static ItemStack cyclingDisplayStack(HbmIngredient ingredient, Minecraft minecraft) {
        List<ItemStack> stacks = ingredient.displayStacks();
        if (stacks.isEmpty()) {
            return ItemStack.EMPTY;
        }
        long tick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        ItemStack stack = stacks.get((int) ((tick / CYCLE_TICKS) % stacks.size())).copy();
        stack.setCount(ingredient.count());
        return stack;
    }

    private record GhostEntry(ItemStack stack, int x, int y, int u, int v) {
    }
}
