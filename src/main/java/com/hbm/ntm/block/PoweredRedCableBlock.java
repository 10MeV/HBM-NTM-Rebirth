package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.PoweredRedCableBlockEntity;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PoweredRedCableBlock extends HbmEnergyNodeBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final Kind kind;

    public PoweredRedCableBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean active = kind == Kind.DETECTOR && context.getLevel().hasNeighborSignal(context.getClickedPos());
        return getConnectionState(defaultBlockState().setValue(ACTIVE, active), context.getLevel(), context.getClickedPos());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return kind == Kind.SWITCH
                ? PoweredRedCableBlockEntity.switchEntity(pos, state)
                : PoweredRedCableBlockEntity.detectorEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (kind != Kind.SWITCH || player.isShiftKeyDown()) {
            return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            setActive(level, pos, state, !state.getValue(ACTIVE), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos,
            boolean movedByPiston) {
        if (kind == Kind.DETECTOR) {
            boolean powered = level.hasNeighborSignal(pos);
            if (state.getValue(ACTIVE) != powered) {
                setActive(level, pos, state, powered, true);
                return;
            }
        }
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
    }

    public boolean isActive(BlockState state) {
        return state.hasProperty(ACTIVE) && state.getValue(ACTIVE);
    }

    @Override
    public boolean canConnectEnergy(BlockGetter level, BlockPos pos, @Nullable net.minecraft.core.Direction side) {
        return isActive(level.getBlockState(pos)) && super.canConnectEnergy(level, pos, side);
    }

    @Override
    protected BlockState getConnectionState(BlockState state, BlockGetter level, BlockPos pos) {
        return state
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    private void setActive(Level level, BlockPos pos, BlockState state, boolean active, boolean playSound) {
        if (level == null || level.isClientSide || !state.is(this) || state.getValue(ACTIVE) == active) {
            return;
        }
        BlockState updated = state.setValue(ACTIVE, active);
        level.setBlock(pos, updated, Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
        if (playSound) {
            LegacySoundPlayer.playLegacyReactorStart(level, pos, 1.0F, active ? 1.0F : 0.85F);
        }
        updateEnergyConnectionGraph(level, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    public enum Kind {
        SWITCH,
        DETECTOR
    }
}
