package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 1.7.10 BlockNTMTrapdoor: manually operable iron trapdoor with ladder continuity. */
public class LegacySteelTrapdoorBlock extends TrapDoorBlock {
    private static final VoxelShape NORTH_LADDER_SHAPE = box(0.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SOUTH_LADDER_SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
    private static final VoxelShape WEST_LADDER_SHAPE = box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape EAST_LADDER_SHAPE = box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 16.0D);

    public LegacySteelTrapdoorBlock(BlockBehaviour.Properties properties) {
        super(properties, BlockSetType.IRON);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide) {
            level.setBlock(pos, state.cycle(OPEN), 2);
            level.levelEvent(player, 1003, pos, 0);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return state.getValue(OPEN) && level.getBlockState(pos.below()).isLadder(level, pos.below(), entity);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(OPEN) || !(level instanceof LevelReader reader)
                || !level.getBlockState(pos.below()).isLadder(reader, pos.below(), null)) {
            return super.getCollisionShape(state, level, pos, context);
        }
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_LADDER_SHAPE;
            case SOUTH -> SOUTH_LADDER_SHAPE;
            case WEST -> WEST_LADDER_SHAPE;
            case EAST -> EAST_LADDER_SHAPE;
            default -> super.getCollisionShape(state, level, pos, context);
        };
    }
}
