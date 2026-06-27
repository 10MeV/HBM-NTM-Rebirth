package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
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
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.Nullable;

public class ElectrolyserMetalRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmIngredient input;
    @Nullable
    private final MaterialStack output1;
    @Nullable
    private final MaterialStack output2;
    private final List<ItemStack> byproducts;
    private final int duration;

    public ElectrolyserMetalRecipe(ResourceLocation id, HbmIngredient input, @Nullable MaterialStack output1,
            @Nullable MaterialStack output2, List<ItemStack> byproducts, int duration) {
        this.id = id;
        this.input = input;
        this.output1 = copyOrNull(output1);
        this.output2 = copyOrNull(output2);
        this.byproducts = byproducts == null ? List.of() : byproducts.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
        this.duration = Math.max(1, duration);
        if (input == null) {
            throw new IllegalArgumentException("Electrolyser metal recipe must have an input");
        }
        if ((this.output1 == null || this.output1.isEmpty())
                && (this.output2 == null || this.output2.isEmpty())
                && this.byproducts.isEmpty()) {
            throw new IllegalArgumentException("Electrolyser metal recipe must have at least one output");
        }
    }

    public HbmIngredient input() {
        return input;
    }

    @Nullable
    public MaterialStack output1() {
        return copyOrNull(output1);
    }

    @Nullable
    public MaterialStack output2() {
        return copyOrNull(output2);
    }

    public List<ItemStack> byproducts() {
        return byproducts;
    }

    public int duration() {
        return duration;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return !container.isEmpty() && input.test(container.getItem(0));
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
        return new ItemStack(ModBlocks.MACHINE_ELECTROLYSER.get());
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
        return ModRecipes.ELECTROLYZER_METAL.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ELECTROLYZER_METAL.type().get();
    }

    @Nullable
    private static MaterialStack copyOrNull(@Nullable MaterialStack stack) {
        return stack == null || stack.isEmpty() ? null : stack.copy();
    }

    public static class Serializer implements RecipeSerializer<ElectrolyserMetalRecipe> {
        @Override
        public ElectrolyserMetalRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient input = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            MaterialStack output1 = readOptionalMaterialStack(json.get("output1"), "electrolyzer metal output1");
            MaterialStack output2 = readOptionalMaterialStack(json.get("output2"), "electrolyzer metal output2");
            List<ItemStack> byproducts = readByproducts(json.getAsJsonArray("byproducts"));
            int duration = GsonHelper.getAsInt(json, "duration", 600);
            return new ElectrolyserMetalRecipe(id, input, output1, output2, byproducts, duration);
        }

        @Nullable
        @Override
        public ElectrolyserMetalRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient input = HbmIngredient.fromNetwork(buffer);
            MaterialStack output1 = readMaterialStack(buffer);
            MaterialStack output2 = readMaterialStack(buffer);
            int duration = buffer.readVarInt();
            int byproductCount = buffer.readVarInt();
            List<ItemStack> byproducts = new ArrayList<>();
            for (int i = 0; i < byproductCount; i++) {
                ItemStack stack = buffer.readItem();
                if (!stack.isEmpty()) {
                    byproducts.add(stack);
                }
            }
            return new ElectrolyserMetalRecipe(id, input, output1, output2, byproducts, duration);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ElectrolyserMetalRecipe recipe) {
            recipe.input.toNetwork(buffer);
            writeMaterialStack(buffer, recipe.output1);
            writeMaterialStack(buffer, recipe.output2);
            buffer.writeVarInt(recipe.duration);
            buffer.writeVarInt(recipe.byproducts.size());
            for (ItemStack byproduct : recipe.byproducts) {
                buffer.writeItem(byproduct);
            }
        }

        private static List<ItemStack> readByproducts(@Nullable JsonArray array) {
            if (array == null) {
                return List.of();
            }
            List<ItemStack> byproducts = new ArrayList<>();
            for (JsonElement element : array) {
                ItemStack stack = CraftingHelper.getItemStack(element.getAsJsonObject(), true);
                if (!stack.isEmpty()) {
                    byproducts.add(stack);
                }
            }
            return List.copyOf(byproducts);
        }

        @Nullable
        private static MaterialStack readOptionalMaterialStack(@Nullable JsonElement element, String name) {
            if (element == null || element.isJsonNull()) {
                return null;
            }
            MaterialStack stack;
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                stack = readMaterialStack(GsonHelper.getAsString(object, "material"),
                        GsonHelper.getAsInt(object, "amount", 0), name);
            } else if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                if (array.size() < 2) {
                    throw new JsonSyntaxException("Legacy material array for " + name + " needs material and amount");
                }
                stack = readMaterialStack(array.get(0).getAsString(), array.get(1).getAsInt(), name);
            } else {
                throw new JsonSyntaxException("Expected object or legacy array for " + name + ": " + element);
            }
            if (stack == null || stack.isEmpty()) {
                throw new JsonSyntaxException("Invalid material stack in " + name + ": " + element);
            }
            return stack;
        }

        private static MaterialStack readMaterialStack(String materialName, int amount, String name) {
            NTMMaterial material = materialByName(materialName);
            if (material == null) {
                throw new JsonSyntaxException("Unknown material '" + materialName + "' in " + name);
            }
            if (amount <= 0) {
                throw new JsonSyntaxException("Invalid material amount " + amount + " in " + name);
            }
            return new MaterialStack(material, amount);
        }

        @Nullable
        private static MaterialStack readMaterialStack(FriendlyByteBuf buffer) {
            if (!buffer.readBoolean()) {
                return null;
            }
            NTMMaterial material = materialByName(buffer.readUtf());
            int amount = buffer.readVarInt();
            return material == null || amount <= 0 ? null : new MaterialStack(material, amount);
        }

        @Nullable
        private static NTMMaterial materialByName(String name) {
            NTMMaterial material = Mats.matByName.get(name);
            if (material != null) {
                return material;
            }
            String normalized = name.toLowerCase(Locale.ROOT);
            for (NTMMaterial candidate : Mats.orderedList) {
                for (String candidateName : candidate.names) {
                    if (candidateName.equalsIgnoreCase(name) || candidateName.toLowerCase(Locale.ROOT).equals(normalized)) {
                        return candidate;
                    }
                }
            }
            return null;
        }

        private static void writeMaterialStack(FriendlyByteBuf buffer, @Nullable MaterialStack stack) {
            if (stack == null || stack.isEmpty()) {
                buffer.writeBoolean(false);
                return;
            }
            buffer.writeBoolean(true);
            buffer.writeUtf(stack.material.names[0]);
            buffer.writeVarInt(stack.amount);
        }
    }
}
