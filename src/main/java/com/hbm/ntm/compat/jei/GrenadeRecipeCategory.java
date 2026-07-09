package com.hbm.ntm.compat.jei;

import com.hbm.ntm.item.UniversalGrenadeItem;
import com.hbm.ntm.item.UniversalGrenadeItem.Extra;
import com.hbm.ntm.item.UniversalGrenadeItem.Filling;
import com.hbm.ntm.item.UniversalGrenadeItem.Fuze;
import com.hbm.ntm.item.UniversalGrenadeItem.Shell;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class GrenadeRecipeCategory implements IRecipeCategory<GrenadeRecipeCategory.DisplayRecipe> {
    private final RecipeType<DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;

    GrenadeRecipeCategory(RecipeType<DisplayRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
    }

    @Override
    public RecipeType<DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Grenade Crafting");
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
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, recipe.inputs());
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, List.of(List.of(recipe.output())));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, recipe.catalyst());
    }

    static List<DisplayRecipe> recipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        int sourceOrder = 0;
        for (Shell shell : Shell.values()) {
            for (Filling filling : Filling.values()) {
                if (!filling.compatible(shell)) {
                    continue;
                }
                for (Fuze fuze : Fuze.values()) {
                    recipes.add(display(shell, filling, fuze, null, sourceOrder++));
                    for (Extra extra : Extra.values()) {
                        recipes.add(display(shell, filling, fuze, extra, sourceOrder++));
                    }
                }
            }
        }
        return List.copyOf(recipes);
    }

    private static DisplayRecipe display(Shell shell, Filling filling, Fuze fuze, Extra extra,
            int sourceOrder) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(stack(shellItem(shell)));
        inputs.add(stack(fillingItem(filling)));
        inputs.add(stack(fuzeItem(fuze)));
        if (extra != null) {
            inputs.add(stack(extraItem(extra)));
        }
        return new DisplayRecipe(inputs, UniversalGrenadeItem.make(shell, filling, fuze, extra),
                new ItemStack(net.minecraft.world.level.block.Blocks.CRAFTING_TABLE), sourceOrder);
    }

    private static List<ItemStack> stack(ItemLike item) {
        return List.of(new ItemStack(item));
    }

    private static ItemLike shellItem(Shell shell) {
        return switch (shell) {
            case FRAG -> ModItems.GRENADE_SHELL_FRAG.get();
            case STICK -> ModItems.GRENADE_SHELL_STICK.get();
            case TECH -> ModItems.GRENADE_SHELL_TECH.get();
            case NUKE -> ModItems.GRENADE_SHELL_NUKE.get();
        };
    }

    private static ItemLike fillingItem(Filling filling) {
        return switch (filling) {
            case POWDER -> ModItems.GRENADE_FILLING_POWDER.get();
            case HE -> ModItems.GRENADE_FILLING_HE.get();
            case DEMO -> ModItems.GRENADE_FILLING_DEMO.get();
            case INC -> ModItems.GRENADE_FILLING_INC.get();
            case WP -> ModItems.GRENADE_FILLING_WP.get();
            case CLUSTER -> ModItems.GRENADE_FILLING_CLUSTER.get();
            case EMP -> ModItems.GRENADE_FILLING_EMP.get();
            case PLASMA -> ModItems.GRENADE_FILLING_PLASMA.get();
            case LASER -> ModItems.GRENADE_FILLING_LASER.get();
            case CLUSTER_HEAVY -> ModItems.GRENADE_FILLING_CLUSTER_HEAVY.get();
            case NUCLEAR -> ModItems.GRENADE_FILLING_NUCLEAR.get();
            case NUCLEAR_DEMO -> ModItems.GRENADE_FILLING_NUCLEAR_DEMO.get();
            case SCHRAB -> ModItems.GRENADE_FILLING_SCHRAB.get();
        };
    }

    private static ItemLike fuzeItem(Fuze fuze) {
        return switch (fuze) {
            case S3 -> ModItems.GRENADE_FUZE_S3.get();
            case S7 -> ModItems.GRENADE_FUZE_S7.get();
            case S15 -> ModItems.GRENADE_FUZE_S15.get();
            case IMPACT -> ModItems.GRENADE_FUZE_IMPACT.get();
            case AIRBURST -> ModItems.GRENADE_FUZE_AIRBURST.get();
        };
    }

    private static ItemLike extraItem(Extra extra) {
        return switch (extra) {
            case GLUE -> ModItems.GRENADE_EXTRA_GLUE.get();
            case PROXY_FUZE -> ModItems.GRENADE_EXTRA_PROXY_FUZE.get();
            case FRAG_SLEEVE -> ModItems.GRENADE_EXTRA_FRAG_SLEEVE.get();
            case TRIPLEX -> ModItems.GRENADE_EXTRA_TRIPLEX.get();
        };
    }

    public record DisplayRecipe(List<List<ItemStack>> inputs, ItemStack output, ItemStack catalyst,
                                int sourceOrder) {
        public DisplayRecipe {
            inputs = inputs.stream()
                    .map(stacks -> stacks.stream().map(ItemStack::copy).toList())
                    .toList();
            output = output.copy();
            catalyst = catalyst.copy();
        }
    }
}
