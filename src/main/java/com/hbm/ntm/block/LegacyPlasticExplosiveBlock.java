package com.hbm.ntm.block;

import com.hbm.ntm.api.block.ChainExplodable;
import com.hbm.ntm.entity.item.LegacyPrimedExplosiveEntity;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class LegacyPlasticExplosiveBlock extends DirectionalBlock implements ChainExplodable, RemoteDetonatableBlock {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    public LegacyPlasticExplosiveBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!oldState.is(state.getBlock()) && !level.isClientSide && level.hasNeighborSignal(pos)) {
            detonate(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (!level.isClientSide && level.hasNeighborSignal(pos)) {
            detonate(level, pos);
        }
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        level.removeBlock(pos, false);
        if (!level.isClientSide) {
            level.addFreshEntity(LegacyPrimedExplosiveEntity.create(level,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, this, 0, false));
        }
    }

    @Override
    public void explodeEntity(Level level, Vec3 position, @Nullable Entity source) {
        if (!level.isClientSide) {
            explodeAt(level, position.x, position.y, position.z);
        }
    }

    @Override
    public BombReturnCode detonateFromRemote(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        return detonate(level, pos) ? BombReturnCode.DETONATED : BombReturnCode.ERROR_NO_BOMB;
    }

    private boolean detonate(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() != this) {
            return false;
        }
        level.removeBlock(pos, false);
        level.gameEvent(null, GameEvent.EXPLODE, pos);
        explodeAt(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        return true;
    }

    private void explodeAt(Level level, double x, double y, double z) {
        new ExplosionVnt(level, x, y, z, 20.0F)
                .makeStandard()
                .setBlockProcessor(new BlockProcessorStandard().setNoDrop())
                .explode();
    }
}
