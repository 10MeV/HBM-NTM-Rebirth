package com.hbm.ntm.compat.jei;

import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.item.FluidIconItem;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.item.ItemBlueprints;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.util.BobMathUtil;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class HbmMachineRecipeCategory implements IRecipeCategory<GenericMachineRecipe> {
    private enum DisplayMode {
        DEFAULT,
        LEGACY_UNIVERSAL,
        ARC_FURNACE_SOLID,
        ARC_FURNACE_FLUID;

        boolean usesLegacyUniversalLayout() {
            return this == LEGACY_UNIVERSAL || this == ARC_FURNACE_SOLID || this == ARC_FURNACE_FLUID;
        }
    }

    private final RecipeType<GenericMachineRecipe> type;
    private final GenericMachineRecipe.Machine machine;
    private final DisplayMode displayMode;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final IDrawableStatic machineWithTemplateBackground;
    private final List<ItemStack> machineStacks;
    private final int width;
    private final int height;

    HbmMachineRecipeCategory(RecipeType<GenericMachineRecipe> type, GenericMachineRecipe.Machine machine,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this(type, machine, catalyst, guiHelper, machine == GenericMachineRecipe.Machine.ARC_WELDER
                ? DisplayMode.LEGACY_UNIVERSAL
                : DisplayMode.DEFAULT);
    }

    static HbmMachineRecipeCategory arcFurnaceSolid(RecipeType<GenericMachineRecipe> type, ItemLike catalyst,
            IGuiHelper guiHelper) {
        return new HbmMachineRecipeCategory(type, GenericMachineRecipe.Machine.ARC_FURNACE, catalyst, guiHelper,
                DisplayMode.ARC_FURNACE_SOLID);
    }

    static HbmMachineRecipeCategory arcFurnaceFluid(RecipeType<GenericMachineRecipe> type, ItemLike catalyst,
            IGuiHelper guiHelper) {
        return new HbmMachineRecipeCategory(type, GenericMachineRecipe.Machine.ARC_FURNACE, catalyst, guiHelper,
                DisplayMode.ARC_FURNACE_FLUID);
    }

    private HbmMachineRecipeCategory(RecipeType<GenericMachineRecipe> type, GenericMachineRecipe.Machine machine,
            ItemLike catalyst, IGuiHelper guiHelper, DisplayMode displayMode) {
        this.type = type;
        this.machine = machine;
        this.displayMode = displayMode;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.machineStacks = machineStacks(machine, catalyst);
        this.width = LegacyNeiUniversalLayout.WIDTH;
        this.height = LegacyNeiUniversalLayout.HEIGHT;
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.machineWithTemplateBackground = LegacyNeiUniversalLayout.machineWithTemplateBackground(guiHelper);
    }

    @Override
    public RecipeType<GenericMachineRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return switch (machine) {
            case ASSEMBLY_MACHINE -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_assembly_machine", "Assembly Machine");
            case CHEMICAL_PLANT -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_chemical_plant", "Chemical Plant");
            case PUREX -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_purex", "PUREX");
            case PRECASS -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_precass", "Precision Assembly Machine");
            case ARC_WELDER -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_arc_welder", "Arc Welder");
            case ARC_FURNACE -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_arc_furnace", "Electric Arc Furnace");
            case FUSION_REACTOR -> Component.translatableWithFallback("container.fusionTorus", "Fusion Reactor Vessel");
            case PLASMA_FORGE -> Component.translatableWithFallback("container.machinePlasmaForge", "Plasma Forge");
            default -> Component.literal(machine.name());
        };
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GenericMachineRecipe recipe, IFocusGroup focuses) {
        if (displayMode == DisplayMode.LEGACY_UNIVERSAL) {
            setArcWelderRecipe(builder, recipe);
            return;
        }
        if (displayMode == DisplayMode.ARC_FURNACE_SOLID) {
            setArcFurnaceRecipe(builder, recipe, false);
            return;
        }
        if (displayMode == DisplayMode.ARC_FURNACE_FLUID) {
            setArcFurnaceRecipe(builder, recipe, true);
            return;
        }

        setGenericRecipe(builder, recipe);
    }

    private void setGenericRecipe(IRecipeLayoutBuilder builder, GenericMachineRecipe recipe) {
        List<List<ItemStack>> inputs = itemInputs(recipe);
        for (HbmFluidStack fluid : recipe.getFluidInputs()) {
            if (!fluid.isEmpty()) {
                inputs.add(List.of(FluidIconItem.make(fluid.type(), fluid.amount(), fluid.pressure())));
            }
        }
        addGenericInputSlots(builder, inputs, genericInputXOffset(inputs.size()));

        List<List<ItemStack>> outputs = genericOutputs(recipe);
        addGenericOutputSlots(builder, outputs, genericOutputXOffset(inputs.size()));
        addGenericMachineSlots(builder, recipe, genericMachineXOffset(inputs.size()));
    }

    private void setArcWelderRecipe(IRecipeLayoutBuilder builder, GenericMachineRecipe recipe) {
        List<List<ItemStack>> inputs = itemInputs(recipe);
        for (HbmFluidStack fluid : recipe.getFluidInputs()) {
            if (!fluid.isEmpty()) {
                inputs.add(List.of(FluidIconItem.make(fluid.type(), fluid.amount(), fluid.pressure())));
            }
        }

        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, inputs);
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, recipe.getDisplayItemOutputs());
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, machineStacks);
    }

    private void setArcFurnaceRecipe(IRecipeLayoutBuilder builder, GenericMachineRecipe recipe,
            boolean materialOutputs) {
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, itemInputs(recipe));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground,
                materialOutputs ? arcMaterialOutputs(recipe) : recipe.getDisplayItemOutputs());
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, machineStacks);
    }

    private static List<List<ItemStack>> itemInputs(GenericMachineRecipe recipe) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        for (HbmIngredient input : recipe.getItemInputs()) {
            inputs.add(input.displayStacks());
        }
        return inputs;
    }

    private static List<List<ItemStack>> genericOutputs(GenericMachineRecipe recipe) {
        List<List<ItemStack>> outputs = new ArrayList<>(recipe.getDisplayItemOutputs());
        for (HbmFluidStack fluid : recipe.getFluidOutputs()) {
            if (!fluid.isEmpty()) {
                outputs.add(List.of(FluidIconItem.make(fluid.type(), fluid.amount(), fluid.pressure())));
            }
        }
        return outputs;
    }

    private void addGenericInputSlots(IRecipeLayoutBuilder builder, List<List<ItemStack>> inputs, int xOffset) {
        int[][] coords = genericInputCoords(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            List<ItemStack> stacks = inputs.get(i);
            if (!stacks.isEmpty()) {
                int[] pos = coords[i];
                builder.addInputSlot(pos[0] + xOffset, pos[1])
                        .setBackground(slotBackground, -1, -1)
                        .addItemStacks(stacks);
            }
        }
    }

    private void addGenericOutputSlots(IRecipeLayoutBuilder builder, List<List<ItemStack>> outputs, int xOffset) {
        int[][] coords = genericOutputCoords(outputs.size());
        for (int i = 0; i < outputs.size(); i++) {
            List<ItemStack> stacks = outputs.get(i);
            if (!stacks.isEmpty()) {
                int[] pos = coords[i];
                builder.addOutputSlot(pos[0] + xOffset, pos[1])
                        .setBackground(slotBackground, -1, -1)
                        .addItemStacks(stacks);
            }
        }
    }

    private void addGenericMachineSlots(IRecipeLayoutBuilder builder, GenericMachineRecipe recipe, int xOffset) {
        int x = 75 + xOffset;
        if (recipe.getPools().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, x, 31)
                    .setBackground(machineBackground, -1, -17)
                    .addItemStacks(machineStacks);
            return;
        }

        List<ItemStack> blueprints = recipe.getPools().stream()
                .map(ItemBlueprints::make)
                .toList();
        builder.addSlot(RecipeIngredientRole.CATALYST, x, 10)
                .setBackground(machineWithTemplateBackground, -1, -3)
                .addItemStacks(blueprints);
        builder.addSlot(RecipeIngredientRole.CATALYST, x, 38)
                .addItemStacks(machineStacks);
    }

    private int genericInputXOffset(int inputCount) {
        if (usesAssemblyOffsets()) {
            return inputCount > 12 ? -9 : inputCount > 9 ? 18 : 0;
        }
        return 0;
    }

    private int genericOutputXOffset(int inputCount) {
        return usesAssemblyOffsets() ? genericMachineXOffset(inputCount) : 0;
    }

    private int genericMachineXOffset(int inputCount) {
        if (usesAssemblyOffsets()) {
            if (inputCount > 12) {
                return 27;
            }
            if (inputCount > 9) {
                return 18;
            }
        }
        return 0;
    }

    private boolean usesAssemblyOffsets() {
        return machine == GenericMachineRecipe.Machine.ASSEMBLY_MACHINE
                || machine == GenericMachineRecipe.Machine.PLASMA_FORGE;
    }

    private static int[][] genericInputCoords(int count) {
        if (count == 1) {
            return new int[][] {{48, 24}};
        }
        if (count == 2) {
            return new int[][] {{30, 24}, {48, 24}};
        }
        if (count == 3) {
            return new int[][] {{12, 24}, {30, 24}, {48, 24}};
        }
        if (count == 4) {
            return new int[][] {{30, 15}, {48, 15}, {30, 33}, {48, 33}};
        }
        if (count == 5) {
            return new int[][] {{12, 15}, {30, 15}, {48, 15}, {12, 33}, {30, 33}};
        }
        if (count == 6) {
            return new int[][] {{12, 15}, {30, 15}, {48, 15}, {12, 33}, {30, 33}, {48, 33}};
        }

        int[][] slots = new int[count][2];
        int cols = (count + 2) / 3;
        for (int i = 0; i < count; i++) {
            slots[i][0] = 12 + (i % cols) * 18 - (cols == 4 ? 18 : 0);
            slots[i][1] = 6 + (i / cols) * 18;
        }
        return slots;
    }

    private static int[][] genericOutputCoords(int count) {
        return switch (count) {
            case 1 -> new int[][] {{102, 24}};
            case 2 -> new int[][] {{102, 24}, {120, 24}};
            case 3 -> new int[][] {{102, 24}, {120, 24}, {138, 24}};
            case 4 -> new int[][] {{102, 15}, {120, 15}, {102, 33}, {120, 33}};
            case 5 -> new int[][] {{102, 15}, {120, 15}, {102, 33}, {120, 33}, {138, 24}};
            case 6 -> new int[][] {{102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}};
            case 7 -> new int[][] {{102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}, {138, 24}};
            case 8 -> new int[][] {
                    {102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}, {138, 24}, {138, 42}
            };
            default -> new int[count][2];
        };
    }

    private static List<ItemStack> machineStacks(GenericMachineRecipe.Machine machine, ItemLike catalyst) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(catalyst));
        if (machine == GenericMachineRecipe.Machine.CHEMICAL_PLANT) {
            stacks.add(new ItemStack(ModBlocks.MACHINE_CHEMICAL_FACTORY.get()));
        }
        return List.copyOf(stacks);
    }

    private static List<List<ItemStack>> arcMaterialOutputs(GenericMachineRecipe recipe) {
        List<List<ItemStack>> outputs = new ArrayList<>();
        for (MaterialStack material : recipe.getExtraData().arcMaterialOutputs()) {
            ItemStack stack = FoundryScrapsItem.create(material, true);
            if (!stack.isEmpty()) {
                outputs.add(List.of(stack));
            }
        }
        return outputs;
    }

    @Override
    public void draw(GenericMachineRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (displayMode == DisplayMode.LEGACY_UNIVERSAL) {
            var font = Minecraft.getInstance().font;
            String duration = BobMathUtil.getShortNumber(recipe.getDuration()) + " ticks";
            String consumption = BobMathUtil.getShortNumber(recipe.getPower()) + "HE/t";
            int side = 164;
            guiGraphics.drawString(font, duration, side - font.width(duration), 45, 0x404040, false);
            guiGraphics.drawString(font, consumption, side - font.width(consumption), 57, 0x404040, false);
            return;
        }
        if (displayMode.usesLegacyUniversalLayout()) {
            return;
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, GenericMachineRecipe recipe, IRecipeSlotsView recipeSlotsView,
            double mouseX, double mouseY) {
        if (displayMode == DisplayMode.ARC_FURNACE_SOLID || displayMode == DisplayMode.ARC_FURNACE_FLUID) {
            return;
        }
        if (mouseY >= 54) {
            tooltip.addAll(recipe.getDisplayLines());
        }
    }
}
