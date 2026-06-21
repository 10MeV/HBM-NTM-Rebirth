package com.hbm.ntm.compat.jei;

import com.hbm.ntm.blockentity.GasCentBlockEntity;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

public final class GasCentRecipeCategory implements IRecipeCategory<GasCentJeiRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 82;

    private final RecipeType<GasCentJeiRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    GasCentRecipeCategory(RecipeType<GasCentJeiRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<GasCentJeiRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_gascent", "Gas Centrifuge");
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
    public void setRecipe(IRecipeLayoutBuilder builder, GasCentJeiRecipe recipe, IFocusGroup focuses) {
        addFluidSlot(builder, recipe.inputFluid(), true, 4, 28);
        for (int i = 0; i < recipe.outputs().size(); i++) {
            builder.addOutputSlot(118 + (i % 2) * 20, 14 + (i / 2) * 20)
                    .addItemStack(recipe.outputs().get(i))
                    .setOutputSlotBackground();
        }
    }

    @Override
    public void draw(GasCentJeiRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 72, 30);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, GasCentJeiRecipe recipe, IRecipeSlotsView recipeSlotsView,
            double mouseX, double mouseY) {
        if (mouseY >= 58) {
            tooltip.add(Component.literal(recipe.centrifuges() + " gas centrifuge"
                    + (recipe.centrifuges() == 1 ? "" : "s")));
            tooltip.add(Component.literal(recipe.inputType().legacyName() + " -> "
                    + recipe.outputType().legacyName()));
            if (recipe.highSpeed()) {
                tooltip.add(Component.literal("Requires gas centrifuge speed upgrade"));
            }
        }
    }

    static List<GasCentJeiRecipe> recipes() {
        return List.of(
                new GasCentJeiRecipe(new HbmFluidStack(HbmFluids.UF6, 1200),
                        stacks(stack("nugget_u238", 11), stack("nugget_u235", 1), stack("fluorite", 4)),
                        true, 4, GasCentBlockEntity.PseudoFluidType.NUF6,
                        GasCentBlockEntity.PseudoFluidType.NONE),
                new GasCentJeiRecipe(new HbmFluidStack(HbmFluids.UF6, 1200),
                        stacks(stack("nugget_u238", 6), stack("nugget_uranium_fuel", 6), stack("fluorite", 4)),
                        false, 2, GasCentBlockEntity.PseudoFluidType.LEUF6,
                        GasCentBlockEntity.PseudoFluidType.NONE),
                new GasCentJeiRecipe(new HbmFluidStack(HbmFluids.PUF6, 900),
                        stacks(stack("nugget_pu238", 3), stack("nugget_pu_mix", 6), stack("fluorite", 3)),
                        false, 1, GasCentBlockEntity.PseudoFluidType.PF6,
                        GasCentBlockEntity.PseudoFluidType.NONE),
                new GasCentJeiRecipe(new HbmFluidStack(HbmFluids.WATZ, 1000),
                        stacks(stack("powder_iron", 1), stack("powder_lead", 1),
                                stack("nuclear_waste_tiny", 1), stack("dust", 2)),
                        false, 2, GasCentBlockEntity.PseudoFluidType.MUD,
                        GasCentBlockEntity.PseudoFluidType.NONE));
    }

    private static void addFluidSlot(IRecipeLayoutBuilder builder, HbmFluidStack hbmStack,
            boolean input, int x, int y) {
        JeiFluidSlots.addFluidSlot(builder, hbmStack, input, x, y);
    }

    private static List<ItemStack> stacks(ItemStack... stacks) {
        return Arrays.stream(stacks)
                .filter(stack -> stack != null && !stack.isEmpty())
                .toList();
    }

    private static ItemStack stack(String legacyName, int count) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get(), count);
    }
}
