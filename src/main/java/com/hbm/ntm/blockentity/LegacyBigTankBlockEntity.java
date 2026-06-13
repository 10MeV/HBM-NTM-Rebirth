package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraftforge.items.IItemHandler;

public abstract class LegacyBigTankBlockEntity extends FluidTankBlockEntity {
    protected LegacyBigTankBlockEntity(BlockPos pos, BlockState state, BlockEntityType<?> type, HbmFluidTank tank) {
        super(pos, state, type, tank);
    }

    @Override
    protected boolean checkHazards() {
        if (getTank().isEmpty() || level == null || !getTank().getTankType().isAntimatter()) {
            return false;
        }
        dropInventoryItems();
        int fill = getTank().getFill();
        level.destroyBlock(worldPosition, false);
        level.explode(null, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D, 10.0F, Level.ExplosionInteraction.BLOCK);
        getTank().setFill(0);
        return fill > 0;
    }

    @Override
    protected boolean hasDamageState() {
        return false;
    }

    @Override
    protected int legacyNetworkPackRange() {
        return 50;
    }

    @Override
    public boolean isExploded() {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean usesExternalExplosionDamageChain() {
        return false;
    }

    @Override
    public void repairTank() {
    }

    @Override
    public boolean isDamagedForFluidRepair() {
        return false;
    }

    @Override
    public void explodeFromFluidOverpressure(Level level, BlockPos pos) {
        if (level == null || level.isClientSide || level.isEmptyBlock(pos)) {
            return;
        }
        dropInventoryItems();
        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5.0F,
                Level.ExplosionInteraction.NONE);
    }

    @Override
    public void writePersistentState(CompoundTag persistent) {
        if (getTank().getFill() == 0) {
            return;
        }
        getTank().writeToNbt(persistent, "tank");
        persistent.putShort("mode", (short) getMode());
    }

    @Override
    public void readPersistentState(CompoundTag persistent) {
        getTank().readFromNbt(persistent, "tank");
        int mode = persistent.contains("mode") ? persistent.getShort("mode") : persistent.getShort("nbt");
        setMode(mode);
        setChanged();
        if (level != null) {
            refreshFluidNodeState();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public int[] getFluidIdsToCopy() {
        return super.getFluidIdsToCopy();
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return super.canConnectFluid(type, side) && type == getTank().getTankType();
    }

    @Override
    protected IItemHandler getExternalItemHandler() {
        return getTankContainerAutomationItemHandler();
    }

    private void dropInventoryItems() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (ItemStack stack : getDrops()) {
            Block.popResource(level, worldPosition, stack);
        }
    }
}
