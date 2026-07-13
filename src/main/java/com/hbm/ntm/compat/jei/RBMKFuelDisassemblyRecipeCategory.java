package com.hbm.ntm.compat.jei;

import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.neutron.RBMKFuelRodRegistry;
import com.hbm.ntm.neutron.RBMKFuelRodState;
import com.hbm.ntm.registry.ModItems;
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

import java.util.ArrayList;
import java.util.List;

public final class RBMKFuelDisassemblyRecipeCategory
        implements HbmJeiRecipeCategory<RBMKFuelDisassemblyRecipeCategory.DisplayRecipe> {
    private static final int WIDTH = LegacyNeiUniversalLayout.WIDTH;
    private static final int HEIGHT = LegacyNeiUniversalLayout.HEIGHT;

    private final RecipeType<DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    RBMKFuelDisassemblyRecipeCategory(RecipeType<DisplayRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalyst = new ItemStack(catalyst);
    }

    static List<DisplayRecipe> recipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (RBMKFuelRodRegistry.Entry entry : RBMKFuelRodRegistry.craftable()) {
            RegistryObject<Item> rod = ModItems.legacyItem(entry.legacyRodId());
            RegistryObject<Item> pellet = ModItems.legacyItem(entry.legacyPelletId());
            if (rod == null || pellet == null || !rod.isPresent() || !pellet.isPresent()) {
                continue;
            }
            for (int depletionMeta = 0; depletionMeta <= 4; depletionMeta++) {
                recipes.add(recipe(entry, rod.get(), pellet.get(), depletionMeta, false));
                if (entry.pelletXenonOverlay()) {
                    recipes.add(recipe(entry, rod.get(), pellet.get(), depletionMeta, true));
                }
            }
        }
        return recipes;
    }

    private static DisplayRecipe recipe(RBMKFuelRodRegistry.Entry entry, Item rod, Item pellet,
            int depletionMeta, boolean highXenon) {
        ItemStack input = new ItemStack(rod);
        if (input.getItem() instanceof RBMKFuelRodItem rodItem) {
            RBMKFuelRodState state = RBMKFuelRodState.fresh(entry.spec());
            state.setRemainingYield(Math.min(1.0D - depletionMeta / 5.0D, 0.99D) * entry.spec().totalYield());
            if (highXenon) {
                state.setXenon(50.0D);
            }
            rodItem.setState(input, state);
        }
        int pelletMeta = depletionMeta + (highXenon ? 5 : 0);
        ItemStack output = new ItemStack(pellet, RBMKFuelRodRegistry.DISASSEMBLY_PELLET_COUNT);
        output.setDamageValue(pelletMeta);
        return new DisplayRecipe(input, output, highXenon);
    }

    @Override
    public RecipeType<DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("jei.hbm_ntm_rebirth.rbmk_fuel_disassembly",
                "RBMK Rod Disassembly");
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
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, List.of(List.of(recipe.input())));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, List.of(List.of(recipe.output())));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }

    public record DisplayRecipe(ItemStack input, ItemStack output, boolean highXenon) {
        public DisplayRecipe {
            input = input.copy();
            output = output.copy();
        }
    }
}
