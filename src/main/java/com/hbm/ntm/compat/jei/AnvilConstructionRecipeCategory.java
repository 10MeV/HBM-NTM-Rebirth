package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.NTMAnvilBlock;
import com.hbm.ntm.recipe.AnvilConstructionRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.util.HbmItemStackUtil;
import java.util.Comparator;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public final class AnvilConstructionRecipeCategory implements HbmJeiRecipeCategory<AnvilConstructionRecipe> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 86;
    private static final int TEXTURE_SIZE = 256;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_anvil.png");

    private final RecipeType<AnvilConstructionRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;

    AnvilConstructionRecipeCategory(RecipeType<AnvilConstructionRecipe> type, ItemLike catalyst,
            IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 0, 0, WIDTH, HEIGHT);
    }

    @Override
    public RecipeType<AnvilConstructionRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Anvil");
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
    public IDrawable getRecipeBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AnvilConstructionRecipe recipe, IFocusGroup focuses) {
        Layout layout = layout(recipe);
        for (int i = 0; i < recipe.inputs().size(); i++) {
            HbmIngredient input = recipe.inputs().get(i);
            List<ItemStack> stacks = input.displayStacks();
            if (!stacks.isEmpty()) {
                builder.addInputSlot(layout.inputX(i), layout.inputY(i))
                        .addItemStacks(stacks);
            }
        }

        int slot = 0;
        for (HbmItemOutput output : recipe.outputs()) {
            List<ItemStack> stacks = outputStacks(output);
            if (!stacks.isEmpty()) {
                builder.addOutputSlot(layout.outputX(slot), layout.outputY(slot))
                        .addItemStacks(stacks);
                slot++;
            }
        }

        List<ItemStack> anvils = anvilStacksFromTier(recipe.tierLower());
        if (!anvils.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, layout.anvilX(), 31)
                    .addItemStacks(anvils);
        }
    }

    @Override
    public void draw(AnvilConstructionRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawBackground(guiGraphics);
        switch (layout(recipe).shape()) {
            case NONE -> {
                blit(guiGraphics, 2, 5, 5, 87, 72, 54);
                blit(guiGraphics, 92, 5, 5, 87, 72, 54);
                blit(guiGraphics, 74, 14, 131, 96, 18, 36);
            }
            case SMITHING -> {
                blit(guiGraphics, 47, 23, 113, 105, 18, 18);
                blit(guiGraphics, 101, 23, 113, 105, 18, 18);
                blit(guiGraphics, 74, 14, 149, 96, 18, 36);
            }
            case CONSTRUCTION -> {
                blit(guiGraphics, 11, 5, 5, 87, 108, 54);
                blit(guiGraphics, 137, 23, 113, 105, 18, 18);
                blit(guiGraphics, 119, 14, 167, 96, 18, 36);
            }
            case RECYCLING -> {
                blit(guiGraphics, 11, 23, 113, 105, 18, 18);
                blit(guiGraphics, 47, 5, 5, 87, 108, 54);
                blit(guiGraphics, 29, 14, 185, 96, 18, 36);
            }
        }
    }

    static List<AnvilConstructionRecipe> recipes(RecipeManager recipeManager) {
        return recipeManager.getAllRecipesFor(ModRecipes.ANVIL_CONSTRUCTION.type().get()).stream()
                .sorted(Comparator.comparingInt(AnvilConstructionRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    private static void blit(net.minecraft.client.gui.GuiGraphics guiGraphics, int x, int y, int u, int v,
            int width, int height) {
        guiGraphics.blit(LEGACY_NEI_TEXTURE, x, y, u, v, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private static Layout layout(AnvilConstructionRecipe recipe) {
        int inputCount = recipe.inputs().size();
        int outputCount = recipe.outputs().size();
        if (inputCount == 1 && outputCount == 1) {
            return new Layout(Shape.SMITHING, 1, 1, 48, 24, 102, 24, 75);
        }
        if (inputCount == 1 && outputCount > 1) {
            return new Layout(Shape.RECYCLING, 1, 6, 12, 24, 48, 6, 30);
        }
        if (inputCount > 1 && outputCount == 1) {
            return new Layout(Shape.CONSTRUCTION, 6, 1, 12, 6, 138, 24, 120);
        }
        return new Layout(Shape.NONE, 4, 4, 3, 6, 93, 6, 75);
    }

    private static List<ItemStack> outputStacks(HbmItemOutput output) {
        return output.displayOptions().stream()
                .map(option -> outputStack(option.stack(), option.chance()))
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    private static ItemStack outputStack(ItemStack stack, float chance) {
        ItemStack copy = stack.copy();
        if (chance < 1.0F) {
            HbmItemStackUtil.addTooltipToStack(copy,
                    ChatFormatting.RED + legacyChancePercent(chance) + "%");
        }
        return copy;
    }

    private static String legacyChancePercent(float chance) {
        return Double.toString(((int) (chance * 1000.0F)) / 10.0D);
    }

    private static List<ItemStack> anvilStacksFromTier(int tier) {
        return anvilBlocks().stream()
                .map(RegistryObject::get)
                .filter(block -> block instanceof NTMAnvilBlock anvil && anvil.tier() == tier)
                .map(ItemStack::new)
                .toList();
    }

    private static List<RegistryObject<Block>> anvilBlocks() {
        return List.of(
                ModBlocks.ANVIL_IRON,
                ModBlocks.ANVIL_LEAD,
                ModBlocks.ANVIL_STEEL,
                ModBlocks.ANVIL_DESH,
                ModBlocks.ANVIL_FERROURANIUM,
                ModBlocks.ANVIL_SATURNITE,
                ModBlocks.ANVIL_BISMUTH_BRONZE,
                ModBlocks.ANVIL_ARSENIC_BRONZE,
                ModBlocks.ANVIL_SCHRABIDATE,
                ModBlocks.ANVIL_DNT,
                ModBlocks.ANVIL_OSMIRIDIUM,
                ModBlocks.ANVIL_MURKY);
    }

    private record Layout(Shape shape, int inputLine, int outputLine, int inputOriginX, int inputOriginY,
            int outputOriginX, int outputOriginY, int anvilX) {
        int inputX(int index) {
            return inputOriginX + 18 * (index % inputLine);
        }

        int inputY(int index) {
            return inputOriginY + 18 * (index / inputLine);
        }

        int outputX(int index) {
            return outputOriginX + 18 * (index % outputLine);
        }

        int outputY(int index) {
            return outputOriginY + 18 * (index / outputLine);
        }
    }

    private enum Shape {
        NONE,
        SMITHING,
        CONSTRUCTION,
        RECYCLING
    }
}
