package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbm.ntm.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
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

public class BlastFurnaceRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final List<HbmIngredient> inputs;
    private final List<HbmItemOutput> outputs;
    private final int duration;

    public BlastFurnaceRecipe(ResourceLocation id, List<HbmIngredient> inputs, List<HbmItemOutput> outputs,
            int duration) {
        this.id = id;
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.duration = Math.max(1, duration);
        if (this.inputs.isEmpty() || this.inputs.size() > 2) {
            throw new IllegalArgumentException("Blast furnace recipe needs one or two item inputs");
        }
        if (this.outputs.isEmpty() || this.outputs.size() > 2) {
            throw new IllegalArgumentException("Blast furnace recipe needs one or two item outputs");
        }
    }

    public List<HbmIngredient> inputs() {
        return inputs;
    }

    public List<HbmItemOutput> outputs() {
        return outputs;
    }

    public int duration() {
        return duration;
    }

    public boolean matches(ItemStack first, ItemStack second) {
        if (inputs.size() == 1) {
            return (inputs.get(0).test(first) && second.isEmpty())
                    || (inputs.get(0).test(second) && first.isEmpty());
        }
        return inputs.get(0).test(first) && inputs.get(1).test(second)
                || inputs.get(0).test(second) && inputs.get(1).test(first);
    }

    public int consumedCountForSlot(int slot, ItemStack first, ItemStack second) {
        if (slot == 0) {
            return consumedCountFor(first, second);
        }
        return consumedCountFor(second, first);
    }

    private int consumedCountFor(ItemStack stack, ItemStack other) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (inputs.size() == 1) {
            return inputs.get(0).test(stack) ? inputs.get(0).count() : 0;
        }
        if (inputs.get(0).test(stack) && inputs.get(1).test(other)) {
            return inputs.get(0).count();
        }
        if (inputs.get(1).test(stack) && inputs.get(0).test(other)) {
            return inputs.get(1).count();
        }
        return 0;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return matches(container.getItem(0), container.getItem(1));
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
        return outputs.get(0).representativeStack();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (HbmIngredient input : inputs) {
            ingredients.add(input.ingredient());
        }
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_BLAST_FURNACE.get());
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
        return ModRecipes.BLAST_FURNACE.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.BLAST_FURNACE.type().get();
    }

    public static class Serializer implements RecipeSerializer<BlastFurnaceRecipe> {
        @Override
        public BlastFurnaceRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray inputArray = GsonHelper.getAsJsonArray(json, "inputs");
            List<HbmIngredient> inputs = new ArrayList<>();
            inputArray.forEach(element -> inputs.add(HbmIngredient.fromJson(
                    GsonHelper.convertToJsonObject(element, "blast furnace input"))));
            JsonArray outputArray = GsonHelper.getAsJsonArray(json, "outputs");
            List<HbmItemOutput> outputs = new ArrayList<>();
            outputArray.forEach(element -> outputs.add(HbmItemOutput.fromJson(
                    GsonHelper.convertToJsonObject(element, "blast furnace output"))));
            return new BlastFurnaceRecipe(id, inputs, outputs, GsonHelper.getAsInt(json, "duration", 800));
        }

        @Nullable
        @Override
        public BlastFurnaceRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            List<HbmIngredient> inputs = buffer.readList(HbmIngredient::fromNetwork);
            List<HbmItemOutput> outputs = buffer.readList(HbmItemOutput::fromNetwork);
            return new BlastFurnaceRecipe(id, inputs, outputs, buffer.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BlastFurnaceRecipe recipe) {
            buffer.writeCollection(recipe.inputs, (out, input) -> input.toNetwork(out));
            buffer.writeCollection(recipe.outputs, (out, output) -> output.toNetwork(out));
            buffer.writeVarInt(recipe.duration);
        }
    }
}
