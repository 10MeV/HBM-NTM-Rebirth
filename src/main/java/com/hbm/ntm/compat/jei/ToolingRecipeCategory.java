package com.hbm.ntm.compat.jei;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ToolingRecipeCategory implements IRecipeCategory<ToolingRecipeCategory.DisplayRecipe> {
    private static final TagKey<Item> DURA_BOLTS =
            ItemTags.create(new ResourceLocation("forge", "bolts/dura_steel"));
    private static final TagKey<Item> STEEL_CAST_PLATES =
            ItemTags.create(new ResourceLocation("forge", "cast_plates/steel"));
    private static final TagKey<Item> BISMOID_BRONZE_CAST_PLATES =
            ItemTags.create(new ResourceLocation("forge", "cast_plates/any_bismoid_bronze"));

    private final RecipeType<DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;

    ToolingRecipeCategory(RecipeType<DisplayRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
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
        return Component.literal("Tooling");
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
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, recipe.tools());
    }

    static List<DisplayRecipe> recipes() {
        return List.of(
                new DisplayRecipe(
                        List.of(tagStacks(DURA_BOLTS, 4), stack(ModBlocks.WATZ_END.get())),
                        watzEndRiveted(),
                        toolStacks(Toolable.ToolType.BOLT),
                        0),
                new DisplayRecipe(
                        List.of(tagStacks(STEEL_CAST_PLATES, 1), stack(ModBlocks.FUSION_COMPONENT_BSCCO.get())),
                        new ItemStack(ModBlocks.FUSION_COMPONENT_BSCCO_WELDED.get()),
                        toolStacks(Toolable.ToolType.TORCH),
                        1),
                new DisplayRecipe(
                        List.of(tagStacks(BISMOID_BRONZE_CAST_PLATES, 1), stack(ModBlocks.ICF_COMPONENT_VESSEL.get())),
                        new ItemStack(ModBlocks.ICF_COMPONENT_VESSEL_WELDED.get()),
                        toolStacks(Toolable.ToolType.TORCH),
                        2),
                new DisplayRecipe(
                        List.of(tagStacks(STEEL_CAST_PLATES, 1), tagStacks(DURA_BOLTS, 4),
                                stack(ModBlocks.ICF_COMPONENT_STRUCTURE.get())),
                        new ItemStack(ModBlocks.ICF_COMPONENT_STRUCTURE_BOLTED.get()),
                        toolStacks(Toolable.ToolType.BOLT),
                        3));
    }

    private static List<ItemStack> tagStacks(TagKey<Item> tag, int count) {
        return HbmIngredient.of(tag, count).displayStacks();
    }

    private static List<ItemStack> stack(ItemLike item) {
        return List.of(new ItemStack(item));
    }

    private static List<ItemStack> toolStacks(Toolable.ToolType tool) {
        List<ItemStack> registered = tool.stacksForDisplay().stream()
                .map(ItemStack::copy)
                .toList();
        if (!registered.isEmpty()) {
            return registered;
        }
        return switch (tool) {
            case TORCH -> List.of(new ItemStack(ModItems.BLOWTORCH.get()), new ItemStack(ModItems.ACETYLENE_TORCH.get()));
            case BOLT -> List.of(new ItemStack(ModItems.BOLTGUN.get()));
            default -> List.of();
        };
    }

    private static ItemStack watzEndRiveted() {
        ItemStack stack = new ItemStack(ModBlocks.WATZ_END.get());
        CompoundTag state = stack.getOrCreateTagElement("BlockStateTag");
        state.putString("riveted", "true");
        stack.setHoverName(Component.translatableWithFallback(
                "block.hbm_ntm_rebirth.watz_end_riveted",
                "Watz Reactor Stability Element (Riveted)"));
        return stack;
    }

    public record DisplayRecipe(List<List<ItemStack>> inputs, ItemStack output, List<ItemStack> tools,
                                int sourceOrder) {
        public DisplayRecipe {
            inputs = inputs.stream()
                    .map(stacks -> stacks.stream().map(ItemStack::copy).toList())
                    .toList();
            output = output.copy();
            tools = tools.stream().map(ItemStack::copy).toList();
        }
    }
}
