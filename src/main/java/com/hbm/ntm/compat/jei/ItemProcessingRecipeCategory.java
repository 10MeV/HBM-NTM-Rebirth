package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.FluidIconItem;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ItemProcessingRecipeCategory implements IRecipeCategory<ItemProcessingRecipe> {
    private static final int UNIVERSAL_WIDTH = 166;
    private static final int UNIVERSAL_HEIGHT = 65;
    private static final int SHREDDER_WIDTH = 176;
    private static final int SHREDDER_HEIGHT = 85;
    private static final ResourceLocation UNIVERSAL_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei.png");
    private static final ResourceLocation SHREDDER_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_shredder.png");

    private final RecipeType<ItemProcessingRecipe> type;
    private final ItemProcessingRecipe.Machine machine;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final IDrawableAnimated shredderPower;
    private final IDrawableAnimated shredderProgress;
    private final ItemStack catalyst;
    private final int width;
    private final int height;

    ItemProcessingRecipeCategory(RecipeType<ItemProcessingRecipe> type, ItemProcessingRecipe.Machine machine,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.machine = machine;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.catalyst = new ItemStack(catalyst);
        if (machine == ItemProcessingRecipe.Machine.SHREDDER) {
            this.width = SHREDDER_WIDTH;
            this.height = SHREDDER_HEIGHT;
            this.background = guiHelper.createDrawable(SHREDDER_NEI_TEXTURE, 0, 0, SHREDDER_WIDTH, SHREDDER_HEIGHT);
            this.slotBackground = null;
            this.machineBackground = null;
            this.shredderPower = guiHelper.createAnimatedDrawable(
                    guiHelper.createDrawable(SHREDDER_NEI_TEXTURE, 36, 86, 16, 52),
                    480, StartDirection.BOTTOM, false);
            this.shredderProgress = guiHelper.createAnimatedDrawable(
                    guiHelper.createDrawable(SHREDDER_NEI_TEXTURE, 100, 118, 24, 16),
                    48, StartDirection.LEFT, false);
        } else {
            this.width = UNIVERSAL_WIDTH;
            this.height = UNIVERSAL_HEIGHT;
            this.background = guiHelper.createDrawable(UNIVERSAL_NEI_TEXTURE, 5, 11, UNIVERSAL_WIDTH, UNIVERSAL_HEIGHT);
            this.slotBackground = guiHelper.createDrawable(UNIVERSAL_NEI_TEXTURE, 5, 87, 18, 18);
            this.machineBackground = guiHelper.createDrawable(UNIVERSAL_NEI_TEXTURE, 59, 87, 18, 36);
            this.shredderPower = null;
            this.shredderProgress = null;
        }
    }

    @Override
    public RecipeType<ItemProcessingRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return switch (machine) {
            case SHREDDER -> Component.literal("Shredder");
            case CENTRIFUGE -> Component.literal("Centrifuge");
            case CRYSTALLIZER -> Component.literal("Acidizer");
        };
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
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
    public void setRecipe(IRecipeLayoutBuilder builder, ItemProcessingRecipe recipe, IFocusGroup focuses) {
        if (machine == ItemProcessingRecipe.Machine.SHREDDER) {
            setShredderRecipe(builder, recipe);
            return;
        }

        setUniversalRecipe(builder, recipe);
    }

    private void setShredderRecipe(IRecipeLayoutBuilder builder, ItemProcessingRecipe recipe) {
        builder.addInputSlot(39, 24)
                .addItemStacks(recipe.input().displayStacks());

        List<ItemStack> output = recipe.outputs().isEmpty()
                ? List.of()
                : recipe.outputs().get(0).displayStacks();
        if (!output.isEmpty()) {
            builder.addOutputSlot(129, 24)
                    .addItemStacks(output);
        }

        List<ItemStack> blades = shredderBlades();
        builder.addSlot(RecipeIngredientRole.CATALYST, 84, 6)
                .addItemStacks(blades);
        builder.addSlot(RecipeIngredientRole.CATALYST, 84, 42)
                .addItemStacks(blades);
    }

    private void setUniversalRecipe(IRecipeLayoutBuilder builder, ItemProcessingRecipe recipe) {
        List<List<ItemStack>> inputs = universalInputs(recipe);
        int[][] inputCoords = inputCoords(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            int[] pos = inputCoords[i];
            List<ItemStack> stacks = inputs.get(i);
            if (!stacks.isEmpty()) {
                builder.addInputSlot(pos[0], pos[1])
                        .setBackground(slotBackground, -1, -1)
                        .addItemStacks(stacks);
            }
        }

        List<HbmItemOutput> outputs = recipe.outputs();
        int[][] outputCoords = outputCoords(outputs.size());
        for (int i = 0; i < outputs.size(); i++) {
            List<ItemStack> stacks = outputs.get(i).displayStacks();
            if (!stacks.isEmpty()) {
                int[] pos = outputCoords[i];
                builder.addOutputSlot(pos[0], pos[1])
                        .setBackground(slotBackground, -1, -1)
                        .addItemStacks(stacks);
            }
        }

        builder.addSlot(RecipeIngredientRole.CATALYST, 75, 31)
                .setBackground(machineBackground, -1, -17)
                .addItemStack(catalyst);
    }

    private List<List<ItemStack>> universalInputs(ItemProcessingRecipe recipe) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        if (machine == ItemProcessingRecipe.Machine.CRYSTALLIZER) {
            recipe.fluidInput().ifPresent(input -> inputs.add(List.of(FluidIconItem.make(
                    input.type(), input.amount(), input.pressure()))));
        }
        inputs.add(recipe.input().displayStacks());
        return inputs;
    }

    @Override
    public void draw(ItemProcessingRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (machine == ItemProcessingRecipe.Machine.SHREDDER) {
            shredderPower.draw(guiGraphics, 3, 6);
            shredderProgress.draw(guiGraphics, 80, 23);
        } else if (machine == ItemProcessingRecipe.Machine.CRYSTALLIZER && recipe.productivity() > 0.0F) {
            int effectiveness = Math.min((int) (recipe.productivity() * 100), 99);
            guiGraphics.drawString(Minecraft.getInstance().font,
                    "Effectiveness: +" + effectiveness + "% per level", 8, 52, 0x404040, false);
        }
    }

    private static List<ItemStack> shredderBlades() {
        return List.of(
                new ItemStack(ModItems.SHREDDER_BLADES_STEEL.get()),
                new ItemStack(ModItems.SHREDDER_BLADES_TITANIUM.get()),
                new ItemStack(ModItems.SHREDDER_BLADES_DESH.get()));
    }

    private static int[][] inputCoords(int count) {
        return switch (count) {
            case 1 -> new int[][] {{48, 24}};
            case 2 -> new int[][] {{48, 24}, {30, 24}};
            case 3 -> new int[][] {{48, 24}, {30, 24}, {12, 24}};
            case 4 -> new int[][] {{48, 15}, {30, 15}, {48, 33}, {30, 33}};
            case 5 -> new int[][] {{48, 15}, {30, 15}, {12, 24}, {48, 33}, {30, 33}};
            case 6 -> new int[][] {{48, 15}, {30, 15}, {12, 15}, {48, 33}, {30, 33}, {12, 33}};
            case 7 -> new int[][] {{48, 6}, {30, 15}, {12, 15}, {48, 24}, {30, 33}, {12, 33}, {48, 42}};
            case 8 -> new int[][] {
                    {48, 6}, {30, 6}, {12, 15}, {48, 24}, {30, 24}, {12, 33}, {48, 42}, {30, 42}
            };
            case 9 -> new int[][] {
                    {48, 6}, {30, 6}, {12, 6}, {48, 24}, {30, 24}, {12, 24}, {48, 42}, {30, 42}, {12, 42}
            };
            default -> generatedCoords(count);
        };
    }

    private static int[][] outputCoords(int count) {
        return switch (count) {
            case 1 -> new int[][] {{102, 24}};
            case 2 -> new int[][] {{102, 24}, {120, 24}};
            case 3 -> new int[][] {{102, 24}, {120, 24}, {138, 24}};
            case 4 -> new int[][] {{102, 15}, {120, 15}, {102, 33}, {120, 33}};
            case 5 -> new int[][] {{102, 15}, {120, 15}, {102, 33}, {120, 33}, {138, 24}};
            case 6 -> new int[][] {{102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}};
            case 7 -> new int[][] {{102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}, {138, 24}};
            case 8 -> new int[][] {
                    {102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}, {138, 24}, {138, 42}
            };
            default -> generatedCoords(count);
        };
    }

    private static int[][] generatedCoords(int count) {
        int[][] slots = new int[count][2];
        for (int i = 0; i < count; i++) {
            slots[i] = new int[] {i % 4 * 18, i / 4 * 18};
        }
        return slots;
    }
}
