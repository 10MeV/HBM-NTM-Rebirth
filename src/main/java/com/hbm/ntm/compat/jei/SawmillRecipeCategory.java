package com.hbm.ntm.compat.jei;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmItemStackUtil;
import java.util.Arrays;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class SawmillRecipeCategory implements IRecipeCategory<SawmillJeiRecipe> {
    private static final TagKey<Item> WOODEN_RODS =
            ItemTags.create(new ResourceLocation("forge", "rods/wooden"));

    private final RecipeType<SawmillJeiRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    SawmillRecipeCategory(RecipeType<SawmillJeiRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<SawmillJeiRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_sawmill", "Sawmill");
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
    public void setRecipe(IRecipeLayoutBuilder builder, SawmillJeiRecipe recipe, IFocusGroup focuses) {
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, List.of(recipe.inputs()));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, outputs(recipe));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }

    private static List<List<ItemStack>> outputs(SawmillJeiRecipe recipe) {
        if (recipe.bonusOutput() != null && !recipe.bonusOutput().isEmpty()) {
            return List.of(List.of(recipe.output()), List.of(recipe.bonusOutput()));
        }
        return List.of(List.of(recipe.output()));
    }

    static List<SawmillJeiRecipe> recipes() {
        return List.of(
                new SawmillJeiRecipe(tagStacks(ItemTags.LOGS), new ItemStack(Items.OAK_PLANKS, 6),
                        bonusSawdust("50%"), "50%"),
                new SawmillJeiRecipe(tagStacks(ItemTags.PLANKS), new ItemStack(Items.STICK, 6),
                        bonusSawdust("10%"), "10%"),
                new SawmillJeiRecipe(tagStacks(WOODEN_RODS), new ItemStack(ModItems.POWDER_SAWDUST.get()),
                        null, null),
                new SawmillJeiRecipe(tagStacks(ItemTags.SAPLINGS), new ItemStack(Items.STICK),
                        bonusSawdust("10%"), "10%"));
    }

    private static List<ItemStack> tagStacks(TagKey<Item> tag) {
        return Arrays.stream(Ingredient.of(tag).getItems())
                .map(ItemStack::copy)
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    private static ItemStack bonusSawdust(String chance) {
        return HbmItemStackUtil.addTooltipToStack(new ItemStack(ModItems.POWDER_SAWDUST.get()),
                ChatFormatting.RED + chance);
    }
}
