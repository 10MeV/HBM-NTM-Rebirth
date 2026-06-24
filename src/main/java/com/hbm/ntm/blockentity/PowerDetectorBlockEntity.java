package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.tile.InfoProviderEC;
import com.hbm.ntm.block.PowerDetectorBlock;
import com.hbm.ntm.energy.HbmEnergyConnector;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmLoadedEnergy;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PowerDetectorBlockEntity extends BlockEntity
        implements HbmEnergyReceiver, HbmEnergyConnector, HbmLoadedEnergy, InfoProviderEC {
    private long power;

    public PowerDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POWER_DETECTOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PowerDetectorBlockEntity detector) {
        HbmEnergyUtil.subscribeReceiverToAllNeighborNetworks(level, pos, detector);
        boolean active = detector.power > 0L;
        if (active) {
            detector.power--;
        }
        if (state.hasProperty(PowerDetectorBlock.ACTIVE) && state.getValue(PowerDetectorBlock.ACTIVE) != active) {
            BlockState updated = state.setValue(PowerDetectorBlock.ACTIVE, active);
            level.setBlock(pos, updated, Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
            level.updateNeighborsAt(pos, updated.getBlock());
            detector.setChanged();
        }
    }

    @Override
    public boolean canConnectEnergy(@Nullable Direction side) {
        return side != null;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = Math.max(0L, Math.min(power, getMaxPower()));
    }

    @Override
    public long getMaxPower() {
        return 5L;
    }

    @Override
    public ConnectionPriority getPriority() {
        return ConnectionPriority.HIGH;
    }

    @Override
    public boolean isEnergyLoaded() {
        return level != null && !isRemoved();
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        data.putLong("power", power);
        data.putBoolean("active", getBlockState().hasProperty(PowerDetectorBlock.ACTIVE)
                && getBlockState().getValue(PowerDetectorBlock.ACTIVE));
    }
}
