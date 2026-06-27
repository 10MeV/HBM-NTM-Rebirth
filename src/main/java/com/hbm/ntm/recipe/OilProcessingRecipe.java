package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.Nullable;

public class OilProcessingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Machine machine;
    private final List<HbmFluidStack> fluidInputs;
    private final List<HbmFluidStack> fluidOutputs;
    private final ItemStack itemOutput;

    public OilProcessingRecipe(ResourceLocation id, Machine machine, List<HbmFluidStack> fluidInputs,
            List<HbmFluidStack> fluidOutputs, ItemStack itemOutput) {
        this.id = id;
        this.machine = machine;
        this.fluidInputs = sanitizeInputs(fluidInputs);
        this.fluidOutputs = sanitizeOutputs(fluidOutputs);
        this.itemOutput = itemOutput == null ? ItemStack.EMPTY : itemOutput.copy();
        if (this.fluidInputs.isEmpty()) {
            throw new IllegalArgumentException("Oil processing recipe needs at least one fluid input: " + id);
        }
        if (this.fluidOutputs.size() > machine.maxFluidOutputs) {
            throw new IllegalArgumentException(machine + " recipe has too many fluid outputs: " + id);
        }
        if (this.fluidOutputs.isEmpty() && this.itemOutput.isEmpty()) {
            throw new IllegalArgumentException("Oil processing recipe needs at least one output: " + id);
        }
    }

    public Machine machine() {
        return machine;
    }

    public HbmFluidStack primaryInput() {
        return fluidInputs.get(0);
    }

    public List<HbmFluidStack> fluidInputs() {
        return fluidInputs;
    }

    public List<HbmFluidStack> fluidOutputs() {
        return fluidOutputs;
    }

    public ItemStack itemOutput() {
        return itemOutput.copy();
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return getResultItem(registryAccess);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return itemOutput();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(machine.toastSymbol.get());
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
        return machine.serializer();
    }

    @Override
    public RecipeType<?> getType() {
        return machine.type();
    }

    private static List<HbmFluidStack> sanitizeInputs(List<HbmFluidStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return List.of();
        }
        List<HbmFluidStack> result = new ArrayList<>();
        for (HbmFluidStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                result.add(stack);
            }
        }
        return List.copyOf(result);
    }

    private static List<HbmFluidStack> sanitizeOutputs(List<HbmFluidStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return List.of();
        }
        List<HbmFluidStack> result = new ArrayList<>();
        for (HbmFluidStack stack : stacks) {
            result.add(stack == null ? new HbmFluidStack(HbmFluids.NONE, 0) : stack);
        }
        return List.copyOf(result);
    }

    public enum Machine {
        REFINERY("refinery", 4, () -> ModBlocks.MACHINE_REFINERY.get()),
        CATALYTIC_CRACKER("catalytic_cracker", 2, () -> ModBlocks.MACHINE_CATALYTIC_CRACKER.get()),
        CATALYTIC_REFORMER("catalytic_reformer", 3, () -> ModBlocks.MACHINE_CATALYTIC_REFORMER.get()),
        VACUUM_DISTILL("vacuum_distill", 4, () -> ModBlocks.MACHINE_VACUUM_DISTILL.get()),
        FRACTION_TOWER("fraction_tower", 2, () -> ModBlocks.MACHINE_FRACTION_TOWER.get()),
        HYDROTREATER("hydrotreater", 2, () -> ModBlocks.MACHINE_HYDROTREATER.get()),
        SOLIDIFIER("solidifier", 0, () -> ModBlocks.MACHINE_SOLIDIFIER.get()),
        COKER("coker", 1, () -> ModBlocks.MACHINE_COKER.get());

        private final String typeName;
        private final int maxFluidOutputs;
        private final Supplier<? extends ItemLike> toastSymbol;

        Machine(String typeName, int maxFluidOutputs, Supplier<? extends ItemLike> toastSymbol) {
            this.typeName = typeName;
            this.maxFluidOutputs = maxFluidOutputs;
            this.toastSymbol = toastSymbol;
        }

        public String typeName() {
            return typeName;
        }

        public int maxFluidOutputs() {
            return maxFluidOutputs;
        }

        public RecipeType<OilProcessingRecipe> type() {
            return switch (this) {
                case REFINERY -> ModRecipes.REFINERY.type().get();
                case CATALYTIC_CRACKER -> ModRecipes.CATALYTIC_CRACKER.type().get();
                case CATALYTIC_REFORMER -> ModRecipes.CATALYTIC_REFORMER.type().get();
                case VACUUM_DISTILL -> ModRecipes.VACUUM_DISTILL.type().get();
                case FRACTION_TOWER -> ModRecipes.FRACTION_TOWER.type().get();
                case HYDROTREATER -> ModRecipes.HYDROTREATER.type().get();
                case SOLIDIFIER -> ModRecipes.SOLIDIFIER.type().get();
                case COKER -> ModRecipes.COKER.type().get();
            };
        }

        public RecipeSerializer<OilProcessingRecipe> serializer() {
            return switch (this) {
                case REFINERY -> ModRecipes.REFINERY.serializer().get();
                case CATALYTIC_CRACKER -> ModRecipes.CATALYTIC_CRACKER.serializer().get();
                case CATALYTIC_REFORMER -> ModRecipes.CATALYTIC_REFORMER.serializer().get();
                case VACUUM_DISTILL -> ModRecipes.VACUUM_DISTILL.serializer().get();
                case FRACTION_TOWER -> ModRecipes.FRACTION_TOWER.serializer().get();
                case HYDROTREATER -> ModRecipes.HYDROTREATER.serializer().get();
                case SOLIDIFIER -> ModRecipes.SOLIDIFIER.serializer().get();
                case COKER -> ModRecipes.COKER.serializer().get();
            };
        }
    }

    public static class Serializer implements RecipeSerializer<OilProcessingRecipe> {
        private final Machine machine;

        public Serializer(Machine machine) {
            this.machine = machine;
        }

        @Override
        public OilProcessingRecipe fromJson(ResourceLocation id, JsonObject json) {
            List<HbmFluidStack> inputs = readFluidInputs(json);
            List<HbmFluidStack> outputs = readFluidOutputs(json);
            ItemStack itemOutput = readOptionalItem(json);
            return new OilProcessingRecipe(id, machine, inputs, outputs, itemOutput);
        }

        @Nullable
        @Override
        public OilProcessingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            List<HbmFluidStack> inputs = buffer.readList(Serializer::readFluidStack);
            List<HbmFluidStack> outputs = buffer.readList(Serializer::readFluidStack);
            ItemStack itemOutput = buffer.readItem();
            return new OilProcessingRecipe(id, machine, inputs, outputs, itemOutput);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, OilProcessingRecipe recipe) {
            buffer.writeCollection(recipe.fluidInputs, Serializer::writeFluidStack);
            buffer.writeCollection(recipe.fluidOutputs, Serializer::writeFluidStack);
            buffer.writeItem(recipe.itemOutput);
        }

        private List<HbmFluidStack> readFluidInputs(JsonObject json) {
            if (json.has("fluid_inputs")) {
                return readFluidArray(GsonHelper.getAsJsonArray(json, "fluid_inputs"), "fluid_inputs", 100);
            }
            List<HbmFluidStack> inputs = new ArrayList<>();
            inputs.add(readFluidElement(GsonHelper.getNonNull(json, "input"), "input", 100));
            if (json.has("hydrogen")) {
                inputs.add(readFluidElement(json.get("hydrogen"), "hydrogen", 0));
            }
            return inputs;
        }

        private List<HbmFluidStack> readFluidOutputs(JsonObject json) {
            if (json.has("fluid_outputs")) {
                return readFluidArray(GsonHelper.getAsJsonArray(json, "fluid_outputs"), "fluid_outputs", 0);
            }
            if (json.has("byproduct")) {
                return List.of(readFluidElement(json.get("byproduct"), "byproduct", 0));
            }
            List<HbmFluidStack> outputs = new ArrayList<>();
            boolean zeroBasedFields = json.has("output0");
            for (int i = 0; i < machine.maxFluidOutputs; i++) {
                String field = zeroBasedFields ? "output" + i : "output" + (i + 1);
                if (json.has(field)) {
                    outputs.add(readFluidElement(json.get(field), field, 0));
                }
            }
            return outputs;
        }

        private static ItemStack readOptionalItem(JsonObject json) {
            if (json.has("item_output")) {
                return CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "item_output"), true);
            }
            if (json.has("solid")) {
                return CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "solid"), true);
            }
            if (json.has("output") && json.get("output").isJsonObject()) {
                return CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "output"), true);
            }
            return ItemStack.EMPTY;
        }

        private static List<HbmFluidStack> readFluidArray(JsonArray array, String name, int defaultAmount) {
            List<HbmFluidStack> stacks = new ArrayList<>();
            for (JsonElement element : array) {
                stacks.add(readFluidElement(element, name, defaultAmount));
            }
            return stacks;
        }

        private static HbmFluidStack readFluidElement(JsonElement element, String name, int defaultAmount) {
            if (element == null || element.isJsonNull()) {
                throw new JsonSyntaxException("Missing fluid stack " + name);
            }
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                if (array.size() < 2) {
                    throw new JsonSyntaxException("Legacy fluid array for " + name + " needs fluid and amount");
                }
                FluidType type = readFluidReference(array.get(0), name, true);
                int amount = array.get(1).getAsInt();
                HbmFluidStack stack = new HbmFluidStack(type, amount, array.size() < 3 ? 0 : array.get(2).getAsInt());
                if (stack.isEmpty() && !(type == HbmFluids.NONE && amount == 0)) {
                    throw new JsonSyntaxException("Invalid legacy fluid array for " + name + ": " + array);
                }
                return stack;
            }
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("amount")) {
                    FluidType type = readFluidReference(object.get("fluid"), name, true);
                    int amount = GsonHelper.getAsInt(object, "amount");
                    if (type == HbmFluids.NONE && amount == 0) {
                        return new HbmFluidStack(HbmFluids.NONE, 0);
                    }
                    return HbmFluidJsonUtil.readFluidStack(object, name);
                }
                FluidType type = HbmFluidJsonUtil.requireFluidReference(object.get("fluid"), name);
                return new HbmFluidStack(type, defaultAmount);
            }
            FluidType type = HbmFluidJsonUtil.requireFluidReference(element, name);
            return new HbmFluidStack(type, defaultAmount);
        }

        private static FluidType readFluidReference(JsonElement element, String name, boolean allowEmpty) {
            FluidType type = HbmFluidJsonUtil.readFluidReference(element);
            if (type == HbmFluids.NONE && !(allowEmpty && HbmFluidJsonUtil.isExplicitNoneReference(element))) {
                throw new JsonSyntaxException("Unknown HBM fluid reference in " + name + ": " + element);
            }
            return type;
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
