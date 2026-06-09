package com.hbm.ntm.compat.jei;

import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.recipe.PressRecipe;
import com.hbm.ntm.registry.ModItems;
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

public final class PressRecipeCategory implements IRecipeCategory<PressRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 54;

    private final RecipeType<PressRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    PressRecipeCategory(RecipeType<PressRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<PressRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_press", "Press");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PressRecipe recipe, IFocusGroup focuses) {
        if (!recipe.getIngredients().isEmpty()) {
            builder.addInputSlot(4, 18)
                    .addItemStacks(List.of(recipe.getIngredients().get(0).getItems()))
                    .setStandardSlotBackground();
        }
        builder.addInputSlot(40, 18)
                .addItemStack(stampStack(recipe.getStampType()))
                .setStandardSlotBackground();
        if (Minecraft.getInstance().level != null) {
            builder.addOutputSlot(130, 18)
                    .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()))
                    .setOutputSlotBackground();
        }
    }

    @Override
    public void draw(PressRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 82, 18);
    }

    private static ItemStack stampStack(ItemPressStamp.StampType stampType) {
        return switch (stampType) {
            case FLAT -> new ItemStack(ModItems.IRON_FLAT_STAMP.get());
            case PLATE -> new ItemStack(ModItems.IRON_PLATE_STAMP.get());
            case WIRE -> new ItemStack(ModItems.IRON_WIRE_STAMP.get());
            case CIRCUIT -> new ItemStack(ModItems.IRON_CIRCUIT_STAMP.get());
            case C357 -> new ItemStack(ModItems.STAMP_357.get());
            case C44 -> new ItemStack(ModItems.STAMP_44.get());
            case C50 -> new ItemStack(ModItems.STAMP_50.get());
            case C9 -> new ItemStack(ModItems.STAMP_9.get());
            case PRINTING1 -> new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(0).get());
            case PRINTING2 -> new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(1).get());
            case PRINTING3 -> new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(2).get());
            case PRINTING4 -> new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(3).get());
            case PRINTING5 -> new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(4).get());
            case PRINTING6 -> new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(5).get());
            case PRINTING7 -> new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(6).get());
            case PRINTING8 -> new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(7).get());
        };
    }
}
