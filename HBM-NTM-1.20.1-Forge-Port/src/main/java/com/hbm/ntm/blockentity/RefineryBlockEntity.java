package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RefineryBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmPersistentBlockState, HbmStandardFluidTransceiver {
    private static final String TAG_SULFUR = "sulfur";
    private static final String TAG_EXPLODED = "exploded";
    private static final String TAG_ON_FIRE = "onFire";

    private boolean exploded;
    private boolean onFire;
    private int sulfur;
    private Explosion lastExplosion;

    public RefineryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REFINERY.get(), pos, state, new HbmEnergyStorage(1_000_000L, 1_000_000L, 0L),
                List.of(
                        new HbmFluidTank(HbmFluids.HOTOIL, 64_000),
                        new HbmFluidTank(HbmFluids.HEAVYOIL, 24_000),
                        new HbmFluidTank(HbmFluids.NAPHTHA, 24_000),
                        new HbmFluidTank(HbmFluids.LIGHTOIL, 24_000),
                        new HbmFluidTank(HbmFluids.PETROLEUM, 24_000)));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RefineryBlockEntity refinery) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, refinery);
        boolean changed = false;
        if (refinery.exploded && refinery.onFire) {
            changed = refinery.burnResidualFluid();
        }
        if (changed) {
            refinery.setChanged();
        }
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return exploded ? List.of() : List.of(getAllTanks().get(0));
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return exploded ? List.of() : getAllTanks().subList(1, 5);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        if (exploded || side == Direction.DOWN) {
            return HbmFluidSideMode.NONE;
        }
        return HbmFluidSideMode.BOTH;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return exploded ? List.of() : List.of(getAllTanks().get(0));
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return exploded ? List.of() : getAllTanks().subList(1, 5);
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return !exploded && type == getAllTanks().get(0).getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return !exploded && getSendingTanks().stream()
                .anyMatch(tank -> tank.getTankType() == type && tank.getFill() > 0);
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return List.of(
                FluidPort.of(2, 0, 1, Direction.EAST),
                FluidPort.of(2, 0, -1, Direction.EAST),
                FluidPort.of(-2, 0, 1, Direction.WEST),
                FluidPort.of(-2, 0, -1, Direction.WEST),
                FluidPort.of(1, 0, 2, Direction.SOUTH),
                FluidPort.of(-1, 0, 2, Direction.SOUTH),
                FluidPort.of(1, 0, -2, Direction.NORTH),
                FluidPort.of(-1, 0, -2, Direction.NORTH));
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidTransceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidTransceiver.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return exploded ? HbmEnergySideMode.NONE : HbmEnergySideMode.INPUT;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return !exploded && side != null && side != Direction.DOWN
                && type != null && getFluidNodeTypes().contains(type);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_SULFUR, sulfur);
        tag.putBoolean(TAG_EXPLODED, exploded);
        tag.putBoolean(TAG_ON_FIRE, onFire);
        getAllTanks().get(0).writeToNbt(tag, "input");
        getAllTanks().get(1).writeToNbt(tag, "heavy");
        getAllTanks().get(2).writeToNbt(tag, "naphtha");
        getAllTanks().get(3).writeToNbt(tag, "light");
        getAllTanks().get(4).writeToNbt(tag, "petroleum");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        sulfur = tag.getInt(TAG_SULFUR);
        exploded = tag.getBoolean(TAG_EXPLODED);
        onFire = tag.getBoolean(TAG_ON_FIRE);
        getAllTanks().get(0).readFromNbt(tag, "input");
        getAllTanks().get(1).readFromNbt(tag, "heavy");
        getAllTanks().get(2).readFromNbt(tag, "naphtha");
        getAllTanks().get(3).readFromNbt(tag, "light");
        getAllTanks().get(4).readFromNbt(tag, "petroleum");
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
        if (getAllTanks().stream().allMatch(tank -> tank.getFill() == 0) && !exploded) {
            return;
        }
        HbmPersistentBlockState.writeIndexedTanks(persistent, getAllTanks());
        persistent.putBoolean("hasExploded", exploded);
        persistent.putBoolean(TAG_ON_FIRE, onFire);
    }

    @Override
    public void readPersistentState(CompoundTag persistent) {
        HbmPersistentBlockState.readIndexedTanks(persistent, getAllTanks());
        exploded = persistent.getBoolean("hasExploded");
        onFire = persistent.getBoolean(TAG_ON_FIRE);
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

    public boolean isExploded() {
        return exploded;
    }

    public boolean isOnFire() {
        return onFire;
    }

    public boolean hasStoredFluid() {
        return getAllTanks().stream().anyMatch(tank -> tank.getFill() > 0);
    }

    public void explode() {
        if (exploded) {
            return;
        }
        exploded = true;
        onFire = true;
        invalidateFluidHandlers();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public boolean markExplosionHandled(Explosion explosion) {
        if (lastExplosion == explosion) {
            return false;
        }
        lastExplosion = explosion;
        return true;
    }

    public void tryExtinguish(ExtinguishType type) {
        if (!exploded || !onFire) {
            return;
        }
        if (type == ExtinguishType.FOAM || type == ExtinguishType.CO2) {
            onFire = false;
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
            return;
        }
        if (type == ExtinguishType.WATER && level != null && hasStoredFluid()) {
            level.explode(null, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.5D,
                    worldPosition.getZ() + 0.5D, 5.0F, Level.ExplosionInteraction.TNT);
        }
    }

    public void repair() {
        exploded = false;
        invalidateFluidHandlers();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private boolean burnResidualFluid() {
        boolean changed = false;
        for (HbmFluidTank tank : getAllTanks()) {
            int fill = tank.getFill();
            if (fill > 0) {
                tank.setFill(Math.max(fill - 10, 0));
                changed = true;
            }
        }
        return changed;
    }

    public enum ExtinguishType {
        WATER,
        FOAM,
        CO2
    }
}
