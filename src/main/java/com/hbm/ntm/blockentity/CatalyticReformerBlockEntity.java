package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidRecipeIO;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidPortLayouts.LegacyPort;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.LegacyFluidTankPacket;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.TripleRecipe;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

public class CatalyticReformerBlockEntity extends LegacyRemoteFluidMachineBlockEntity {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT_CONTAINER = 1;
    public static final int SLOT_INPUT_CONTAINER_OUTPUT = 2;
    public static final int SLOT_OUTPUT_1_CONTAINER = 3;
    public static final int SLOT_OUTPUT_1_CONTAINER_OUTPUT = 4;
    public static final int SLOT_OUTPUT_2_CONTAINER = 5;
    public static final int SLOT_OUTPUT_2_CONTAINER_OUTPUT = 6;
    public static final int SLOT_OUTPUT_3_CONTAINER = 7;
    public static final int SLOT_OUTPUT_3_CONTAINER_OUTPUT = 8;
    public static final int SLOT_IDENTIFIER = 9;
    public static final int SLOT_CATALYST = 10;
    public static final int ITEM_COUNT = 11;
    private static final long MAX_POWER = 1_000_000L;
    private static final long POWER_PER_OPERATION = 20_000L;

    private final HbmFluidTank inputTank;
    private final HbmFluidTank reformateTank;
    private final HbmFluidTank petroleumTank;
    private final HbmFluidTank hydrogenTank;

