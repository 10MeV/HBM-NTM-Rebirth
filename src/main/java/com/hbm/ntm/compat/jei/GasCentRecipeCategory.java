package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.energy.HbmLegacyBatteryMaps;
import com.hbm.ntm.recipe.GasCentRecipe;
import com.hbm.ntm.recipe.GasCentRecipeRuntime;
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
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;

public final class GasCentRecipeCategory implements HbmJeiRecipeCategory<GasCentJeiRecipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 65;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_centrifuge_gas.png");

    private final RecipeType<GasCentJeiRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableAnimated powerBar;
    private final IDrawableAnimated progressNormal;
    private final IDrawableAnimated progressHighSpeed;
    private final IDrawableStatic highSpeedMarker;

    GasCentRecipeCategory(RecipeType<GasCentJeiRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 5, 11, WIDTH, HEIGHT);
        this.powerBar = guiHelper.createAnimatedDrawable(
                guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 176, 0, 16, 34), 480, StartDirection.BOTTOM, false);
        this.progressNormal = guiHelper.createAnimatedDrawable(
                guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 208, 0, 44, 37), 150, StartDirection.LEFT, false);
        this.progressHighSpeed = guiHelper.createAnimatedDrawable(
                guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 208, 0, 44, 37), 80, StartDirection.LEFT, false);
        this.highSpeedMarker = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 192, 0, 16, 16);
    }

    @Override
    public RecipeType<GasCentJeiRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Gas Centrifuge");
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
    public void setRecipe(IRecipeLayoutBuilder builder, GasCentJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(47, 24)
                .addItemStack(LegacyNeiUniversalLayout.fluidIcon(recipe.inputFluid()));
        builder.addInputSlot(3, 42)
                .addItemStacks(HbmLegacyBatteryMaps.legacyMachineRecipeBatteryDisplayStacks());
        List<ItemStack> outputs = outputsWithNothing(recipe.outputs());
        for (int i = 0; i < outputs.size(); i++) {
            builder.addOutputSlot(i % 2 == 0 ? 129 : 147, i < 2 ? 15 : 33)
                    .addItemStack(outputs.get(i));
        }
    }

    @Override
    public void draw(GasCentJeiRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawBackground(guiGraphics);
        powerBar.draw(guiGraphics, 3, 6);
        (recipe.highSpeed() ? progressHighSpeed : progressNormal).draw(guiGraphics, 74, 17);
        if (recipe.highSpeed()) {
            highSpeedMarker.draw(guiGraphics, 25, 24);
        }
        String centrifuges = centrifugeText(recipe);
        guiGraphics.drawString(Minecraft.getInstance().font, centrifuges,
                50 - Minecraft.getInstance().font.width(centrifuges) / 2, 10, 0x00FF00, false);
    }

    static List<GasCentJeiRecipe> recipes(RecipeManager recipeManager) {
        return GasCentRecipeRuntime.recipes(recipeManager).stream()
                .map(GasCentRecipeCategory::toJeiRecipe)
                .toList();
    }

    private static GasCentJeiRecipe toJeiRecipe(GasCentRecipe recipe) {
        return new GasCentJeiRecipe(recipe.input(), recipe.outputs(), recipe.highSpeed(), recipe.centrifuges(),
                recipe.inputType(), recipe.outputType());
    }

    private static String centrifugeText(GasCentJeiRecipe recipe) {
        return recipe.centrifuges() + " G. Cents";
    }

    private static List<ItemStack> outputsWithNothing(List<ItemStack> outputs) {
        return java.util.stream.IntStream.range(0, GasCentRecipe.MAX_OUTPUTS)
                .mapToObj(i -> i < outputs.size() ? outputs.get(i).copy() : new ItemStack(ModItems.NOTHING.get()))
                .toList();
    }
}
