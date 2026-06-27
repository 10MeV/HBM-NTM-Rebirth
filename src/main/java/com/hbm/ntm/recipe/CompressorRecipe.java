package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
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

public class CompressorRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmFluidStack input;
    private final HbmFluidStack output;
    private final int duration;

    public CompressorRecipe(ResourceLocation id, HbmFluidStack input, HbmFluidStack output, int duration) {
        this.id = id;
        this.input = input == null ? new HbmFluidStack(HbmFluids.NONE, 0) : input;
        this.output = output == null ? new HbmFluidStack(HbmFluids.NONE, 0) : output;
        this.duration = Math.max(1, duration);
        if (this.input.isEmpty()) {
            throw new IllegalArgumentException("Compressor recipe must have a fluid input");
        }
        if (this.output.isEmpty()) {
            throw new IllegalArgumentException("Compressor recipe must have a fluid output");
        }
    }

    public HbmFluidStack input() {
        return input;
    }

    public HbmFluidStack output() {
        return output;
    }

    public int duration() {
        return duration;
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
        return new ItemStack(ModBlocks.MACHINE_COMPRESSOR.get());
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
        return ModRecipes.COMPRESSOR.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.COMPRESSOR.type().get();
    }

    public static class Serializer implements RecipeSerializer<CompressorRecipe> {
        @Override
        public CompressorRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmFluidStack input = readFluidStack(GsonHelper.getNonNull(json, "input"), "compressor input");
            HbmFluidStack output = readFluidStack(GsonHelper.getNonNull(json, "output"), "compressor output");
            int duration = GsonHelper.getAsInt(json, "duration", 100);
            return new CompressorRecipe(id, input, output, duration);
        }

        @Nullable
        @Override
        public CompressorRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmFluidStack input = readFluidStack(buffer);
            HbmFluidStack output = readFluidStack(buffer);
            int duration = buffer.readVarInt();
            return new CompressorRecipe(id, input, output, duration);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CompressorRecipe recipe) {
            writeFluidStack(buffer, recipe.input);
            writeFluidStack(buffer, recipe.output);
            buffer.writeVarInt(recipe.duration);
        }

        private static HbmFluidStack readFluidStack(JsonElement element, String name) {
            if (element == null || element.isJsonNull()) {
                throw new JsonSyntaxException("Missing " + name);
            }
            if (element.isJsonObject()) {
                return HbmFluidJsonUtil.readFluidStack(element.getAsJsonObject(), name);
            }
            if (element.isJsonArray()) {
                return readLegacyFluidArray(element.getAsJsonArray(), name);
            }
            throw new JsonSyntaxException("Expected object or legacy array for " + name + ": " + element);
        }

        private static HbmFluidStack readLegacyFluidArray(JsonArray array, String name) {
            if (array.size() < 2) {
                throw new JsonSyntaxException("Legacy fluid array for " + name + " needs fluid and amount");
            }
            FluidType type = HbmFluidJsonUtil.requireFluidReference(array.get(0), name);
            HbmFluidStack stack = new HbmFluidStack(
                    type,
                    array.get(1).getAsInt(),
                    array.size() < 3 ? 0 : array.get(2).getAsInt());
            if (stack.isEmpty()) {
                throw new JsonSyntaxException("Invalid legacy fluid array for " + name + ": " + array);
            }
            return stack;
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
