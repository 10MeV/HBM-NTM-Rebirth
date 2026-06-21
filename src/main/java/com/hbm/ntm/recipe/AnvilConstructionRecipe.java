package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnvilConstructionRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final List<HbmIngredient> inputs;
    private final List<HbmItemOutput> outputs;
    private final int tierLower;
    private final int tierUpper;
    private final OverlayType overlay;

    public AnvilConstructionRecipe(ResourceLocation id, List<HbmIngredient> inputs, List<HbmItemOutput> outputs,
            int tierLower, int tierUpper, OverlayType overlay) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("Anvil construction recipe must have at least one input");
        }
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("Anvil construction recipe must have at least one output");
        }
        this.id = id;
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.tierLower = Math.max(0, tierLower);
        this.tierUpper = tierUpper < 0 ? -1 : Math.max(this.tierLower, tierUpper);
        this.overlay = overlay == null ? OverlayType.NONE : overlay;
    }

    public List<HbmIngredient> inputs() {
        return inputs;
    }

    public List<HbmItemOutput> outputs() {
        return outputs;
    }

    public int tierLower() {
        return tierLower;
    }

    public int tierUpper() {
        return tierUpper;
    }

    public OverlayType overlay() {
        return overlay;
    }

    public boolean isTierValid(int tier) {
        return tier >= tierLower && (tierUpper < 0 || tier <= tierUpper);
    }

    public ItemStack displayStack() {
        if (overlay == OverlayType.RECYCLING) {
            for (HbmIngredient input : inputs) {
                List<ItemStack> stacks = input.displayStacks();
                if (!stacks.isEmpty()) {
                    return stacks.get(0).copy();
                }
            }
        }
        return outputs.get(0).representativeStack();
    }

    @Override
    public boolean matches(Container container, Level level) {
        List<ItemStack> remaining = new ArrayList<>(container.getContainerSize());
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                remaining.add(stack.copy());
            }
        }

        for (HbmIngredient input : inputs) {
            int needed = input.count();
            for (ItemStack stack : remaining) {
                if (needed <= 0) {
                    break;
                }
                if (!stack.isEmpty() && input.test(stack, true)) {
                    int taken = Math.min(needed, stack.getCount());
                    stack.shrink(taken);
                    needed -= taken;
                }
            }
            if (needed > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return outputs.get(0).representativeStack();
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
        inputs.forEach(input -> ingredients.add(input.ingredient()));
        return ingredients;
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
        return ModRecipes.ANVIL_CONSTRUCTION.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ANVIL_CONSTRUCTION.type().get();
    }

    public enum OverlayType {
        NONE,
        CONSTRUCTION,
        RECYCLING,
        SMITHING;

        public static OverlayType byName(String name) {
            if (name == null || name.isBlank()) {
                return NONE;
            }
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        }
    }

    public static class Serializer implements RecipeSerializer<AnvilConstructionRecipe> {
        @Override
        public AnvilConstructionRecipe fromJson(ResourceLocation id, JsonObject json) {
            List<HbmIngredient> inputs = readInputs(json);
            List<HbmItemOutput> outputs = readOutputs(json);
            int tierLower = json.has("tier_lower")
                    ? GsonHelper.getAsInt(json, "tier_lower")
                    : GsonHelper.getAsInt(json, "tierLower", 0);
            int tierUpper = json.has("tier_upper")
                    ? GsonHelper.getAsInt(json, "tier_upper")
                    : GsonHelper.getAsInt(json, "tierUpper", -1);
            OverlayType overlay = OverlayType.byName(GsonHelper.getAsString(json, "overlay", "none"));
            return new AnvilConstructionRecipe(id, inputs, outputs, tierLower, tierUpper, overlay);
        }

        @Nullable
        @Override
        public AnvilConstructionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            List<HbmIngredient> inputs = buffer.readList(HbmIngredient::fromNetwork);
            List<HbmItemOutput> outputs = buffer.readList(HbmItemOutput::fromNetwork);
            int tierLower = buffer.readVarInt();
            int tierUpper = buffer.readVarInt();
            OverlayType overlay = buffer.readEnum(OverlayType.class);
            return new AnvilConstructionRecipe(id, inputs, outputs, tierLower, tierUpper, overlay);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, AnvilConstructionRecipe recipe) {
            buffer.writeCollection(recipe.inputs, (output, input) -> input.toNetwork(output));
            buffer.writeCollection(recipe.outputs, (output, itemOutput) -> itemOutput.toNetwork(output));
            buffer.writeVarInt(recipe.tierLower);
            buffer.writeVarInt(recipe.tierUpper);
            buffer.writeEnum(recipe.overlay);
        }

        private static List<HbmIngredient> readInputs(JsonObject json) {
            if (json.has("inputs")) {
                JsonArray array = GsonHelper.getAsJsonArray(json, "inputs");
                return array.asList().stream()
                        .map(element -> HbmIngredient.fromJson(
                                GsonHelper.convertToJsonObject(element, "anvil construction input")))
                        .toList();
            }
            return List.of(HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input")));
        }

        private static List<HbmItemOutput> readOutputs(JsonObject json) {
            if (json.has("outputs")) {
                JsonArray array = GsonHelper.getAsJsonArray(json, "outputs");
                return array.asList().stream()
                        .map(element -> HbmItemOutput.fromJson(
                                GsonHelper.convertToJsonObject(element, "anvil construction output")))
                        .toList();
            }
            return List.of(HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output")));
        }
    }
}
