package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlocks;
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

public class FusionFluidBreederRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmFluidStack input;
    private final HbmFluidStack output;

    public FusionFluidBreederRecipe(ResourceLocation id, HbmFluidStack input, HbmFluidStack output) {
        if (input.isEmpty() || output.isEmpty()) {
            throw new IllegalArgumentException("Fusion fluid breeder recipe requires non-empty input and output");
        }
        this.id = id;
        this.input = input;
        this.output = output;
    }

    public HbmFluidStack input() {
        return input;
    }

    public HbmFluidStack output() {
        return output;
    }

    public boolean matches(FluidType type, int fill) {
        return type == input.type() && fill >= input.amount();
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
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
        return NonNullList.create();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.FUSION_BREEDER.get());
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
        return ModRecipes.FUSION_FLUID_BREEDER.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FUSION_FLUID_BREEDER.type().get();
    }

    public static class Serializer implements RecipeSerializer<FusionFluidBreederRecipe> {
        @Override
        public FusionFluidBreederRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmFluidStack input = readFluidStack(GsonHelper.getAsJsonObject(json, "input"));
            HbmFluidStack output = readFluidStack(GsonHelper.getAsJsonObject(json, "output"));
            if (input.isEmpty() || output.isEmpty()) {
                throw new JsonSyntaxException("Invalid fusion fluid breeder recipe " + id);
            }
            return new FusionFluidBreederRecipe(id, input, output);
        }

        @Nullable
        @Override
        public FusionFluidBreederRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new FusionFluidBreederRecipe(id, readFluidStack(buffer), readFluidStack(buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, FusionFluidBreederRecipe recipe) {
            writeFluidStack(buffer, recipe.input);
            writeFluidStack(buffer, recipe.output);
        }

        private static HbmFluidStack readFluidStack(JsonObject object) {
            FluidType fluid = HbmFluids.fromName(normalizeFluidName(GsonHelper.getAsString(object, "fluid")));
            int amount = GsonHelper.getAsInt(object, "amount");
            int pressure = GsonHelper.getAsInt(object, "pressure", 0);
            return new HbmFluidStack(fluid, amount, pressure);
        }

        private static HbmFluidStack readFluidStack(FriendlyByteBuf buffer) {
            return new HbmFluidStack(HbmFluids.fromName(buffer.readUtf()), buffer.readVarInt(), buffer.readVarInt());
        }

        private static void writeFluidStack(FriendlyByteBuf buffer, HbmFluidStack stack) {
            buffer.writeUtf(stack.type().getName());
            buffer.writeVarInt(stack.amount());
            buffer.writeVarInt(stack.pressure());
        }

        private static String normalizeFluidName(String name) {
            ResourceLocation id = ResourceLocation.tryParse(name);
            return id == null || name.indexOf(':') < 0 ? name : id.getPath();
        }
    }
}
