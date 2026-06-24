package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class SpotlightBeamBlockEntity extends BlockEntity {
    private static final String TAG_MASK = "mask";

    private int mask;

    public SpotlightBeamBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPOTLIGHT_BEAM.get(), pos, state);
    }

    public int setDirection(Direction direction, boolean state) {
        int flag = 1 << direction.ordinal();
        mask = state ? mask | flag : mask & ~flag;
        setChanged();
        return mask;
    }

    public List<Direction> directions() {
        List<Direction> directions = new ArrayList<>(6);
        for (Direction direction : Direction.values()) {
            if ((mask & (1 << direction.ordinal())) != 0) {
                directions.add(direction);
            }
        }
        return directions;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_MASK, mask);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mask = tag.getInt(TAG_MASK);
    }
}
