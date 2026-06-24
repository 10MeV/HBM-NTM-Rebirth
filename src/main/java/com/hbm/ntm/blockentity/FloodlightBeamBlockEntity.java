package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FloodlightBeamBlockEntity extends BlockEntity {
    private BlockPos sourcePos;
    private int index;
    private LegacyLightBlockEntity cache;

    public FloodlightBeamBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLOODLIGHT_BEAM.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FloodlightBeamBlockEntity beam) {
        if (level.isClientSide || level.getGameTime() % 5L != 0L) {
            return;
        }

        if (beam.sourcePos == null) {
            level.removeBlock(pos, false);
            return;
        }

        if (beam.cache == null) {
            if (!level.hasChunk(beam.sourcePos.getX() >> 4, beam.sourcePos.getZ() >> 4)) {
                return;
            }
            BlockEntity source = level.getBlockEntity(beam.sourcePos);
            if (source instanceof LegacyLightBlockEntity floodlight) {
                beam.cache = floodlight;
            } else {
                level.removeBlock(pos, false);
                return;
            }
        }

        if (beam.cache.isRemoved()
                || !beam.cache.isOn()
                || !pos.equals(beam.cache.lightPos(beam.index))) {
            level.removeBlock(pos, false);
        }
    }

    public void setSource(LegacyLightBlockEntity floodlight, int index) {
        this.cache = floodlight;
        this.sourcePos = floodlight.getBlockPos();
        this.index = index;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (sourcePos != null) {
            tag.putInt("sourceX", sourcePos.getX());
            tag.putInt("sourceY", sourcePos.getY());
            tag.putInt("sourceZ", sourcePos.getZ());
        }
        tag.putInt("index", index);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("sourceX") || tag.contains("sourceY") || tag.contains("sourceZ")) {
            sourcePos = new BlockPos(tag.getInt("sourceX"), tag.getInt("sourceY"), tag.getInt("sourceZ"));
        } else {
            sourcePos = null;
        }
        index = tag.getInt("index");
        cache = null;
    }
}
