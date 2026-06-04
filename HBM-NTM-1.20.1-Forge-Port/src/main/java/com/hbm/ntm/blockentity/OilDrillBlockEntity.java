package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class OilDrillBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmPersistentBlockState, HbmStandardFluidSender {
    public OilDrillBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OIL_DRILL.get(), pos, state, oilDrillEnergy(state),
                List.of(tank(HbmFluids.OIL, 64_000), tank(HbmFluids.GAS, 64_000)));
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state,
            OilDrillBlockEntity drill) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, drill);
        if (level.getGameTime() % 20L == 0L) {
            drill.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return getAllTanks();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == null ? HbmFluidSideMode.BOTH : HbmFluidSideMode.OUTPUT;
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return getAllTanks();
    }

    @Override
    public HbmFluidTank getTankToPasteFluidSettings() {
        return null;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type != HbmFluids.NONE && getAllTanks().stream()
                .anyMatch(tank -> tank.getTankType() == type && tank.getFill() > 0);
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        BlockState state = getBlockState();
        if (state.is(ModBlocks.MACHINE_PUMPJACK.get())) {
            Direction dir = facing();
            Direction rot = rotatedFacing();
            return List.of(
                    new FluidPort(offset(rot, 2).offset(offset(dir, 2)), dir),
                    new FluidPort(offset(rot, 2).offset(offset(dir.getOpposite(), 2)), dir.getOpposite()),
                    new FluidPort(offset(rot, 4).offset(offset(dir.getOpposite(), 2)), dir),
                    new FluidPort(offset(rot, 4).offset(offset(dir, 2)), dir.getOpposite()));
        }
        return List.of(
                FluidPort.of(1, 0, 0, Direction.EAST),
                FluidPort.of(-1, 0, 0, Direction.WEST),
                FluidPort.of(0, 0, 1, Direction.SOUTH),
                FluidPort.of(0, 0, -1, Direction.NORTH));
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        getAllTanks().get(0).writeToNbt(tag, "t0");
        getAllTanks().get(1).writeToNbt(tag, "t1");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        getAllTanks().get(0).readFromNbt(tag, "t0");
        getAllTanks().get(1).readFromNbt(tag, "t1");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void writePersistentState(CompoundTag persistent) {
        boolean empty = getPower() == 0L;
        for (HbmFluidTank tank : getAllTanks()) {
            if (tank.getFill() > 0) {
                empty = false;
            }
        }
        if (empty) {
            return;
        }
        persistent.putLong("power", getPower());
        getAllTanks().get(0).writeToNbt(persistent, "t0");
        getAllTanks().get(1).writeToNbt(persistent, "t1");
    }

    @Override
    public void readPersistentState(CompoundTag persistent) {
        energy.setPower(persistent.getLong("power"));
        getAllTanks().get(0).readFromNbt(persistent, "t0");
        getAllTanks().get(1).readFromNbt(persistent, "t1");
        invalidateFluidHandlers();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    public ItemStack createPersistentBlockDrop(Item item) {
        ItemStack stack = new ItemStack(item);
        writePersistentStateToStack(stack);
        return stack;
    }

    public boolean hasStoredFluid() {
        return getAllTanks().stream().anyMatch(tank -> tank.getFill() > 0);
    }

    public void clearStoredFluids() {
        for (HbmFluidTank tank : getAllTanks()) {
            tank.setFill(0);
        }
        onFluidContentsChanged();
    }

    private static HbmFluidTank tank(FluidType type, int capacity) {
        return new HbmFluidTank(type, capacity);
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                ? state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private Direction rotatedFacing() {
        return com.hbm.ntm.multiblock.LegacyMultiblockOffsets.legacyUpSide(facing());
    }

    private static BlockPos offset(Direction direction, int amount) {
        return new BlockPos(
                direction.getStepX() * amount,
                direction.getStepY() * amount,
                direction.getStepZ() * amount);
    }

    private static HbmEnergyStorage oilDrillEnergy(BlockState state) {
        long maxPower = 100_000L;
        if (state.is(ModBlocks.MACHINE_PUMPJACK.get())) {
            maxPower = 250_000L;
        } else if (state.is(ModBlocks.MACHINE_FRACKING_TOWER.get())) {
            maxPower = 5_000_000L;
        }
        return new HbmEnergyStorage(maxPower, maxPower, 0L);
    }
}
