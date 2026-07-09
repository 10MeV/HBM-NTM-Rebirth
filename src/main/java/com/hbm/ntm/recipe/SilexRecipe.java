package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.LaserWavelength;
import com.hbm.ntm.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

public class SilexRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    @Nullable
    private final HbmIngredient itemSource;
    private final FluidType fluidSource;
    private final int fluidProduced;
    private final int fluidConsumed;
    private final LaserWavelength laserStrength;
    private final List<WeightedOutput> outputs;
    private final int sourceOrder;

    public SilexRecipe(ResourceLocation id, @Nullable HbmIngredient itemSource, FluidType fluidSource,
            int fluidProduced, int fluidConsumed, LaserWavelength laserStrength, List<WeightedOutput> outputs,
            int sourceOrder) {
        boolean hasItemSource = itemSource != null;
        boolean hasFluidSource = fluidSource != null && fluidSource != HbmFluids.NONE;
        if (hasItemSource == hasFluidSource) {
            throw new IllegalArgumentException("SILEX recipe must define exactly one source: " + id);
        }
        if (fluidProduced <= 0 || fluidConsumed <= 0) {
            throw new IllegalArgumentException("SILEX recipe fluid amounts must be positive: " + id);
        }
        if (laserStrength == null || laserStrength == LaserWavelength.NULL) {
            throw new IllegalArgumentException("SILEX recipe needs a real laser wavelength: " + id);
        }
        List<WeightedOutput> resolvedOutputs = outputs == null ? List.of() : outputs.stream()
                .filter(output -> output != null && !output.stack().isEmpty() && output.weight() > 0)
                .toList();
        if (resolvedOutputs.isEmpty()) {
            throw new IllegalArgumentException("SILEX recipe needs at least one weighted output: " + id);
        }
        this.id = id;
        this.itemSource = itemSource;
        this.fluidSource = hasFluidSource ? fluidSource : HbmFluids.NONE;
        this.fluidProduced = fluidProduced;
        this.fluidConsumed = fluidConsumed;
        this.laserStrength = laserStrength;
        this.outputs = List.copyOf(resolvedOutputs);
        this.sourceOrder = sourceOrder;
    }

    @Nullable
    public HbmIngredient itemSource() {
        return itemSource;
    }

    public FluidType fluidSource() {
        return fluidSource;
    }

    public boolean hasFluidSource() {
        return fluidSource != HbmFluids.NONE;
    }

    public int fluidProduced() {
        return fluidProduced;
    }

    public int fluidConsumed() {
        return fluidConsumed;
    }

    public LaserWavelength laserStrength() {
        return laserStrength;
    }

    public List<WeightedOutput> outputs() {
        return outputs;
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    public boolean matches(ItemStack stack) {
        return itemSource != null && itemSource.test(stack, true);
    }

    public boolean matchesFluid(FluidType fluid) {
        return fluidSource != HbmFluids.NONE && fluidSource == fluid;
    }

    public int totalWeight() {
        int total = 0;
        for (WeightedOutput output : outputs) {
            total += output.weight();
        }
        return Math.max(total, 1);
    }

    public ItemStack selectOutput(int index) {
        int normalized = Math.floorMod(index, totalWeight());
        int weight = 0;
        for (WeightedOutput output : outputs) {
            weight += output.weight();
            if (normalized < weight) {
                return output.stack();
            }
        }
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).stack();
    }

    @Override
    public boolean matches(Container container, Level level) {
        return !hasFluidSource() && !container.isEmpty() && matches(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return outputs.get(0).stack();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return outputs.get(0).stack();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        if (itemSource != null) {
            ingredients.add(itemSource.ingredient());
        }
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_SILEX.get());
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
        return ModRecipes.SILEX.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.SILEX.type().get();
    }

    public record WeightedOutput(ItemStack stack, int weight) {
        public WeightedOutput {
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
            weight = Math.max(0, weight);
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }

    public static class Serializer implements RecipeSerializer<SilexRecipe> {
        @Override
        public SilexRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonObject source = GsonHelper.getAsJsonObject(json, "source");
            HbmIngredient itemSource = null;
            FluidType fluidSource = HbmFluids.NONE;
            if (source.has("item")) {
                itemSource = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(source, "item"));
            }
            if (source.has("fluid")) {
                fluidSource = HbmFluidJsonUtil.requireFluidReference(source.get("fluid"), "SILEX source fluid");
            }
            int fluidProduced = GsonHelper.getAsInt(json, "fluid_produced");
            int fluidConsumed = GsonHelper.getAsInt(json, "fluid_consumed");
            LaserWavelength laserStrength = readLaserStrength(json, id);
            List<WeightedOutput> outputs = readOutputs(GsonHelper.getAsJsonArray(json, "outputs"), id);
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", Integer.MAX_VALUE);
            return new SilexRecipe(id, itemSource, fluidSource, fluidProduced, fluidConsumed, laserStrength, outputs,
                    sourceOrder);
        }

        @Override
        public @Nullable SilexRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient itemSource = buffer.readBoolean() ? HbmIngredient.fromNetwork(buffer) : null;
            FluidType fluidSource = buffer.readBoolean() ? HbmFluids.fromName(buffer.readUtf()) : HbmFluids.NONE;
            int fluidProduced = buffer.readVarInt();
            int fluidConsumed = buffer.readVarInt();
            LaserWavelength laserStrength = buffer.readEnum(LaserWavelength.class);
            List<WeightedOutput> outputs = buffer.readList(SilexRecipe.Serializer::readWeightedOutput);
            int sourceOrder = buffer.readVarInt();
            return new SilexRecipe(id, itemSource, fluidSource, fluidProduced, fluidConsumed, laserStrength, outputs,
                    sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SilexRecipe recipe) {
            buffer.writeBoolean(recipe.itemSource != null);
            if (recipe.itemSource != null) {
                recipe.itemSource.toNetwork(buffer);
            }
            buffer.writeBoolean(recipe.fluidSource != HbmFluids.NONE);
            if (recipe.fluidSource != HbmFluids.NONE) {
                buffer.writeUtf(recipe.fluidSource.getName());
            }
            buffer.writeVarInt(recipe.fluidProduced);
            buffer.writeVarInt(recipe.fluidConsumed);
            buffer.writeEnum(recipe.laserStrength);
            buffer.writeCollection(recipe.outputs, SilexRecipe.Serializer::writeWeightedOutput);
            buffer.writeVarInt(recipe.sourceOrder);
        }

        private static LaserWavelength readLaserStrength(JsonObject json, ResourceLocation id) {
            String name = GsonHelper.getAsString(json, "laser_strength");
            try {
                LaserWavelength wavelength = LaserWavelength.valueOf(name.trim().toUpperCase(Locale.ROOT));
                if (wavelength == LaserWavelength.NULL) {
                    throw new JsonSyntaxException("SILEX recipe cannot use NULL laser strength: " + id);
                }
                return wavelength;
            } catch (IllegalArgumentException exception) {
                throw new JsonSyntaxException("Unknown SILEX laser strength '" + name + "' in " + id, exception);
            }
        }

        private static List<WeightedOutput> readOutputs(JsonArray array, ResourceLocation id) {
            List<WeightedOutput> outputs = new ArrayList<>();
            for (JsonElement element : array) {
                JsonObject object = GsonHelper.convertToJsonObject(element, "SILEX output");
                ItemStack stack = HbmItemOutput.fromJson(object).representativeStack();
                int weight = GsonHelper.getAsInt(object, "weight");
                if (weight <= 0) {
                    throw new JsonSyntaxException("SILEX output weight must be positive in " + id);
                }
                outputs.add(new WeightedOutput(stack, weight));
            }
            return List.copyOf(outputs);
        }

        private static WeightedOutput readWeightedOutput(FriendlyByteBuf buffer) {
            return new WeightedOutput(buffer.readItem(), buffer.readVarInt());
        }

        private static void writeWeightedOutput(FriendlyByteBuf buffer, WeightedOutput output) {
            buffer.writeItem(output.stack);
            buffer.writeVarInt(output.weight);
        }
    }
}
