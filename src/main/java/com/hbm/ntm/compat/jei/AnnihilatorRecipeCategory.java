package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.FluidIconItem;
import com.hbm.ntm.recipe.AnnihilatorRecipe;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmItemStackUtil;
import com.hbm.ntm.util.HbmRegistryUtil;
import com.hbm.ntm.world.saveddata.AnnihilatorSavedData;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

public final class AnnihilatorRecipeCategory implements IRecipeCategory<AnnihilatorRecipeCategory.DisplayRecipe> {
    private final RecipeType<DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    AnnihilatorRecipeCategory(RecipeType<DisplayRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Annihilator");
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
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, List.of(recipe.inputs()));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, List.of(List.of(recipe.output())));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }

    static List<DisplayRecipe> recipes(RecipeManager recipeManager) {
        List<DisplayRecipe> displays = new ArrayList<>();
        List<AnnihilatorRecipe> recipes = recipeManager.getAllRecipesFor(ModRecipes.ANNIHILATOR.type().get())
                .stream()
                .sorted(Comparator.comparingInt(AnnihilatorRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .toList();
        for (AnnihilatorRecipe recipe : recipes) {
            List<ItemStack> inputs = inputStacks(recipe.key());
            if (inputs.isEmpty() || containsLegacySecretItem(inputs)) {
                continue;
            }
            int milestoneIndex = 0;
            for (AnnihilatorRecipe.Milestone milestone : recipe.milestones()) {
                ItemStack output = milestone.payout();
                if (isLegacySecretItem(output)) {
                    milestoneIndex++;
                    continue;
                }
                displays.add(new DisplayRecipe(recipe.getId(), recipe.sourceOrder(), milestoneIndex,
                        withAmountLore(inputs, milestone.amount()), output, milestone.amount()));
                milestoneIndex++;
            }
        }
        return List.copyOf(displays);
    }

    private static List<ItemStack> inputStacks(AnnihilatorSavedData.PoolKey key) {
        return switch (key.kind()) {
            case ITEM -> itemStack(key.item(), 0);
            case ITEM_META -> LegacyMetaItemMappings.stack(key.item(), key.meta(), 1)
                    .map(stack -> List.of(stack))
                    .orElseGet(() -> itemStack(key.item(), key.meta()));
            case FLUID -> fluidStack(key.fluid());
            case ORE_DICT -> oreDictStacks(key.oreDict());
            case UNKNOWN -> List.of();
        };
    }

    private static List<ItemStack> itemStack(ResourceLocation id, int meta) {
        Optional<Item> item = HbmRegistryUtil.item(id);
        if (item.isEmpty() && HbmNtm.MOD_ID.equals(id.getNamespace())) {
            RegistryObject<Item> legacyItem = ModItems.legacyItem(id.getPath());
            if (legacyItem != null) {
                item = Optional.of(legacyItem.get());
            }
        }
        if (item.isEmpty()) {
            return List.of();
        }
        ItemStack stack = new ItemStack(item.get());
        if (meta > 0) {
            stack.setDamageValue(meta);
        }
        return List.of(stack);
    }

    private static List<ItemStack> fluidStack(String fluidName) {
        FluidType type = HbmFluids.fromName(fluidName);
        if (type == HbmFluids.NONE) {
            return List.of();
        }
        return List.of(FluidIconItem.make(type, 0));
    }

    private static List<ItemStack> oreDictStacks(String legacyOreName) {
        return Arrays.stream(Ingredient.of(LegacyOreDictionaryMappings.itemTag(legacyOreName)).getItems())
                .map(ItemStack::copy)
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    private static List<ItemStack> withAmountLore(List<ItemStack> stacks, BigInteger amount) {
        String label = ChatFormatting.RED + String.format(Locale.US, "%,d", amount);
        return stacks.stream()
                .map(ItemStack::copy)
                .map(stack -> HbmItemStackUtil.addTooltipToStack(stack, label))
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    private static boolean containsLegacySecretItem(List<ItemStack> stacks) {
        return stacks.stream().anyMatch(AnnihilatorRecipeCategory::isLegacySecretItem);
    }

    private static boolean isLegacySecretItem(ItemStack stack) {
        return LegacyMetaItemMappings.legacyIdentity(stack)
                .map(identity -> LegacyMetaItemMappings.ITEM_SECRET.equals(identity.legacyId()))
                .orElse(false);
    }

    public record DisplayRecipe(ResourceLocation sourceId, int sourceOrder, int milestoneIndex,
                                List<ItemStack> inputs, ItemStack output, BigInteger amount) {
        public DisplayRecipe {
            inputs = inputs == null ? List.of() : inputs.stream()
                    .map(ItemStack::copy)
                    .filter(stack -> !stack.isEmpty())
                    .toList();
            output = output == null ? ItemStack.EMPTY : output.copy();
            amount = amount == null ? BigInteger.ZERO : amount;
        }

        @Override
        public List<ItemStack> inputs() {
            return inputs.stream().map(ItemStack::copy).toList();
        }

        @Override
        public ItemStack output() {
            return output.copy();
        }
    }
}
