package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.BlastFurnaceRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.util.BobMathUtil;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class BlastFurnaceRecipeCategory implements HbmJeiRecipeCategory<BlastFurnaceRecipe> {
    private static final int WIDTH = LegacyNeiUniversalLayout.WIDTH;
    private static final int HEIGHT = LegacyNeiUniversalLayout.HEIGHT;

    private final RecipeType<BlastFurnaceRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    BlastFurnaceRecipeCategory(RecipeType<BlastFurnaceRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<BlastFurnaceRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_blast_furnace", "Blast Furnace");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getRecipeBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlastFurnaceRecipe recipe, IFocusGroup focuses) {
        int[][] inputCoords = inputCoords(recipe.inputs().size());
        for (int i = 0; i < recipe.inputs().size(); i++) {
            HbmIngredient input = recipe.inputs().get(i);
            int[] pos = inputCoords[i];
            var slot = builder.addInputSlot(pos[0], pos[1])
                    .setBackground(slotBackground, -1, -1);
            List<ItemStack> stacks = input.displayStacks();
            if (!stacks.isEmpty()) {
                slot.addItemStacks(stacks);
            }
        }

        int[][] outputCoords = outputCoords(recipe.outputs().size());
        for (int i = 0; i < recipe.outputs().size(); i++) {
            HbmItemOutput output = recipe.outputs().get(i);
            int[] pos = outputCoords[i];
            var slot = builder.addOutputSlot(pos[0], pos[1])
                    .setBackground(slotBackground, -1, -1);
            List<ItemStack> stacks = output.displayStacks();
            if (!stacks.isEmpty()) {
                slot.addItemStacks(stacks);
            }
        }

        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }

    @Override
    public void draw(BlastFurnaceRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawBackground(guiGraphics);
        var font = Minecraft.getInstance().font;
        String duration = BobMathUtil.getShortNumber(recipe.duration()) + " ticks";
        int side = 164;
        guiGraphics.drawString(font, duration, side - font.width(duration), 45, 0x404040, false);
    }

    private static int[][] inputCoords(int count) {
        if (count == 1) {
            return new int[][] {{48, 24}};
        }
        if (count == 2) {
            return new int[][] {{30, 24}, {48, 24}};
        }
        if (count == 3) {
            return new int[][] {{12, 24}, {30, 24}, {48, 24}};
        }
        if (count == 4) {
            return new int[][] {{30, 15}, {48, 15}, {30, 33}, {48, 33}};
        }
        if (count == 5) {
            return new int[][] {{12, 15}, {30, 15}, {48, 15}, {12, 33}, {30, 33}};
        }
        if (count == 6) {
            return new int[][] {{12, 15}, {30, 15}, {48, 15}, {12, 33}, {30, 33}, {48, 33}};
        }

        int[][] slots = new int[count][2];
        int cols = (count + 2) / 3;
        for (int i = 0; i < count; i++) {
            slots[i][0] = 12 + (i % cols) * 18 - (cols == 4 ? 18 : 0);
            slots[i][1] = 6 + (i / cols) * 18;
        }
        return slots;
    }

    private static int[][] outputCoords(int count) {
        return switch (count) {
            case 1 -> new int[][] {{102, 24}};
            case 2 -> new int[][] {{102, 24}, {120, 24}};
            case 3 -> new int[][] {{102, 24}, {120, 24}, {138, 24}};
            case 4 -> new int[][] {{102, 15}, {120, 15}, {102, 33}, {120, 33}};
            case 5 -> new int[][] {{102, 15}, {120, 15}, {102, 33}, {120, 33}, {138, 24}};
            case 6 -> new int[][] {{102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}};
            case 7 -> new int[][] {
                    {102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}, {138, 24}
            };
            case 8 -> new int[][] {
                    {102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}, {138, 24}, {138, 42}
            };
            default -> new int[count][2];
        };
    }
}
