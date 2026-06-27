package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
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
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.Nullable;

public class ElectrolyserFluidRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmFluidStack input;
    private final HbmFluidStack output1;
    private final HbmFluidStack output2;
    private final List<ItemStack> byproducts;
    private final int duration;

    public ElectrolyserFluidRecipe(ResourceLocation id, HbmFluidStack input, HbmFluidStack output1,
            HbmFluidStack output2, List<ItemStack> byproducts, int duration) {
        this.id = id;
        this.input = input == null ? new HbmFluidStack(HbmFluids.NONE, 0) : input;
        this.output1 = output1 == null ? new HbmFluidStack(HbmFluids.NONE, 0) : output1;
        this.output2 = output2 == null ? new HbmFluidStack(HbmFluids.NONE, 0) : output2;
        this.byproducts = byproducts == null ? List.of() : byproducts.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
        this.duration = Math.max(1, duration);
        if (this.input.isEmpty()) {
            throw new IllegalArgumentException("Electrolyser fluid recipe must have a fluid input");
        }
        if (this.output1.isEmpty() && this.output2.isEmpty() && this.byproducts.isEmpty()) {
            throw new IllegalArgumentException("Electrolyser fluid recipe must have at least one output");
        }
    }

    public HbmFluidStack input() {
        return input;
    }

    public HbmFluidStack output1() {
        return output1;
    }

    public HbmFluidStack output2() {
        return output2;
    }

    public List<ItemStack> byproducts() {
        return byproducts;
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
        return ModRecipes.ELECTROLYZER_FLUID.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ELECTROLYZER_FLUID.type().get();
    }

    public static class Serializer implements RecipeSerializer<ElectrolyserFluidRecipe> {
        @Override
        public ElectrolyserFluidRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmFluidStack input = readRequiredFluidStack(GsonHelper.getNonNull(json, "input"),
                    "electrolyzer fluid input");
            HbmFluidStack output1 = readOptionalFluidStack(GsonHelper.getNonNull(json, "output1"),
                    "electrolyzer fluid output1");
            HbmFluidStack output2 = readOptionalFluidStack(GsonHelper.getNonNull(json, "output2"),
                    "electrolyzer fluid output2");
            List<ItemStack> byproducts = readByproducts(json.getAsJsonArray("byproducts"));
            int duration = GsonHelper.getAsInt(json, "duration", 20);
            return new ElectrolyserFluidRecipe(id, input, output1, output2, byproducts, duration);
        }

        @Nullable
        @Override
        public ElectrolyserFluidRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmFluidStack input = readFluidStack(buffer);
            HbmFluidStack output1 = readFluidStack(buffer);
            HbmFluidStack output2 = readFluidStack(buffer);
            int duration = buffer.readVarInt();
            int byproductCount = buffer.readVarInt();
            List<ItemStack> byproducts = new ArrayList<>();
            for (int i = 0; i < byproductCount; i++) {
                ItemStack stack = buffer.readItem();
                if (!stack.isEmpty()) {
                    byproducts.add(stack);
                }
            }
            return new ElectrolyserFluidRecipe(id, input, output1, output2, byproducts, duration);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ElectrolyserFluidRecipe recipe) {
            writeFluidStack(buffer, recipe.input);
            writeFluidStack(buffer, recipe.output1);
            writeFluidStack(buffer, recipe.output2);
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

        private static HbmFluidStack readRequiredFluidStack(JsonElement element, String name) {
            HbmFluidStack stack = readFluidStack(element, name, false);
            if (stack.isEmpty()) {
                throw new JsonSyntaxException("Missing or empty " + name);
            }
            return stack;
        }

        private static HbmFluidStack readOptionalFluidStack(JsonElement element, String name) {
            return readFluidStack(element, name, true);
        }

        private static HbmFluidStack readFluidStack(JsonElement element, String name, boolean allowEmpty) {
            if (element == null || element.isJsonNull()) {
                return allowEmpty ? new HbmFluidStack(HbmFluids.NONE, 0) : invalidFluid(name, element);
            }
            HbmFluidStack stack;
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                FluidType type = readFluidReference(object, name, allowEmpty);
                int amount = GsonHelper.getAsInt(object, "amount", 0);
                int pressure = GsonHelper.getAsInt(object, "pressure", 0);
                stack = new HbmFluidStack(type, amount, pressure);
            } else if (element.isJsonArray()) {
                stack = readLegacyFluidArray(element.getAsJsonArray(), name, allowEmpty);
            } else {
                throw new JsonSyntaxException("Expected object or legacy array for " + name + ": " + element);
            }
            if (stack.isEmpty() && !(allowEmpty && stack.type() == HbmFluids.NONE && stack.amount() == 0)) {
                throw new JsonSyntaxException("Invalid " + name + ": " + element);
            }
            if (!stack.isEmpty() && stack.amount() <= 0) {
                throw new JsonSyntaxException("Invalid fluid amount in " + name + ": " + element);
            }
            return stack;
        }

        private static HbmFluidStack invalidFluid(String name, @Nullable JsonElement element) {
            throw new JsonSyntaxException("Missing " + name + ": " + element);
        }

        private static HbmFluidStack readLegacyFluidArray(JsonArray array, String name, boolean allowEmpty) {
            if (array.size() < 2) {
                throw new JsonSyntaxException("Legacy fluid array for " + name + " needs fluid and amount");
            }
            FluidType type = readFluidReference(array.get(0), name, allowEmpty);
            int amount = array.get(1).getAsInt();
            int pressure = array.size() < 3 ? 0 : HbmFluidTank.clampPressure(array.get(2).getAsInt());
            HbmFluidStack stack = new HbmFluidStack(type, amount, pressure);
            if (!allowEmpty && stack.isEmpty()) {
                throw new JsonSyntaxException("Invalid legacy fluid array for " + name + ": " + array);
            }
            return stack;
        }

        private static FluidType readFluidReference(JsonElement element, String name, boolean allowEmpty) {
            FluidType type = HbmFluidJsonUtil.readFluidReference(element);
            if (type == HbmFluids.NONE && !(allowEmpty && HbmFluidJsonUtil.isExplicitNoneReference(element))) {
                throw new JsonSyntaxException("Unknown HBM fluid reference in " + name + ": " + element);
            }
            return type;
        }

        private static HbmFluidStack readFluidStack(FriendlyByteBuf buffer) {
            return new HbmFluidStack(HbmFluids.fromName(buffer.readUtf()), buffer.readVarInt(),
                    buffer.readVarInt());
        }

        private static void writeFluidStack(FriendlyByteBuf buffer, HbmFluidStack stack) {
            buffer.writeUtf(stack.type().getName());
            buffer.writeVarInt(stack.amount());
            buffer.writeVarInt(stack.pressure());
        }
    }
}
