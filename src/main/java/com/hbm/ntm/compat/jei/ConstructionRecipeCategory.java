package com.hbm.ntm.compat.jei;

import com.hbm.ntm.blockentity.FusionTorusStructCoreBlockEntity;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmItemStackUtil;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ConstructionRecipeCategory
        implements HbmJeiRecipeCategory<ConstructionRecipeCategory.DisplayRecipe> {
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

    ConstructionRecipeCategory(RecipeType<DisplayRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
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
        return Component.literal("Construction");
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
    public IDrawable getRecipeBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DisplayRecipe recipe, IFocusGroup focuses) {
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, recipe.inputs());
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, List.of(List.of(recipe.output())));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, recipe.machine());
    }

    static List<DisplayRecipe> recipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();

        recipes.add(new DisplayRecipe(
                List.of(
                        stack(ModBlocks.WATZ_END.get(), 48),
                        tagStacks(DURA_BOLTS, 64),
                        tagStacks(DURA_BOLTS, 64),
                        tagStacks(DURA_BOLTS, 64),
                        stack(ModBlocks.WATZ_ELEMENT.get(), 36),
                        stack(ModBlocks.WATZ_COOLER.get(), 26),
                        stack(ModItems.BOLTGUN.get())),
                new ItemStack(ModBlocks.WATZ.get()),
                new ItemStack(ModBlocks.STRUCT_WATZ_CORE.get()),
                0));

        recipes.add(new DisplayRecipe(
                List.of(stack(ModBlocks.STRUCT_LAUNCHER.get(), 8)),
                new ItemStack(ModBlocks.COMPACT_LAUNCHER.get()),
                new ItemStack(ModBlocks.STRUCT_LAUNCHER_CORE.get()),
                1));

        recipes.add(new DisplayRecipe(
                List.of(
                        stack(ModBlocks.STRUCT_LAUNCHER.get(), 16),
                        stack(ModBlocks.STRUCT_LAUNCHER.get(), 64),
                        stack(ModBlocks.STRUCT_SCAFFOLD.get(), 11)),
                new ItemStack(ModBlocks.LAUNCH_TABLE.get()),
                new ItemStack(ModBlocks.STRUCT_LAUNCHER_CORE_LARGE.get()),
                2));

        recipes.add(new DisplayRecipe(
                List.of(
                        stack(ModBlocks.ICF_COMPONENT_SCAFFOLD.get(), 50),
                        labeledStack(ModBlocks.ICF_COMPONENT_STRUCTURE_BOLTED.get(), 240),
                        tagStacks(DURA_BOLTS, 960),
                        tagStacks(STEEL_CAST_PLATES, 240),
                        labeledStack(ModBlocks.ICF_COMPONENT_VESSEL_WELDED.get(), 117),
                        tagStacks(BISMOID_BRONZE_CAST_PLATES, 117),
                        stack(ModItems.BLOWTORCH.get()),
                        stack(ModItems.BOLTGUN.get())),
                new ItemStack(ModBlocks.ICF.get()),
                new ItemStack(ModBlocks.STRUCT_ICF_CORE.get()),
                4));

        recipes.add(new DisplayRecipe(
                fusionTorusInputs(),
                new ItemStack(ModBlocks.FUSION_TORUS.get()),
                new ItemStack(ModBlocks.STRUCT_TORUS_CORE.get()),
                5));

        return List.copyOf(recipes);
    }

    private static List<List<ItemStack>> fusionTorusInputs() {
        int wallCount = 0;
        int blanketCount = 0;
        int pipeCount = -1;

        for (int y = 0; y < FusionTorusStructCoreBlockEntity.LEGACY_LAYOUT_HEIGHT; y++) {
            for (int x = 0; x < FusionTorusStructCoreBlockEntity.LEGACY_LAYOUT_SIZE; x++) {
                for (int z = 0; z < FusionTorusStructCoreBlockEntity.LEGACY_LAYOUT_SIZE; z++) {
                    int component = FusionTorusStructCoreBlockEntity.legacyLayoutComponent(x, y, z);
                    if (component == 1) {
                        wallCount++;
                    } else if (component == 2) {
                        blanketCount++;
                    } else if (component == 3) {
                        pipeCount++;
                    }
                }
            }
        }

        List<List<ItemStack>> inputs = new ArrayList<>();
        int plateCount = wallCount;
        addChunked(inputs, ModBlocks.FUSION_COMPONENT_BSCCO.get(), wallCount);
        addChunked(inputs, STEEL_CAST_PLATES, plateCount);
        addChunked(inputs, ModBlocks.FUSION_COMPONENT_BLANKET.get(), blanketCount);
        addChunked(inputs, ModBlocks.FUSION_COMPONENT_MOTOR.get(), pipeCount);
        inputs.add(stack(ModItems.BLOWTORCH.get()));
        return List.copyOf(inputs);
    }

    private static void addChunked(List<List<ItemStack>> inputs, ItemLike item, int count) {
        int remaining = count;
        while (remaining > 0) {
            int chunk = Math.min(remaining, 256);
            inputs.add(labeledStack(item, chunk));
            remaining -= chunk;
        }
    }

    private static void addChunked(List<List<ItemStack>> inputs, TagKey<Item> tag, int count) {
        int remaining = count;
        while (remaining > 0) {
            int chunk = Math.min(remaining, 256);
            inputs.add(tagStacks(tag, chunk));
            remaining -= chunk;
        }
    }

    private static List<ItemStack> tagStacks(TagKey<Item> tag, int count) {
        return labelStacks(HbmIngredient.of(tag, count).displayStacks());
    }

    private static List<ItemStack> stack(ItemLike item) {
        return stack(item, 1);
    }

    private static List<ItemStack> stack(ItemLike item, int count) {
        return List.of(new ItemStack(item, count));
    }

    private static List<ItemStack> labeledStack(ItemLike item, int count) {
        return List.of(HbmItemStackUtil.addStackSizeLabel(new ItemStack(item, count)));
    }

    private static List<ItemStack> labelStacks(List<ItemStack> stacks) {
        return stacks.stream()
                .map(ItemStack::copy)
                .map(HbmItemStackUtil::addStackSizeLabel)
                .toList();
    }

    public record DisplayRecipe(List<List<ItemStack>> inputs, ItemStack output, ItemStack machine,
                                int sourceOrder) {
        public DisplayRecipe {
            inputs = inputs.stream()
                    .map(stacks -> stacks.stream().map(ItemStack::copy).toList())
                    .toList();
            output = output.copy();
            machine = machine.copy();
        }
    }
}
