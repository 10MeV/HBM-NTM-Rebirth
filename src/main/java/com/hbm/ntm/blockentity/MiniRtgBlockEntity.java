package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.MiniRtgBlock;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MiniRtgBlockEntity extends HbmEnergyBlockEntity {
    private static final String TAG_POWER = "power";
    private final MiniRtgBlock.Kind kind;

    public MiniRtgBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, kindFor(state));
    }

    private MiniRtgBlockEntity(BlockPos pos, BlockState state, MiniRtgBlock.Kind kind) {
        super(ModBlockEntities.MINI_RTG.get(), pos, state, new HbmEnergyStorage(kind.maxPower(), 0L, kind.output()));
        this.kind = kind;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MiniRtgBlockEntity rtg) {
        if (level.isClientSide) {
            return;
        }
        long oldPower = rtg.getPower();
        rtg.setPower(rtg.getPower() + rtg.getOutput());
        HbmEnergyUtil.tryProvideToAllNeighbors(level, pos, rtg.energy);
        if (oldPower != rtg.getPower()) {
            rtg.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public long getOutput() {
        return kind.output();
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.OUTPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_POWER, getPower());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_POWER)) {
            setPower(tag.getLong(TAG_POWER));
        }
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putLong(TAG_POWER, getPower());
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains(TAG_POWER)) {
            setPower(tag.getLong(TAG_POWER));
        }
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        data.putBoolean(CompatEnergyControl.B_ACTIVE, true);
        data.putDouble(CompatEnergyControl.D_OUTPUT_HE, getOutput());
    }

    private static MiniRtgBlock.Kind kindFor(BlockState state) {
        return state.getBlock() instanceof MiniRtgBlock rtg ? rtg.kind() : MiniRtgBlock.Kind.CELL;
    }
}
