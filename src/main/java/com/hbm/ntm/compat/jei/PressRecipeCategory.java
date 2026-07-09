package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.recipe.PressRecipe;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class PressRecipeCategory implements IRecipeCategory<PressRecipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 65;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_press.png");

    private final RecipeType<PressRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableAnimated progress;

    PressRecipeCategory(RecipeType<PressRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 5, 11, WIDTH, HEIGHT);
        this.progress = guiHelper.createAnimatedDrawable(
                guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 0, 86, 18, 18), 20, StartDirection.TOP, false);
    }

    @Override
    public RecipeType<PressRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Press");
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PressRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> inputs = recipe.input().displayStacks();
        if (!inputs.isEmpty()) {
            builder.addInputSlot(48, 42)
                    .addItemStacks(inputs);
        }
        builder.addInputSlot(48, 6)
                .addItemStacks(stampStacks(recipe.getStampType()));
        if (Minecraft.getInstance().level != null) {
            builder.addOutputSlot(111, 24)
                    .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
        }
    }

    @Override
    public void draw(PressRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        progress.draw(guiGraphics, 47, 24);
    }

    private static List<ItemStack> stampStacks(ItemPressStamp.StampType stampType) {
        return switch (stampType) {
            case FLAT -> List.of(
                    new ItemStack(ModItems.STONE_FLAT_STAMP.get()),
                    new ItemStack(ModItems.IRON_FLAT_STAMP.get()),
                    new ItemStack(ModItems.STEEL_FLAT_STAMP.get()),
                    new ItemStack(ModItems.TITANIUM_FLAT_STAMP.get()),
                    new ItemStack(ModItems.OBSIDIAN_FLAT_STAMP.get()),
                    new ItemStack(ModItems.DESH_FLAT_STAMP.get()));
            case PLATE -> List.of(
                    new ItemStack(ModItems.STONE_PLATE_STAMP.get()),
                    new ItemStack(ModItems.IRON_PLATE_STAMP.get()),
                    new ItemStack(ModItems.STEEL_PLATE_STAMP.get()),
                    new ItemStack(ModItems.TITANIUM_PLATE_STAMP.get()),
                    new ItemStack(ModItems.OBSIDIAN_PLATE_STAMP.get()),
                    new ItemStack(ModItems.DESH_PLATE_STAMP.get()));
            case WIRE -> List.of(
                    new ItemStack(ModItems.STONE_WIRE_STAMP.get()),
                    new ItemStack(ModItems.IRON_WIRE_STAMP.get()),
                    new ItemStack(ModItems.STEEL_WIRE_STAMP.get()),
                    new ItemStack(ModItems.TITANIUM_WIRE_STAMP.get()),
                    new ItemStack(ModItems.OBSIDIAN_WIRE_STAMP.get()),
                    new ItemStack(ModItems.DESH_WIRE_STAMP.get()));
            case CIRCUIT -> List.of(
                    new ItemStack(ModItems.STONE_CIRCUIT_STAMP.get()),
                    new ItemStack(ModItems.IRON_CIRCUIT_STAMP.get()),
                    new ItemStack(ModItems.STEEL_CIRCUIT_STAMP.get()),
                    new ItemStack(ModItems.TITANIUM_CIRCUIT_STAMP.get()),
                    new ItemStack(ModItems.OBSIDIAN_CIRCUIT_STAMP.get()),
                    new ItemStack(ModItems.DESH_CIRCUIT_STAMP.get()));
            case C357 -> List.of(new ItemStack(ModItems.STAMP_357.get()),
                    new ItemStack(ModItems.DESH_STAMP_357.get()));
            case C44 -> List.of(new ItemStack(ModItems.STAMP_44.get()),
                    new ItemStack(ModItems.DESH_STAMP_44.get()));
            case C50 -> List.of(new ItemStack(ModItems.STAMP_50.get()),
                    new ItemStack(ModItems.DESH_STAMP_50.get()));
            case C9 -> List.of(new ItemStack(ModItems.STAMP_9.get()),
                    new ItemStack(ModItems.DESH_STAMP_9.get()));
            case PRINTING1 -> List.of(new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(0).get()));
            case PRINTING2 -> List.of(new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(1).get()));
            case PRINTING3 -> List.of(new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(2).get()));
            case PRINTING4 -> List.of(new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(3).get()));
            case PRINTING5 -> List.of(new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(4).get()));
            case PRINTING6 -> List.of(new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(5).get()));
            case PRINTING7 -> List.of(new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(6).get()));
            case PRINTING8 -> List.of(new ItemStack(ModItems.STAMP_BOOK_ITEMS.get(7).get()));
        };
    }
}
