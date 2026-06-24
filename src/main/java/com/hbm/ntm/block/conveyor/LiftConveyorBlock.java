package com.hbm.ntm.block.conveyor;

import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.ConveyorPathType;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiftConveyorBlock extends ConveyorBlock {
    private static final VoxelShape FULL_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape TOP_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);

    public LiftConveyorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {
        return ConveyorMath.liftTravelLocation(level, pos, legacyMetadata(level.getBlockState(pos)), itemPos, speed);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {
        return ConveyorMath.liftSnappingPosition(level, pos, legacyMetadata(level.getBlockState(pos)), itemPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ConveyorMath.isLiftTop(level, pos) ? TOP_SHAPE : FULL_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ConveyorMath.isLiftTop(level, pos) ? TOP_SHAPE : FULL_SHAPE;
    }

    @Override
    protected BlockState nextScrewdriverState(BlockState state, int metadata, int baseMetadata,
            ConveyorPathType path, boolean sneaking) {
        if (sneaking) {
            return ((ConveyorBlock) ModBlocks.CONVEYOR_CHUTE.get()).stateFromLegacyMetadata(baseMetadata);
        }
        return stateFromLegacyMetadata(nextBendableMetadata(metadata, baseMetadata, path, false));
    }
}
