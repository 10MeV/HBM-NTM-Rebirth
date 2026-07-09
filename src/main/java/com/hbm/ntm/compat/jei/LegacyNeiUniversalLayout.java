package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.item.FluidIconItem;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

final class LegacyNeiUniversalLayout {
    static final int WIDTH = 166;
    static final int HEIGHT = 65;

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei.png");

    private LegacyNeiUniversalLayout() {
    }

    static IDrawableStatic background(IGuiHelper guiHelper) {
        return guiHelper.createDrawable(TEXTURE, 5, 11, WIDTH, HEIGHT);
    }

    static IDrawableStatic slotBackground(IGuiHelper guiHelper) {
        return guiHelper.createDrawable(TEXTURE, 5, 87, 18, 18);
    }

    static IDrawableStatic machineBackground(IGuiHelper guiHelper) {
        return guiHelper.createDrawable(TEXTURE, 59, 87, 18, 36);
    }

    static IDrawableStatic machineWithTemplateBackground(IGuiHelper guiHelper) {
        return guiHelper.createDrawable(TEXTURE, 77, 87, 18, 50);
    }

    static void addInputSlots(IRecipeLayoutBuilder builder, IDrawableStatic slotBackground,
            List<List<ItemStack>> inputs) {
        int[][] coords = inputCoords(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            List<ItemStack> stacks = inputs.get(i);
            if (!stacks.isEmpty()) {
                int[] pos = coords[i];
                builder.addInputSlot(pos[0], pos[1])
                        .setBackground(slotBackground, -1, -1)
                        .addItemStacks(stacks);
            }
        }
    }

    static void addOutputSlots(IRecipeLayoutBuilder builder, IDrawableStatic slotBackground,
            List<List<ItemStack>> outputs) {
        int[][] coords = outputCoords(outputs.size());
        for (int i = 0; i < outputs.size(); i++) {
            List<ItemStack> stacks = outputs.get(i);
            if (!stacks.isEmpty()) {
                int[] pos = coords[i];
                builder.addOutputSlot(pos[0], pos[1])
                        .setBackground(slotBackground, -1, -1)
                        .addItemStacks(stacks);
            }
        }
    }

    static void addMachineCatalyst(IRecipeLayoutBuilder builder, IDrawableStatic machineBackground,
            ItemStack catalyst) {
        addMachineCatalyst(builder, machineBackground, List.of(catalyst));
    }

    static void addMachineCatalyst(IRecipeLayoutBuilder builder, IDrawableStatic machineBackground,
            List<ItemStack> catalysts) {
        if (catalysts.isEmpty()) {
            return;
        }
        builder.addSlot(RecipeIngredientRole.CATALYST, 75, 31)
                .setBackground(machineBackground, -1, -17)
                .addItemStacks(catalysts);
    }

    static ItemStack fluidIcon(HbmFluidStack fluid) {
        return FluidIconItem.make(fluid.type(), fluid.amount(), fluid.pressure());
    }

    private static int[][] inputCoords(int count) {
        return switch (count) {
            case 1 -> new int[][] {{48, 24}};
            case 2 -> new int[][] {{48, 24}, {30, 24}};
            case 3 -> new int[][] {{48, 24}, {30, 24}, {12, 24}};
            case 4 -> new int[][] {{48, 15}, {30, 15}, {48, 33}, {30, 33}};
            case 5 -> new int[][] {{48, 15}, {30, 15}, {12, 24}, {48, 33}, {30, 33}};
            case 6 -> new int[][] {{48, 15}, {30, 15}, {12, 15}, {48, 33}, {30, 33}, {12, 33}};
            case 7 -> new int[][] {{48, 6}, {30, 15}, {12, 15}, {48, 24}, {30, 33}, {12, 33}, {48, 42}};
            case 8 -> new int[][] {
                    {48, 6}, {30, 6}, {12, 15}, {48, 24}, {30, 24}, {12, 33}, {48, 42}, {30, 42}
            };
            case 9 -> new int[][] {
                    {48, 6}, {30, 6}, {12, 6}, {48, 24}, {30, 24}, {12, 24}, {48, 42}, {30, 42}, {12, 42}
            };
            default -> generatedCoords(count);
        };
    }

    private static int[][] outputCoords(int count) {
        return switch (count) {
            case 1 -> new int[][] {{102, 24}};
            case 2 -> new int[][] {{102, 24}, {120, 24}};
            case 3 -> new int[][] {{102, 24}, {120, 24}, {138, 24}};
            case 4 -> new int[][] {{102, 15}, {120, 15}, {102, 33}, {120, 33}};
            case 5 -> new int[][] {{102, 15}, {120, 15}, {102, 33}, {120, 33}, {138, 24}};
            case 6 -> new int[][] {{102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}};
            case 7 -> new int[][] {{102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}, {138, 24}};
            case 8 -> new int[][] {
                    {102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}, {138, 24}, {138, 42}
            };
            default -> generatedCoords(count);
        };
    }

    private static int[][] generatedCoords(int count) {
        int[][] slots = new int[count][2];
        for (int i = 0; i < count; i++) {
            slots[i] = new int[] {i % 4 * 18, i / 4 * 18};
        }
        return slots;
    }
}
