package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LegacyLanternBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public class LegacyLanternBlock extends BaseEntityBlock {
    public static final IntegerProperty SEGMENT = IntegerProperty.create("segment", 0, 4);
    private static final VoxelShape SHAPE = box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public LegacyLanternBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SEGMENT, 0));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        for (int offset = 0; offset <= 4; offset++) {
            BlockPos checkPos = pos.above(offset);
            if (!canReplace(level, checkPos, context)) {
                return null;
            }
        }
        return defaultBlockState();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide || state.getValue(SEGMENT) != 0 || state.is(oldState.getBlock())) {
            return;
        }
        for (int offset = 1; offset <= 4; offset++) {
            level.setBlock(pos.above(offset), defaultBlockState().setValue(SEGMENT, offset), Block.UPDATE_ALL);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            removeLantern(level, basePos(pos, state), pos);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        if (state.getValue(SEGMENT) != 0) {
            return List.of();
        }
        return super.getDrops(state, builder);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        removeLantern(level, basePos(pos, state), pos);
        super.destroy(level, pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            removeLantern(level, basePos(pos, state), pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(SEGMENT) == 0 ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(SEGMENT) == 0 ? SHAPE : Shapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(SEGMENT) == 0 ? super.getLightEmission(state, level, pos) : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(SEGMENT) == 0 ? new LegacyLanternBlockEntity(pos, state) : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SEGMENT);
    }

    private static boolean canReplace(Level level, BlockPos pos, BlockPlaceContext context) {
        BlockState state = level.getBlockState(pos);
        if (state.getFluidState().getType() != Fluids.EMPTY) {
            return false;
        }
        return state.canBeReplaced(context);
    }

    private static BlockPos basePos(BlockPos pos, BlockState state) {
        return pos.below(state.getValue(SEGMENT));
    }

    private static void removeLantern(LevelAccessor level, BlockPos basePos, BlockPos skippedPos) {
        for (int offset = 0; offset <= 4; offset++) {
            BlockPos target = basePos.above(offset);
            if (target.equals(skippedPos)) {
                continue;
            }
            BlockState targetState = level.getBlockState(target);
            if (targetState.getBlock() instanceof LegacyLanternBlock) {
                level.removeBlock(target, false);
            }
        }
    }
}
