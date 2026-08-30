package com.hbm.ntm.block;

import com.hbm.ntm.multiblock.DummyBlock;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Internal carrier for the dummy segments of legacy custom rails. Unlike a
 * normal multiblock dummy, the 1.7.10 rail block supplied a local 1/8-block
 * collision and outline at every segment.
 */
@SuppressWarnings("deprecation")
public class RailDummyBlock extends DummyBlock {
    public static final DirectionProperty FACING = HorizontalMachineBlock.FACING;
    private static final VoxelShape RAIL_SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public RailDummyBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core != null && core.state().getBlock() instanceof MultiblockCoreBlock coreBlock
                && coreBlock.usesForwardedDummyShape(core.state(), level, core.pos())) {
            return coreBlock.getMultiblockDummyShape(core.state(), level, core.pos(), pos, context);
        }
        return RAIL_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core != null && core.state().getBlock() instanceof MultiblockCoreBlock coreBlock
                && coreBlock.usesForwardedDummyCollisionShape(core.state(), level, core.pos())) {
            return coreBlock.getMultiblockDummyCollisionShape(core.state(), level, core.pos(), pos, context);
        }
        return RAIL_SHAPE;
    }
}
