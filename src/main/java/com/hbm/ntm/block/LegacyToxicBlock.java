package com.hbm.ntm.block;

import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.radiation.RadiationUtil.ContaminationType;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("deprecation")
public class LegacyToxicBlock extends Block {
    public LegacyToxicBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, new Vec3(0.25D, 0.05D, 0.25D));
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            RadiationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.CREATIVE, 1.0F);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        reactToAdjacentLiquids(level, pos);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
            BlockPos pos, BlockPos neighborPos) {
        if (!level.isClientSide() && reactsTo(state, neighborState)) {
            return ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    private void reactToAdjacentLiquids(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        for (Direction direction : Direction.values()) {
            if (reactsTo(state, level.getBlockState(pos.relative(direction)))) {
                level.setBlock(pos, ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState(), Block.UPDATE_ALL);
                return;
            }
        }
    }

    private boolean reactsTo(BlockState self, BlockState neighbor) {
        return neighbor.getBlock() != self.getBlock() && !neighbor.getFluidState().isEmpty();
    }
}
