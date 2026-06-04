package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

public class GenericMachineRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Machine machine;
    private final String internalName;
    private final int duration;
    private final long power;
    private final List<HbmIngredient> itemInputs;
    private final List<HbmFluidStack> fluidInputs;
    private final List<HbmItemOutput> itemOutputs;
    private final List<HbmFluidStack> fluidOutputs;
    private final List<String> pools;
    private final ItemStack icon;
    private final boolean customLocalization;
    private final GenericMachineRecipeExtraData extraData;
    @Nullable
    private final String autoSwitchGroup;
    @Nullable
    private final String nameWrapper;

    public GenericMachineRecipe(ResourceLocation id, Machine machine, String internalName, int duration, long power,
            List<HbmIngredient> itemInputs, List<HbmFluidStack> fluidInputs,
            List<HbmItemOutput> itemOutputs, List<HbmFluidStack> fluidOutputs,
            List<String> pools, ItemStack icon, boolean customLocalization,
            GenericMachineRecipeExtraData extraData, @Nullable String autoSwitchGroup, @Nullable String nameWrapper) {
        this.id = id;
        this.machine = machine;
        this.internalName = internalName.isBlank() ? id.toString() : internalName;
        this.duration = Math.max(0, duration);
        this.power = Math.max(0L, power);
        this.itemInputs = List.copyOf(itemInputs);
        this.fluidInputs = List.copyOf(fluidInputs);
        this.itemOutputs = List.copyOf(itemOutputs);
        this.fluidOutputs = List.copyOf(fluidOutputs);
        this.pools = List.copyOf(pools);
        this.icon = icon == null ? ItemStack.EMPTY : icon.copy();
        this.customLocalization = customLocalization;
        this.extraData = extraData == null ? GenericMachineRecipeExtraData.EMPTY : extraData;
        this.autoSwitchGroup = autoSwitchGroup;
        this.nameWrapper = nameWrapper;
    }

    public Machine getMachine() {
        return machine;
    }

    public String getInternalName() {
        return internalName;
    }

    public int getDuration() {
        return duration;
    }

    public long getPower() {
        return power;
    }

    public List<HbmIngredient> getItemInputs() {
        return itemInputs;
    }

    public List<List<ItemStack>> getDisplayItemInputs() {
        return itemInputs.stream()
                .map(HbmIngredient::displayStacks)
                .toList();
    }

    public List<HbmFluidStack> getFluidInputs() {
        return fluidInputs;
    }

    public List<ItemStack> getItemOutputs() {
        return itemOutputs.stream()
                .map(HbmItemOutput::representativeStack)
                .toList();
    }

    public List<HbmItemOutput> getItemOutputEntries() {
        return itemOutputs;
    }

    public List<List<ItemStack>> getDisplayItemOutputs() {
        return itemOutputs.stream()
                .map(HbmItemOutput::displayStacks)
                .toList();
    }

    public List<HbmFluidStack> getFluidOutputs() {
        return fluidOutputs;
    }

    public List<String> getPools() {
        return pools;
    }

    public GenericMachineRecipeExtraData getExtraData() {
        return extraData;
    }

    public ItemStack getIcon() {
        if (!icon.isEmpty()) {
            return icon.copy();
        }
        if (!itemOutputs.isEmpty()) {
            return itemOutputs.get(0).representativeStack();
        }
        return getToastSymbol();
    }

    public Component getDisplayName() {
        Component name = customLocalization
                ? Component.translatableWithFallback(internalName, getIcon().getHoverName().getString())
                : getIcon().getHoverName();
        if (nameWrapper != null) {
            return Component.translatableWithFallback(nameWrapper, "%s", name);
        }
        return name;
    }

    public List<Component> getDisplayLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(getDisplayName().copy().withStyle(ChatFormatting.YELLOW));
        if (autoSwitchGroup != null) {
            lines.add(Component.translatableWithFallback("autoswitch", "Auto-switch: %s",
                    Component.translatable(autoSwitchGroup)).withStyle(ChatFormatting.GOLD));
        }
        if (duration > 0) {
            lines.add(Component.translatableWithFallback("gui.recipe.duration", "Duration")
                    .append(Component.literal(": " + secondsLabel(duration))).withStyle(ChatFormatting.RED));
        }
        if (power > 0) {
            lines.add(Component.translatableWithFallback("gui.recipe.consumption", "Consumption")
                    .append(Component.literal(": " + power + "HE/t")).withStyle(ChatFormatting.RED));
        }
        addExtraDisplayLines(lines);

        lines.add(Component.translatableWithFallback("gui.recipe.input", "Input")
                .append(Component.literal(":")).withStyle(ChatFormatting.BOLD));
        for (HbmIngredient input : itemInputs) {
            lines.add(Component.literal("  ").append(inputDisplayName(input)).withStyle(ChatFormatting.GRAY));
        }
        for (HbmFluidStack fluid : fluidInputs) {
            lines.add(Component.literal("  ").append(fluidDisplayName(fluid)).withStyle(ChatFormatting.BLUE));
        }

        lines.add(Component.translatableWithFallback("gui.recipe.output", "Output")
                .append(Component.literal(":")).withStyle(ChatFormatting.BOLD));
        for (HbmItemOutput output : itemOutputs) {
            for (String label : output.displayLabels()) {
                lines.add(Component.literal("  " + label).withStyle(ChatFormatting.GRAY));
            }
        }
        for (HbmFluidStack fluid : fluidOutputs) {
            lines.add(Component.literal("  ").append(fluidDisplayName(fluid)).withStyle(ChatFormatting.BLUE));
        }
        return List.copyOf(lines);
    }

    public String getSearchText() {
        StringBuilder builder = new StringBuilder();
        appendSearch(builder, internalName);
        appendSearch(builder, id.toString());
        appendSearch(builder, getDisplayName().getString());
        pools.forEach(pool -> appendSearch(builder, pool));
        if (autoSwitchGroup != null) {
            appendSearch(builder, autoSwitchGroup);
        }
        extraData.plasmaForge().ifPresent(plasma -> appendSearch(builder, Long.toString(plasma.ignitionTemp())));
        extraData.fusion().ifPresent(fusion -> {
            appendSearch(builder, Long.toString(fusion.ignitionTemp()));
            appendSearch(builder, Long.toString(fusion.outputTemp()));
            appendSearch(builder, Double.toString(fusion.outputFlux()));
        });
        for (HbmIngredient input : itemInputs) {
            appendSearch(builder, input.legacyOreName());
            input.displayStacks().forEach(stack -> appendSearch(builder, stack.getHoverName().getString()));
        }
        fluidInputs.forEach(fluid -> appendSearch(builder, fluid.type().getDisplayName().getString()));
        for (HbmItemOutput output : itemOutputs) {
            output.displayOptions().forEach(option -> appendSearch(builder, option.stack().getHoverName().getString()));
        }
        fluidOutputs.forEach(fluid -> appendSearch(builder, fluid.type().getDisplayName().getString()));
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    public boolean matchesSearch(String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        return getSearchText().contains(query.toLowerCase(Locale.ROOT));
    }

    public boolean hasCustomLocalization() {
        return customLocalization;
    }

    private void addExtraDisplayLines(List<Component> lines) {
        extraData.plasmaForge().ifPresent(plasma -> lines.add(
                Component.translatableWithFallback("gui.recipe.plasmaIn", "Plasma input")
                        .append(Component.literal(": " + plasma.ignitionTemp() + "TU/t"))
                        .withStyle(ChatFormatting.LIGHT_PURPLE)));
        extraData.fusion().ifPresent(fusion -> {
            lines.add(Component.translatableWithFallback("gui.recipe.fusionIn", "Fusion input")
                    .append(Component.literal(": " + fusion.ignitionTemp() + "KyU/t"))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            lines.add(Component.translatableWithFallback("gui.recipe.fusionOut", "Fusion output")
                    .append(Component.literal(": " + fusion.outputTemp() + "TU/t"))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            lines.add(Component.translatableWithFallback("gui.recipe.fusionFlux", "Fusion flux")
                    .append(Component.literal(": " + Math.floor(fusion.outputFlux() * 10.0D) / 10.0D + " flux/t"))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        });
    }

    @Nullable
    public String getAutoSwitchGroup() {
        return autoSwitchGroup;
    }

    @Nullable
    public String getNameWrapper() {
        return nameWrapper;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.get(0).representativeStack();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.get(0).representativeStack();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        itemInputs.forEach(input -> ingredients.add(input.ingredient()));
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return switch (machine) {
            case ASSEMBLY_MACHINE -> new ItemStack(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get());
            case CHEMICAL_PLANT -> new ItemStack(ModBlocks.MACHINE_CHEMICAL_PLANT.get());
            case PUREX -> new ItemStack(ModBlocks.MACHINE_PUREX.get());
            case PRECASS -> new ItemStack(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get());
        };
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

    public enum Machine {
        CHEMICAL_PLANT(3, 3, 3, 3),
        ASSEMBLY_MACHINE(12, 1, 1, 1),
        PUREX(3, 3, 6, 1),
        PRECASS(9, 1, 9, 1);

        private final int inputItemLimit;
        private final int inputFluidLimit;
        private final int outputItemLimit;
        private final int outputFluidLimit;

        Machine(int inputItemLimit, int inputFluidLimit, int outputItemLimit, int outputFluidLimit) {
            this.inputItemLimit = inputItemLimit;
            this.inputFluidLimit = inputFluidLimit;
            this.outputItemLimit = outputItemLimit;
            this.outputFluidLimit = outputFluidLimit;
        }

        public int inputItemLimit() {
            return inputItemLimit;
        }

        public int inputFluidLimit() {
            return inputFluidLimit;
        }

        public int outputItemLimit() {
            return outputItemLimit;
        }

        public int outputFluidLimit() {
            return outputFluidLimit;
        }

        public ResourceLocation serializerId() {
            return switch (this) {
                case CHEMICAL_PLANT -> ModRecipes.CHEMICAL_PLANT.serializer().getId();
                case ASSEMBLY_MACHINE -> ModRecipes.ASSEMBLY_MACHINE.serializer().getId();
                case PUREX -> ModRecipes.PUREX.serializer().getId();
                case PRECASS -> ModRecipes.PRECASS.serializer().getId();
            };
        }

        public void validateRecipeLimits(ResourceLocation id, int itemInputs, int fluidInputs, int itemOutputs, int fluidOutputs) {
            validateLimit(id, "item inputs", itemInputs, inputItemLimit);
            validateLimit(id, "fluid inputs", fluidInputs, inputFluidLimit);
            validateLimit(id, "item outputs", itemOutputs, outputItemLimit);
            validateLimit(id, "fluid outputs", fluidOutputs, outputFluidLimit);
        }

        public RecipeType<GenericMachineRecipe> type() {
            return switch (this) {
                case CHEMICAL_PLANT -> ModRecipes.CHEMICAL_PLANT.type().get();
                case ASSEMBLY_MACHINE -> ModRecipes.ASSEMBLY_MACHINE.type().get();
                case PUREX -> ModRecipes.PUREX.type().get();
                case PRECASS -> ModRecipes.PRECASS.type().get();
            };
        }

        public RecipeSerializer<GenericMachineRecipe> serializer() {
            return switch (this) {
                case CHEMICAL_PLANT -> ModRecipes.CHEMICAL_PLANT.serializer().get();
                case ASSEMBLY_MACHINE -> ModRecipes.ASSEMBLY_MACHINE.serializer().get();
                case PUREX -> ModRecipes.PUREX.serializer().get();
                case PRECASS -> ModRecipes.PRECASS.serializer().get();
            };
        }

        private static void validateLimit(ResourceLocation id, String label, int actual, int limit) {
            if (limit >= 0 && actual > limit) {
                throw new JsonSyntaxException("HBM machine recipe " + id + " has too many " + label
                        + ": " + actual + " > " + limit);
            }
        }
    }

    private static Component inputDisplayName(HbmIngredient input) {
        List<ItemStack> stacks = input.displayStacks();
        if (!stacks.isEmpty()) {
            ItemStack stack = stacks.get(0);
            return Component.literal(input.count() + "x ").append(stack.getHoverName());
        }
        if (input.legacyOreName() != null) {
            return Component.literal(input.count() + "x " + input.legacyOreName());
        }
        return Component.literal(input.count() + "x " + input.ingredient().toJson());
    }

    private static Component fluidDisplayName(HbmFluidStack fluid) {
        Component base = Component.literal(fluid.amount() + "mB ").append(fluid.type().getDisplayName());
        if (fluid.pressure() != 0) {
            base = base.copy()
                    .append(Component.literal(" "))
                    .append(Component.translatableWithFallback("gui.recipe.atPressure", "at pressure"))
                    .append(Component.literal(" " + fluid.pressure() + " PU").withStyle(ChatFormatting.RED));
        }
        return base;
    }

    private static String secondsLabel(int ticks) {
        double seconds = ticks / 20.0D;
        if (seconds == Math.rint(seconds)) {
            return (int) seconds + "s";
        }
        return String.format(Locale.US, "%.2fs", seconds);
    }

    private static void appendSearch(StringBuilder builder, @Nullable String value) {
        if (value != null && !value.isBlank()) {
            builder.append(' ').append(value);
        }
    }

    public static class Serializer implements RecipeSerializer<GenericMachineRecipe> {
        private final Machine machine;

        public Serializer(Machine machine) {
            this.machine = machine;
        }

        @Override
        public GenericMachineRecipe fromJson(ResourceLocation id, JsonObject json) {
            String internalName = LegacyGenericRecipeFormat.internalName(id, json);
            int duration = json.has("duration") ? json.get("duration").getAsInt() : 0;
            long power = json.has("power") ? json.get("power").getAsLong() : 0L;
            List<HbmIngredient> itemInputs = LegacyGenericRecipeFormat.readItemInputs(json);
            List<HbmFluidStack> fluidInputs = LegacyGenericRecipeFormat.readFluidInputs(json);
            List<HbmItemOutput> itemOutputs = LegacyGenericRecipeFormat.readItemOutputs(json);
            List<HbmFluidStack> fluidOutputs = LegacyGenericRecipeFormat.readFluidOutputs(json);
            List<String> pools = LegacyGenericRecipeFormat.readPools(json);
            ItemStack icon = LegacyGenericRecipeFormat.readIcon(json);
            boolean customLocalization = LegacyGenericRecipeFormat.readCustomLocalization(json);
            String autoSwitchGroup = LegacyGenericRecipeFormat.readAutoSwitchGroup(json);
            String nameWrapper = LegacyGenericRecipeFormat.readNameWrapper(json);
            GenericMachineRecipeExtraData extraData = GenericMachineRecipeExtraData.fromJson(json);
            machine.validateRecipeLimits(id, itemInputs.size(), fluidInputs.size(), itemOutputs.size(), fluidOutputs.size());
            validateItemInputStackLimits(id, itemInputs);
            return new GenericMachineRecipe(id, machine, internalName, duration, power, itemInputs, fluidInputs, itemOutputs, fluidOutputs,
                    pools, icon, customLocalization, extraData, autoSwitchGroup, nameWrapper);
        }

        private static void validateItemInputStackLimits(ResourceLocation id, List<HbmIngredient> itemInputs) {
            for (HbmIngredient input : itemInputs) {
                if (input.exceedsStackLimit()) {
                    int limit = input.stackLimit().orElse(64);
                    throw new JsonSyntaxException("HBM machine recipe " + id + " input count exceeds stack limit: "
                            + input.count() + " > " + limit);
                }
            }
        }

        @Nullable
        @Override
        public GenericMachineRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            String internalName = buffer.readUtf();
            int duration = buffer.readVarInt();
            long power = buffer.readVarLong();
            List<HbmIngredient> itemInputs = buffer.readList(HbmIngredient::fromNetwork);
            List<HbmFluidStack> fluidInputs = buffer.readList(Serializer::readFluidStack);
            List<HbmItemOutput> itemOutputs = buffer.readList(HbmItemOutput::fromNetwork);
            List<HbmFluidStack> fluidOutputs = buffer.readList(Serializer::readFluidStack);
            List<String> pools = buffer.readList(FriendlyByteBuf::readUtf);
            ItemStack icon = buffer.readItem();
            boolean customLocalization = buffer.readBoolean();
            GenericMachineRecipeExtraData extraData = GenericMachineRecipeExtraData.fromNetwork(buffer);
            String autoSwitchGroup = buffer.readBoolean() ? buffer.readUtf() : null;
            String nameWrapper = buffer.readBoolean() ? buffer.readUtf() : null;
            return new GenericMachineRecipe(id, machine, internalName, duration, power, itemInputs, fluidInputs, itemOutputs, fluidOutputs,
                    pools, icon, customLocalization, extraData, autoSwitchGroup, nameWrapper);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GenericMachineRecipe recipe) {
            buffer.writeUtf(recipe.internalName);
            buffer.writeVarInt(recipe.duration);
            buffer.writeVarLong(recipe.power);
            buffer.writeCollection(recipe.itemInputs, (output, input) -> input.toNetwork(output));
            buffer.writeCollection(recipe.fluidInputs, Serializer::writeFluidStack);
            buffer.writeCollection(recipe.itemOutputs, (output, itemOutput) -> itemOutput.toNetwork(output));
            buffer.writeCollection(recipe.fluidOutputs, Serializer::writeFluidStack);
            buffer.writeCollection(recipe.pools, FriendlyByteBuf::writeUtf);
            buffer.writeItem(recipe.icon);
            buffer.writeBoolean(recipe.customLocalization);
            recipe.extraData.toNetwork(buffer);
            buffer.writeBoolean(recipe.autoSwitchGroup != null);
            if (recipe.autoSwitchGroup != null) {
                buffer.writeUtf(recipe.autoSwitchGroup);
            }
            buffer.writeBoolean(recipe.nameWrapper != null);
            if (recipe.nameWrapper != null) {
                buffer.writeUtf(recipe.nameWrapper);
            }
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