    public CatalyticReformerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                tank(HbmFluids.NAPHTHA, 64_000),
                tank(HbmFluids.REFORMATE, 24_000),
                tank(HbmFluids.PETROLEUM, 24_000),
                tank(HbmFluids.HYDROGEN, 24_000));
    }

    private CatalyticReformerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank reformateTank, HbmFluidTank petroleumTank, HbmFluidTank hydrogenTank) {
        super(ModBlockEntities.CATALYTIC_REFORMER.get(), pos, state, MAX_POWER,
                List.of(inputTank, reformateTank, petroleumTank, hydrogenTank),
                List.of(inputTank),
                List.of(reformateTank, petroleumTank, hydrogenTank),
                true, ITEM_COUNT);
        this.inputTank = inputTank;
        this.reformateTank = reformateTank;
        this.petroleumTank = petroleumTank;
        this.hydrogenTank = hydrogenTank;
    }

    @Override
    protected int legacyNetworkPackRange() {
        return 150;
    }

    @Override
    public LegacyGuiProfile getLegacyGuiProfile() {
        return LegacyGuiProfile.CATALYTIC_REFORMER;
    }

    @Override
    protected String legacyContainerKey() {
        return "container.catalyticReformer";
    }

    @Override
    protected boolean tickLegacyMachine(Level level, BlockPos pos, BlockState state) {
        boolean changed = setInputTypeFromIdentifier();
        changed |= processFluidContainers();
        chargeFromSlot(SLOT_BATTERY);
        changed |= reform();
        return changed;
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_BATTERY, SLOT_INPUT_CONTAINER, SLOT_OUTPUT_1_CONTAINER, SLOT_OUTPUT_2_CONTAINER,
                 SLOT_OUTPUT_3_CONTAINER, SLOT_IDENTIFIER, SLOT_CATALYST -> true;
            default -> false;
        };
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return HbmFluidPortLayouts.legacy(facing, rot,
                LegacyPort.of(2, 1, facing),
                LegacyPort.of(2, -1, facing),
                LegacyPort.of(-2, 1, facing.getOpposite()),
                LegacyPort.of(-2, -1, facing.getOpposite()),
                LegacyPort.of(0, 3, rot),
                LegacyPort.of(0, -3, rot.getOpposite()));
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                EnergyPort.of(facing.getStepX() * 2 + rot.getStepX(), 0,
                        facing.getStepZ() * 2 + rot.getStepZ(), facing),
                EnergyPort.of(facing.getStepX() * 2 - rot.getStepX(), 0,
                        facing.getStepZ() * 2 - rot.getStepZ(), facing),
                EnergyPort.of(-facing.getStepX() * 2 + rot.getStepX(), 0,
                        -facing.getStepZ() * 2 + rot.getStepZ(), facing.getOpposite()),
                EnergyPort.of(-facing.getStepX() * 2 - rot.getStepX(), 0,
                        -facing.getStepZ() * 2 - rot.getStepZ(), facing.getOpposite()),
                EnergyPort.of(rot.getStepX() * 3, 0, rot.getStepZ() * 3, rot),
                EnergyPort.of(-rot.getStepX() * 3, 0, -rot.getStepZ() * 3, rot.getOpposite()));
    }

    @Override
    protected void refreshFluidPorts() {
        // TileEntityMachineCatalyticReformer runs updateConnections() every 20
        // ticks, but sends its three output tanks every server tick.
        if (level != null && level.getGameTime() % 20L == 0L) {
            refreshTrackedReceiverFluidPorts(getReceivingTanks(), this);
        }
        refreshTrackedProviderFluidPorts(getSendingTanks(), this);
    }

    private boolean setInputTypeFromIdentifier() {
        ItemStackHandler items = getItems();
        return items != null && setFluidTankTypeFromIdentifierSlot(items, SLOT_IDENTIFIER, inputTank);
    }

    private boolean processFluidContainers() {
        ItemStackHandler items = getItems();
        return items != null
                && (processFluidItemLoadTransfer(items, SLOT_INPUT_CONTAINER, SLOT_INPUT_CONTAINER_OUTPUT, inputTank)
                | processFluidItemUnloadTransfers(items, SLOT_OUTPUT_1_CONTAINER,
                        SLOT_OUTPUT_1_CONTAINER_OUTPUT, 2, reformateTank, petroleumTank, hydrogenTank));
    }

    private boolean reform() {
        TripleRecipe recipe = LegacyOilFluidRecipes.getReforming(level, inputTank.getTankType());
        boolean changed = setupRecipeTanks(recipe);
        if (recipe == null || !hasCatalyst()) {
            return changed;
        }
        if (energy.getPower() < POWER_PER_OPERATION) {
            return changed;
        }
        if (!HbmFluidRecipeIO.canConsumeInput(inputTank, 100)
                || !HbmFluidRecipeIO.canProduceOutput(reformateTank, recipe.first())
                || !HbmFluidRecipeIO.canProduceOutput(petroleumTank, recipe.second())
                || !HbmFluidRecipeIO.canProduceOutput(hydrogenTank, recipe.third())) {
            return changed;
        }
        HbmFluidRecipeIO.consumeInput(inputTank, 100, false);
        HbmFluidRecipeIO.produceOutput(reformateTank, recipe.first(), false);
        HbmFluidRecipeIO.produceOutput(petroleumTank, recipe.second(), false);
        HbmFluidRecipeIO.produceOutput(hydrogenTank, recipe.third(), false);
        consumePower(POWER_PER_OPERATION);
        onFluidContentsChanged();
        return true;
    }

    private boolean setupRecipeTanks(TripleRecipe recipe) {
        if (recipe == null) {
            return HbmFluidRecipeIO.conformTankChanged(reformateTank, null, 0)
                    | HbmFluidRecipeIO.conformTankChanged(petroleumTank, null, 0)
                    | HbmFluidRecipeIO.conformTankChanged(hydrogenTank, null, 0);
        }
        return HbmFluidRecipeIO.conformTankChanged(reformateTank, recipe.first(), 0)
                | HbmFluidRecipeIO.conformTankChanged(petroleumTank, recipe.second(), 0)
                | HbmFluidRecipeIO.conformTankChanged(hydrogenTank, recipe.third(), 0);
    }

    private boolean hasCatalyst() {
        ItemStackHandler items = getItems();
        return items != null && items.getStackInSlot(SLOT_CATALYST).is(ModItems.CATALYTIC_CONVERTER.get());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("power", energy.getPower());
        inputTank.writeToNbt(tag, "input");
        reformateTank.writeToNbt(tag, "o1");
        petroleumTank.writeToNbt(tag, "o2");
        hydrogenTank.writeToNbt(tag, "o3");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("power")) {
            energy.setPower(tag.getLong("power"));
        }
        if (tag.contains("input")) {
            inputTank.readFromNbt(tag, "input");
        }
        if (tag.contains("o1")) {
            reformateTank.readFromNbt(tag, "o1");
        }
        if (tag.contains("o2")) {
            petroleumTank.readFromNbt(tag, "o2");
        }
        if (tag.contains("o3")) {
            hydrogenTank.readFromNbt(tag, "o3");
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        // TileEntityMachineCatalyticReformer#serialize: MachineBase/LoadedBase,
        // current power, then its four process tanks.
        writeLegacyLoadedTileBinary(data);
        data.writeLong(energy.getPower());
        LegacyFluidTankPacket.write(data, inputTank);
        LegacyFluidTankPacket.write(data, reformateTank);
        LegacyFluidTankPacket.write(data, petroleumTank);
        LegacyFluidTankPacket.write(data, hydrogenTank);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        energy.setPower(data.readLong());
        LegacyFluidTankPacket.read(data, inputTank);
        LegacyFluidTankPacket.read(data, reformateTank);
        LegacyFluidTankPacket.read(data, petroleumTank);
        LegacyFluidTankPacket.read(data, hydrogenTank);
    }
}
