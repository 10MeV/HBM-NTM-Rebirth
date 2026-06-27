package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlocks;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CombinationOvenRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmIngredient input;
    private final Optional<HbmItemOutput> outputItem;
    private final Optional<HbmFluidStack> outputFluid;

    public CombinationOvenRecipe(ResourceLocation id, HbmIngredient input,
            Optional<HbmItemOutput> outputItem, Optional<HbmFluidStack> outputFluid) {
        this.id = id;
        this.input = input;
        this.outputItem = outputItem == null ? Optional.empty() : outputItem;
        this.outputFluid = outputFluid == null ? Optional.empty() : outputFluid;
        if (this.outputItem.isEmpty() && this.outputFluid.isEmpty()) {
            throw new IllegalArgumentException("Combination oven recipe must produce an item or fluid");
        }
    }

    public HbmIngredient input() {
        return input;
    }

    public Optional<HbmItemOutput> outputItem() {
        return outputItem;
    }

    public Optional<HbmFluidStack> outputFluid() {
        return outputFluid;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return outputItem.map(HbmItemOutput::representativeStack).orElse(ItemStack.EMPTY);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input.ingredient());
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.FURNACE_COMBINATION.get());
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.COMBINATION_OVEN.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.COMBINATION_OVEN.type().get();
    }

    public static class Serializer implements RecipeSerializer<CombinationOvenRecipe> {
        @Override
        public CombinationOvenRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient input = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            Optional<HbmItemOutput> outputItem = json.has("output_item")
                    ? Optional.of(HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output_item")))
                    : Optional.empty();
            Optional<HbmFluidStack> outputFluid = json.has("output_fluid")
                    ? Optional.of(readFluidStack(GsonHelper.getAsJsonObject(json, "output_fluid")))
                    : Optional.empty();
            return new CombinationOvenRecipe(id, input, outputItem, outputFluid);
        }

        @Nullable
        @Override
        public CombinationOvenRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient input = HbmIngredient.fromNetwork(buffer);
            Optional<HbmItemOutput> outputItem = buffer.readBoolean()
                    ? Optional.of(HbmItemOutput.fromNetwork(buffer))
                    : Optional.empty();
            Optional<HbmFluidStack> outputFluid = buffer.readBoolean()
                    ? Optional.of(readFluidStack(buffer))
                    : Optional.empty();
            return new CombinationOvenRecipe(id, input, outputItem, outputFluid);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CombinationOvenRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeBoolean(recipe.outputItem.isPresent());
            recipe.outputItem.ifPresent(output -> output.toNetwork(buffer));
            buffer.writeBoolean(recipe.outputFluid.isPresent());
            recipe.outputFluid.ifPresent(fluid -> writeFluidStack(buffer, fluid));
        }

        private static HbmFluidStack readFluidStack(JsonObject object) {
            return HbmFluidJsonUtil.readFluidStack(object, "combination oven fluid output");
        }

        private static HbmFluidStack readFluidStack(FriendlyByteBuf buffer) {
            return new HbmFluidStack(HbmFluids.fromName(buffer.readUtf()), buffer.readVarInt(), buffer.readVarInt());
        }

        private static void writeFluidStack(FriendlyByteBuf buffer, HbmFluidStack stack) {
            buffer.writeUtf(stack.type().getName());
            buffer.writeVarInt(stack.amount());
            buffer.writeVarInt(stack.pressure());
        }
    }
}
