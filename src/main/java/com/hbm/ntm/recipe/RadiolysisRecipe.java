package com.hbm.ntm.recipe;

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

public class RadiolysisRecipe implements Recipe<Container> {
    public static final int LEGACY_INPUT_AMOUNT = 100;

    private final ResourceLocation id;
    private final HbmFluidStack input;
    private final HbmFluidStack output1;
    private final HbmFluidStack output2;
    private final int sourceOrder;

    public RadiolysisRecipe(ResourceLocation id, HbmFluidStack input, HbmFluidStack output1,
            HbmFluidStack output2, int sourceOrder) {
        if (input == null || input.type() == HbmFluids.NONE || input.amount() != LEGACY_INPUT_AMOUNT) {
            throw new IllegalArgumentException("Radiolysis input must be exactly 100mB of a real fluid: " + id);
        }
        if (output1 == null || output2 == null || output1.isEmpty() && output2.isEmpty()) {
            throw new IllegalArgumentException("Radiolysis recipe needs at least one fluid output: " + id);
        }
        this.id = id;
        this.input = input;
        this.output1 = output1;
        this.output2 = output2;
        this.sourceOrder = sourceOrder;
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
        return new ItemStack(ModBlocks.MACHINE_RADIOLYSIS.get());
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
        return ModRecipes.RADIOLYSIS.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.RADIOLYSIS.type().get();
    }

    public static class Serializer implements RecipeSerializer<RadiolysisRecipe> {
        @Override
        public RadiolysisRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmFluidStack input = readInput(GsonHelper.getAsJsonObject(json, "input"), id);
            HbmFluidStack output1 = readOutput(GsonHelper.getAsJsonObject(json, "output1"), "output1", id);
            HbmFluidStack output2 = readOutput(GsonHelper.getAsJsonObject(json, "output2"), "output2", id);
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", 0);
            return new RadiolysisRecipe(id, input, output1, output2, sourceOrder);
        }

        @Nullable
        @Override
        public RadiolysisRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmFluidStack input = readFluidStack(buffer);
            HbmFluidStack output1 = readFluidStack(buffer);
            HbmFluidStack output2 = readFluidStack(buffer);
            int sourceOrder = buffer.readVarInt();
            return new RadiolysisRecipe(id, input, output1, output2, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RadiolysisRecipe recipe) {
            writeFluidStack(buffer, recipe.input);
            writeFluidStack(buffer, recipe.output1);
            writeFluidStack(buffer, recipe.output2);
            buffer.writeVarInt(recipe.sourceOrder);
        }

        private static HbmFluidStack readInput(JsonObject object, ResourceLocation id) {
            HbmFluidStack input = readFluidStack(object, "input", false);
            if (input.amount() != LEGACY_INPUT_AMOUNT) {
                throw new JsonSyntaxException("Radiolysis input amount must be 100mB in " + id);
            }
            return input;
        }

        private static HbmFluidStack readOutput(JsonObject object, String name, ResourceLocation id) {
            HbmFluidStack stack = readFluidStack(object, name, true);
            if (stack.type() == HbmFluids.NONE && stack.amount() != 0) {
                throw new JsonSyntaxException("Radiolysis empty output must use amount 0 in " + id + " field " + name);
            }
            return stack;
        }

        private static HbmFluidStack readFluidStack(JsonObject object, String name, boolean allowEmpty) {
            FluidType type = HbmFluidJsonUtil.readFluidReference(object.get("fluid"));
            int amount = GsonHelper.getAsInt(object, "amount");
            if (type == HbmFluids.NONE) {
                if (allowEmpty && amount == 0 && HbmFluidJsonUtil.isExplicitNoneReference(object.get("fluid"))) {
                    return new HbmFluidStack(HbmFluids.NONE, 0);
                }
                throw HbmFluidJsonUtil.unknownFluidReference(name + " fluid", object.get("fluid"));
            }
            if (amount <= 0) {
                throw new JsonSyntaxException("Invalid fluid amount " + amount + " in " + name);
            }
            int pressure = GsonHelper.getAsInt(object, "pressure", 0);
            return new HbmFluidStack(type, amount, pressure);
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
