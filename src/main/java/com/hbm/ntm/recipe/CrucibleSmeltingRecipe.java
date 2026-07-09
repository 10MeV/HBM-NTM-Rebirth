package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbm.inventory.material.Mats.MaterialStack;
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

public final class CrucibleSmeltingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmIngredient input;
    private final List<MaterialStack> output;
    private final int sourceOrder;

    public CrucibleSmeltingRecipe(ResourceLocation id, HbmIngredient input, List<MaterialStack> output,
            int sourceOrder) {
        this.id = id;
        if (input == null) {
            throw new IllegalArgumentException("Crucible smelting recipe needs an input: " + id);
        }
        this.input = input;
        this.output = copyMaterialStacks(output, id);
        this.sourceOrder = sourceOrder;
    }

    public HbmIngredient input() {
        return input;
    }

    public List<MaterialStack> output() {
        return output.stream().map(MaterialStack::copy).toList();
    }

    public int sourceOrder() {
        return sourceOrder;
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
        return ModRecipes.CRUCIBLE_SMELTING.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CRUCIBLE_SMELTING.type().get();
    }

    private static List<MaterialStack> copyMaterialStacks(List<MaterialStack> stacks, ResourceLocation id) {
        List<MaterialStack> copy = new ArrayList<>();
        if (stacks != null) {
            for (MaterialStack stack : stacks) {
                if (stack != null && !stack.isEmpty()) {
                    copy.add(stack.copy());
                }
            }
        }
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("Crucible smelting recipe has empty output: " + id);
        }
        return List.copyOf(copy);
    }

    public static final class Serializer implements RecipeSerializer<CrucibleSmeltingRecipe> {
        @Override
        public CrucibleSmeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient input = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            JsonArray outputArray = GsonHelper.getAsJsonArray(json, "output");
            List<MaterialStack> output = new ArrayList<>();
            for (JsonElement element : outputArray) {
                output.add(MaterialStackJsonUtil.readRequired(element, "crucible_smelting output in " + id));
            }
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", Integer.MAX_VALUE);
            return new CrucibleSmeltingRecipe(id, input, output, sourceOrder);
        }

        @Nullable
        @Override
        public CrucibleSmeltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient input = HbmIngredient.fromNetwork(buffer);
            List<MaterialStack> output = buffer.readList(MaterialStackJsonUtil::readNetwork);
            int sourceOrder = buffer.readVarInt();
            return new CrucibleSmeltingRecipe(id, input, output, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CrucibleSmeltingRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeCollection(recipe.output, MaterialStackJsonUtil::writeNetwork);
            buffer.writeVarInt(recipe.sourceOrder);
        }
    }
}
