package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.FluidIconItem;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class FluidContainerRecipeCategory
        implements HbmJeiRecipeCategory<FluidContainerRecipeCategory.DisplayRecipe> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 86;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_fluid.png");

    private final RecipeType<DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;

    FluidContainerRecipeCategory(RecipeType<DisplayRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 0, 0, WIDTH, HEIGHT);
    }

    @Override
    public RecipeType<DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Fluid Containers");
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
    public void setRecipe(IRecipeLayoutBuilder builder, DisplayRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(30, 24)
                .addItemStack(recipe.fluid());
        if (!recipe.empty().isEmpty()) {
            builder.addInputSlot(48, 24)
                    .addItemStack(recipe.empty());
        }
        builder.addOutputSlot(120, 24)
                .addItemStack(recipe.full());
    }

    static List<DisplayRecipe> recipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (HbmFluidContainerRegistry.ContainerEntry entry : HbmFluidContainerRegistry.getAllContainers()) {
            if (entry.type() == HbmFluids.NONE || entry.content() <= 0 || entry.copyFullContainer().isEmpty()) {
                continue;
            }
            ItemStack fluid = FluidIconItem.make(entry.type(), entry.content());
            fluid.setCount(1);
            recipes.add(new DisplayRecipe(fluid, entry.copyEmptyContainer(), entry.copyFullContainer()));
        }
        return List.copyOf(recipes);
    }

    public record DisplayRecipe(ItemStack fluid, ItemStack empty, ItemStack full) {
        public DisplayRecipe {
            fluid = fluid == null ? ItemStack.EMPTY : fluid.copy();
            empty = empty == null ? ItemStack.EMPTY : empty.copy();
            full = full == null ? ItemStack.EMPTY : full.copy();
        }

        @Override
        public ItemStack fluid() {
            return fluid.copy();
        }

        @Override
        public ItemStack empty() {
            return empty.copy();
        }

        @Override
        public ItemStack full() {
            return full.copy();
        }
    }
}
