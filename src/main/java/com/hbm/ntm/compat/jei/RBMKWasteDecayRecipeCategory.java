package com.hbm.ntm.compat.jei;

import com.hbm.items.special.ItemWasteLong;
import com.hbm.items.special.ItemWasteShort;
import com.hbm.ntm.item.NuclearWasteItem;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

public final class RBMKWasteDecayRecipeCategory implements IRecipeCategory<RBMKWasteDecayRecipeCategory.DisplayRecipe> {
    private final RecipeType<DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    RBMKWasteDecayRecipeCategory(RecipeType<DisplayRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Nuclear Waste Decay");
    }

    @Override
    public int getWidth() {
        return LegacyNeiUniversalLayout.WIDTH;
    }

    @Override
    public int getHeight() {
        return LegacyNeiUniversalLayout.HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DisplayRecipe recipe, IFocusGroup focuses) {
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, List.of(List.of(recipe.input())));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, List.of(List.of(recipe.output())));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }

    static List<DisplayRecipe> recipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (ItemWasteShort.WasteClass waste : ItemWasteShort.WasteClass.values()) {
            int meta = waste.ordinal();
            addRecipe(recipes, "nuclear_waste_short", "nuclear_waste_short_depleted", meta);
            addRecipe(recipes, "nuclear_waste_short_tiny", "nuclear_waste_short_depleted_tiny", meta);
        }
        for (ItemWasteLong.WasteClass waste : ItemWasteLong.WasteClass.values()) {
            int meta = waste.ordinal();
            addRecipe(recipes, "nuclear_waste_long", "nuclear_waste_long_depleted", meta);
            addRecipe(recipes, "nuclear_waste_long_tiny", "nuclear_waste_long_depleted_tiny", meta);
        }
        return List.copyOf(recipes);
    }

    private static void addRecipe(List<DisplayRecipe> recipes, String inputName, String outputName, int meta) {
        ItemStack input = wasteStack(inputName, meta);
        ItemStack output = wasteStack(outputName, meta);
        if (!input.isEmpty() && !output.isEmpty()) {
            recipes.add(new DisplayRecipe(input, output));
        }
    }

    private static ItemStack wasteStack(String itemName, int meta) {
        RegistryObject<Item> item = ModItems.legacyItem(itemName);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return NuclearWasteItem.stack(item.get(), meta, 1);
    }

    public record DisplayRecipe(ItemStack input, ItemStack output) {
        public DisplayRecipe {
            input = input == null ? ItemStack.EMPTY : input.copy();
            output = output == null ? ItemStack.EMPTY : output.copy();
        }

        @Override
        public ItemStack input() {
            return input.copy();
        }

        @Override
        public ItemStack output() {
            return output.copy();
        }
    }
}
