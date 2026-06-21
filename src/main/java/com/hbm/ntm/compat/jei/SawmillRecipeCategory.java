package com.hbm.ntm.compat.jei;

import com.hbm.ntm.registry.ModItems;
import java.util.Arrays;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
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
    private static final int WIDTH = 168;
    private static final int HEIGHT = 72;
    private static final TagKey<Item> WOODEN_RODS =
            ItemTags.create(new ResourceLocation("forge", "rods/wooden"));

    private final RecipeType<SawmillJeiRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    SawmillRecipeCategory(RecipeType<SawmillJeiRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
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
    public void setRecipe(IRecipeLayoutBuilder builder, SawmillJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(8, 26)
                .addItemStacks(recipe.inputs())
                .setStandardSlotBackground();
        builder.addOutputSlot(116, 18)
                .addItemStack(recipe.output())
                .setOutputSlotBackground();
        if (recipe.bonusOutput() != null && !recipe.bonusOutput().isEmpty()) {
            builder.addOutputSlot(138, 34)
                    .addItemStack(recipe.bonusOutput())
                    .setOutputSlotBackground();
        }
    }

    @Override
    public void draw(SawmillJeiRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 76, 28);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, SawmillJeiRecipe recipe, IRecipeSlotsView recipeSlotsView,
            double mouseX, double mouseY) {
        if (recipe.bonusChance() != null && mouseX >= 132 && mouseX <= 158 && mouseY >= 28 && mouseY <= 56) {
            tooltip.add(Component.literal(recipe.bonusChance() + " sawdust"));
        }
    }

    static List<SawmillJeiRecipe> recipes() {
        ItemStack sawdust = new ItemStack(ModItems.POWDER_SAWDUST.get());
        return List.of(
                new SawmillJeiRecipe(tagStacks(ItemTags.LOGS), new ItemStack(Items.OAK_PLANKS, 6),
                        sawdust, "50%"),
                new SawmillJeiRecipe(tagStacks(ItemTags.PLANKS), new ItemStack(Items.STICK, 6),
                        sawdust, "10%"),
                new SawmillJeiRecipe(tagStacks(WOODEN_RODS), sawdust, null, null),
                new SawmillJeiRecipe(tagStacks(ItemTags.SAPLINGS), new ItemStack(Items.STICK),
                        sawdust, "10%"));
    }

    private static List<ItemStack> tagStacks(TagKey<Item> tag) {
        return Arrays.stream(Ingredient.of(tag).getItems())
                .map(ItemStack::copy)
                .filter(stack -> !stack.isEmpty())
                .toList();
    }
}
