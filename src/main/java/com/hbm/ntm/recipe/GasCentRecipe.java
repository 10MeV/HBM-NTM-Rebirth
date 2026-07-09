package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.blockentity.GasCentBlockEntity.PseudoFluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
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

public class GasCentRecipe implements Recipe<Container> {
    public static final int MAX_OUTPUTS = 4;

    private final ResourceLocation id;
    private final HbmFluidStack input;
    private final List<ItemStack> outputs;
    private final boolean highSpeed;
    private final int centrifuges;
    private final PseudoFluidType inputType;
    private final PseudoFluidType outputType;
    private final int sourceOrder;

    public GasCentRecipe(ResourceLocation id, HbmFluidStack input, List<ItemStack> outputs, boolean highSpeed,
            int centrifuges, PseudoFluidType inputType, PseudoFluidType outputType, int sourceOrder) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Gas centrifuge recipe requires a non-empty fluid input: " + id);
        }
        List<ItemStack> resolvedOutputs = outputs == null ? List.of() : outputs.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
        if (resolvedOutputs.isEmpty() || resolvedOutputs.size() > MAX_OUTPUTS) {
            throw new IllegalArgumentException("Gas centrifuge recipe needs 1.." + MAX_OUTPUTS
                    + " item outputs: " + id);
        }
        if (centrifuges <= 0) {
            throw new IllegalArgumentException("Gas centrifuge recipe needs a positive centrifuge count: " + id);
        }
        this.id = id;
        this.input = input;
        this.outputs = List.copyOf(resolvedOutputs);
        this.highSpeed = highSpeed;
        this.centrifuges = centrifuges;
        this.inputType = inputType == null ? PseudoFluidType.NONE : inputType;
        this.outputType = outputType == null ? PseudoFluidType.NONE : outputType;
        this.sourceOrder = sourceOrder;
    }

    public HbmFluidStack input() {
        return input;
    }

    public List<ItemStack> outputs() {
        return outputs.stream().map(ItemStack::copy).toList();
    }

    public boolean highSpeed() {
        return highSpeed;
    }

    public int centrifuges() {
        return centrifuges;
    }

    public PseudoFluidType inputType() {
        return inputType;
    }

    public PseudoFluidType outputType() {
        return outputType;
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return getResultItem(access);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_GASCENT.get());
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
        return ModRecipes.GAS_CENT.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.GAS_CENT.type().get();
    }

    public static class Serializer implements RecipeSerializer<GasCentRecipe> {
        @Override
        public GasCentRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmFluidStack input = HbmFluidJsonUtil.readFluidStack(GsonHelper.getAsJsonObject(json, "input"),
                    "gas centrifuge input");
            List<ItemStack> outputs = readOutputs(GsonHelper.getAsJsonArray(json, "outputs"), id);
            boolean highSpeed = GsonHelper.getAsBoolean(json, "high_speed", false);
            int centrifuges = GsonHelper.getAsInt(json, "centrifuges", 1);
            PseudoFluidType inputType = readPseudoType(GsonHelper.getAsString(json, "input_type"), "input_type",
                    id, false);
            PseudoFluidType outputType = json.has("output_type")
                    ? readPseudoType(GsonHelper.getAsString(json, "output_type"), "output_type", id, true)
                    : PseudoFluidType.NONE;
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", Integer.MAX_VALUE);
            return new GasCentRecipe(id, input, outputs, highSpeed, centrifuges, inputType, outputType, sourceOrder);
        }

        @Override
        public @Nullable GasCentRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmFluidStack input = new HbmFluidStack(HbmFluids.fromName(buffer.readUtf()), buffer.readVarInt(),
                    buffer.readVarInt());
            List<ItemStack> outputs = buffer.readList(FriendlyByteBuf::readItem);
            boolean highSpeed = buffer.readBoolean();
            int centrifuges = buffer.readVarInt();
            PseudoFluidType inputType = buffer.readEnum(PseudoFluidType.class);
            PseudoFluidType outputType = buffer.readEnum(PseudoFluidType.class);
            int sourceOrder = buffer.readVarInt();
            return new GasCentRecipe(id, input, outputs, highSpeed, centrifuges, inputType, outputType, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GasCentRecipe recipe) {
            buffer.writeUtf(recipe.input.type().getName());
            buffer.writeVarInt(recipe.input.amount());
            buffer.writeVarInt(recipe.input.pressure());
            buffer.writeCollection(recipe.outputs, FriendlyByteBuf::writeItem);
            buffer.writeBoolean(recipe.highSpeed);
            buffer.writeVarInt(recipe.centrifuges);
            buffer.writeEnum(recipe.inputType);
            buffer.writeEnum(recipe.outputType);
            buffer.writeVarInt(recipe.sourceOrder);
        }

        private static List<ItemStack> readOutputs(JsonArray array, ResourceLocation id) {
            List<ItemStack> outputs = new ArrayList<>();
            for (JsonElement element : array) {
                JsonObject object = GsonHelper.convertToJsonObject(element, "gas centrifuge output");
                ItemStack stack = HbmItemOutput.fromJson(object).representativeStack();
                if (stack.isEmpty()) {
                    throw new JsonSyntaxException("Gas centrifuge output cannot be empty in " + id);
                }
                outputs.add(stack);
            }
            return List.copyOf(outputs);
        }

        private static PseudoFluidType readPseudoType(String name, String field, ResourceLocation id,
                boolean allowNone) {
            if (name == null || name.isBlank()) {
                if (allowNone) {
                    return PseudoFluidType.NONE;
                }
                throw new JsonSyntaxException("Missing gas centrifuge " + field + " in " + id);
            }
            String normalized = name.trim().toUpperCase(Locale.ROOT);
            for (PseudoFluidType type : PseudoFluidType.values()) {
                if (type.name().equalsIgnoreCase(normalized) || type.legacyName().equalsIgnoreCase(normalized)) {
                    if (!allowNone && type == PseudoFluidType.NONE) {
                        throw new JsonSyntaxException("Gas centrifuge " + field + " cannot be NONE in " + id);
                    }
                    return type;
                }
            }
            throw new JsonSyntaxException("Unknown gas centrifuge " + field + " '" + name + "' in " + id);
        }
    }
}
