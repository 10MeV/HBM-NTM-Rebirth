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
import org.jetbrains.annotations.Nullable;

public class CrucibleRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final String internalName;
    private final String fallbackName;
    private final ItemStack icon;
    private final List<MaterialStack> input;
    private final List<MaterialStack> output;
    private final int frequency;
    private final int sourceOrder;

    public CrucibleRecipe(ResourceLocation id, String internalName, String fallbackName, ItemStack icon,
            List<MaterialStack> input, List<MaterialStack> output, int frequency, int sourceOrder) {
        this.id = id;
        this.internalName = internalName == null || internalName.isBlank() ? id.toString() : internalName;
        this.fallbackName = fallbackName == null || fallbackName.isBlank() ? this.internalName : fallbackName;
        this.icon = icon == null ? ItemStack.EMPTY : icon.copy();
        this.input = copyMaterialStacks(input, "input", id);
        this.output = copyMaterialStacks(output, "output", id);
        this.frequency = Math.max(1, frequency);
        this.sourceOrder = sourceOrder;
        if (this.input.isEmpty()) {
            throw new IllegalArgumentException("Crucible recipe needs at least one input material: " + id);
        }
        if (this.output.isEmpty()) {
            throw new IllegalArgumentException("Crucible recipe needs at least one output material: " + id);
        }
    }

    public String internalName() {
        return internalName;
    }

    public String fallbackName() {
        return fallbackName;
    }

    public ItemStack icon() {
        return icon.copy();
    }

    public List<MaterialStack> input() {
        return input.stream().map(MaterialStack::copy).toList();
    }

    public List<MaterialStack> output() {
        return output.stream().map(MaterialStack::copy).toList();
    }

    public int frequency() {
        return frequency;
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    public CrucibleRecipeRuntime.Recipe runtimeRecipe() {
        return new CrucibleRecipeRuntime.Recipe(internalName, fallbackName, this::icon, input, output, frequency,
                sourceOrder);
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
        return icon();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_CRUCIBLE.get());
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
        return ModRecipes.CRUCIBLE.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CRUCIBLE.type().get();
    }

    private static List<MaterialStack> copyMaterialStacks(List<MaterialStack> stacks, String field,
            ResourceLocation id) {
        List<MaterialStack> copy = new ArrayList<>();
        if (stacks != null) {
            for (MaterialStack stack : stacks) {
                if (stack != null && !stack.isEmpty()) {
                    copy.add(stack.copy());
                }
            }
        }
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("Crucible recipe " + id + " has empty " + field);
        }
        return List.copyOf(copy);
    }

    public static class Serializer implements RecipeSerializer<CrucibleRecipe> {
        @Override
        public CrucibleRecipe fromJson(ResourceLocation id, JsonObject json) {
            String internalName = GsonHelper.getAsString(json, "internal_name", id.toString());
            String fallbackName = GsonHelper.getAsString(json, "fallback_name", internalName);
            ItemStack icon = json.has("icon")
                    ? HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "icon")).representativeStack()
                    : ItemStack.EMPTY;
            List<MaterialStack> input = readMaterialStacks(GsonHelper.getAsJsonArray(json, "input"),
                    "crucible input", id);
            List<MaterialStack> output = readMaterialStacks(GsonHelper.getAsJsonArray(json, "output"),
                    "crucible output", id);
            int frequency = GsonHelper.getAsInt(json, "frequency", 1);
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", Integer.MAX_VALUE);
            return new CrucibleRecipe(id, internalName, fallbackName, icon, input, output, frequency, sourceOrder);
        }

        @Nullable
        @Override
        public CrucibleRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            String internalName = buffer.readUtf();
            String fallbackName = buffer.readUtf();
            ItemStack icon = buffer.readItem();
            List<MaterialStack> input = buffer.readList(CrucibleRecipe.Serializer::readMaterialStack);
            List<MaterialStack> output = buffer.readList(CrucibleRecipe.Serializer::readMaterialStack);
            int frequency = buffer.readVarInt();
            int sourceOrder = buffer.readVarInt();
            return new CrucibleRecipe(id, internalName, fallbackName, icon, input, output, frequency, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CrucibleRecipe recipe) {
            buffer.writeUtf(recipe.internalName);
            buffer.writeUtf(recipe.fallbackName);
            buffer.writeItem(recipe.icon);
            buffer.writeCollection(recipe.input, CrucibleRecipe.Serializer::writeMaterialStack);
            buffer.writeCollection(recipe.output, CrucibleRecipe.Serializer::writeMaterialStack);
            buffer.writeVarInt(recipe.frequency);
            buffer.writeVarInt(recipe.sourceOrder);
        }

        private static List<MaterialStack> readMaterialStacks(JsonArray array, String field, ResourceLocation id) {
            List<MaterialStack> stacks = new ArrayList<>();
            for (JsonElement element : array) {
                stacks.add(readMaterialStack(element, field, id));
            }
            return List.copyOf(stacks);
        }

        private static MaterialStack readMaterialStack(JsonElement element, String field, ResourceLocation id) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                return readMaterialStack(GsonHelper.getAsString(object, "material"),
                        GsonHelper.getAsInt(object, "amount"), field, id);
            }
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                if (array.size() < 2) {
                    throw new JsonSyntaxException("Legacy material array for " + field
                            + " needs material and amount in " + id);
                }
                return readMaterialStack(array.get(0).getAsString(), array.get(1).getAsInt(), field, id);
            }
            throw new JsonSyntaxException("Expected material object or legacy array for " + field + " in " + id);
        }

        private static MaterialStack readMaterialStack(String materialName, int amount, String field,
                ResourceLocation id) {
            NTMMaterial material = materialByName(materialName);
            if (material == null) {
                throw new JsonSyntaxException("Unknown crucible material '" + materialName + "' in " + field
                        + " of " + id);
            }
            if (amount <= 0) {
                throw new JsonSyntaxException("Invalid crucible material amount " + amount + " in " + field
                        + " of " + id);
            }
            return new MaterialStack(material, amount);
        }

        private static MaterialStack readMaterialStack(FriendlyByteBuf buffer) {
            NTMMaterial material = materialByName(buffer.readUtf());
            int amount = buffer.readVarInt();
            return new MaterialStack(material, amount);
        }

        private static void writeMaterialStack(FriendlyByteBuf buffer, MaterialStack stack) {
            buffer.writeUtf(stack.material.names[0]);
            buffer.writeVarInt(stack.amount);
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
                    if (candidateName.equalsIgnoreCase(name)
                            || candidateName.toLowerCase(Locale.ROOT).equals(normalized)) {
                        return candidate;
                    }
                }
            }
            return null;
        }
    }
}
