package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
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

public class LiquefactionRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmIngredient input;
    private final HbmFluidStack output;

    public LiquefactionRecipe(ResourceLocation id, HbmIngredient input, HbmFluidStack output) {
        this.id = id;
        this.input = input;
        this.output = output;
    }

    public HbmFluidStack getOutputFluid() {
        return output;
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
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input.ingredient());
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_LIQUEFACTOR.get());
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
        return ModRecipes.LIQUEFACTION.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.LIQUEFACTION.type().get();
    }

    public static class Serializer implements RecipeSerializer<LiquefactionRecipe> {
        @Override
        public LiquefactionRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient input = json.has("input")
                    ? HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"))
                    : new HbmIngredient(Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient")),
                            1, ItemStack.EMPTY, new CompoundTag(), null, -1, false, null, null, 0);
            JsonObject output = GsonHelper.getAsJsonObject(json, "output");
            return new LiquefactionRecipe(id, input,
                    HbmFluidJsonUtil.readFluidStack(output, "liquefaction output"));
        }

        @Nullable
        @Override
        public LiquefactionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient input = HbmIngredient.fromNetwork(buffer);
            HbmFluidStack output = new HbmFluidStack(HbmFluids.fromName(buffer.readUtf()), buffer.readVarInt(), buffer.readVarInt());
            return new LiquefactionRecipe(id, input, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LiquefactionRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeUtf(recipe.output.type().getName());
            buffer.writeVarInt(recipe.output.amount());
            buffer.writeVarInt(recipe.output.pressure());
        }
    }
}
