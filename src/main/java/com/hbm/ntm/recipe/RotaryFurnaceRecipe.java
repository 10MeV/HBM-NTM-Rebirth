package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
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

public class RotaryFurnaceRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final MaterialStack output;
    private final int duration;
    private final int steam;
    @Nullable
    private final HbmFluidStack fluid;
    private final List<HbmIngredient> inputs;
    private final int sourceOrder;

    public RotaryFurnaceRecipe(ResourceLocation id, MaterialStack output, int duration, int steam,
            @Nullable HbmFluidStack fluid, List<HbmIngredient> inputs, int sourceOrder) {
        this.id = id;
        this.output = output == null ? null : output.copy();
        this.duration = Math.max(1, duration);
        this.steam = Math.max(0, steam);
        this.fluid = fluid == null || fluid.isEmpty() ? null : fluid;
        this.inputs = inputs == null ? List.of() : inputs.stream()
                .filter(input -> input != null)
                .toList();
        this.sourceOrder = sourceOrder;
        if (this.output == null || this.output.isEmpty()) {
            throw new IllegalArgumentException("Rotary furnace recipe must have a material output");
        }
        if (this.inputs.size() > 3) {
            throw new IllegalArgumentException("Rotary furnace recipe has too many item inputs: "
                    + this.inputs.size());
        }
    }

    public MaterialStack output() {
        return output.copy();
    }

    public int duration() {
        return duration;
    }

    public int steam() {
        return steam;
    }

    @Nullable
    public HbmFluidStack fluid() {
        return fluid;
    }

    public List<HbmIngredient> inputs() {
        return inputs;
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    public boolean matches(ItemStack first, ItemStack second, ItemStack third) {
        List<HbmIngredient> remaining = new ArrayList<>(inputs);
        for (ItemStack stack : List.of(first, second, third)) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            boolean found = false;
            for (HbmIngredient ingredient : List.copyOf(remaining)) {
                if (ingredient.test(stack)) {
                    remaining.remove(ingredient);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return remaining.isEmpty();
    }

    @Override
    public boolean matches(Container container, Level level) {
        return matches(container.getItem(0), container.getItem(1), container.getItem(2));
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
        for (HbmIngredient input : inputs) {
            ingredients.add(input.ingredient());
        }
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_ROTARY_FURNACE.get());
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
        return ModRecipes.ROTARY_FURNACE.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ROTARY_FURNACE.type().get();
    }

    public static class Serializer implements RecipeSerializer<RotaryFurnaceRecipe> {
        @Override
        public RotaryFurnaceRecipe fromJson(ResourceLocation id, JsonObject json) {
            MaterialStack output = MaterialStackJsonUtil.readRequired(GsonHelper.getNonNull(json, "output"),
                    "rotary furnace output");
            int duration = GsonHelper.getAsInt(json, "duration", 100);
            int steam = GsonHelper.getAsInt(json, "steam", 0);
            HbmFluidStack fluid = json.has("fluid")
                    ? HbmFluidJsonUtil.readFluidStack(GsonHelper.getAsJsonObject(json, "fluid"),
                            "rotary furnace fluid input")
                    : null;
            List<HbmIngredient> inputs = readInputs(json.getAsJsonArray("inputs"));
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", Integer.MAX_VALUE);
            return new RotaryFurnaceRecipe(id, output, duration, steam, fluid, inputs, sourceOrder);
        }

        @Nullable
        @Override
        public RotaryFurnaceRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            MaterialStack output = MaterialStackJsonUtil.readNetwork(buffer);
            int duration = buffer.readVarInt();
            int steam = buffer.readVarInt();
            HbmFluidStack fluid = buffer.readBoolean()
                    ? new HbmFluidStack(HbmFluids.fromName(buffer.readUtf()), buffer.readVarInt(),
                            buffer.readVarInt())
                    : null;
            int inputCount = buffer.readVarInt();
            List<HbmIngredient> inputs = new ArrayList<>();
            for (int i = 0; i < inputCount; i++) {
                inputs.add(HbmIngredient.fromNetwork(buffer));
            }
            int sourceOrder = buffer.readVarInt();
            return new RotaryFurnaceRecipe(id, output, duration, steam, fluid, inputs, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RotaryFurnaceRecipe recipe) {
            MaterialStackJsonUtil.writeNetwork(buffer, recipe.output);
            buffer.writeVarInt(recipe.duration);
            buffer.writeVarInt(recipe.steam);
            buffer.writeBoolean(recipe.fluid != null);
            if (recipe.fluid != null) {
                buffer.writeUtf(recipe.fluid.type().getName());
                buffer.writeVarInt(recipe.fluid.amount());
                buffer.writeVarInt(recipe.fluid.pressure());
            }
            buffer.writeVarInt(recipe.inputs.size());
            for (HbmIngredient input : recipe.inputs) {
                input.toNetwork(buffer);
            }
            buffer.writeVarInt(recipe.sourceOrder);
        }

        private static List<HbmIngredient> readInputs(@Nullable JsonArray array) {
            if (array == null) {
                return List.of();
            }
            List<HbmIngredient> inputs = new ArrayList<>();
            for (JsonElement element : array) {
                inputs.add(HbmIngredient.fromJson(element.getAsJsonObject()));
            }
            return List.copyOf(inputs);
        }
    }
}
