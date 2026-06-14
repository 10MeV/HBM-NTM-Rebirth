package com.hbm.ntm.block;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.FluidPipeAnchorBlockEntity;
import com.hbm.ntm.blockentity.FluidCounterValveBlockEntity;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.blockentity.FluidValveBlockEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.network.HbmServerKeybinds;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FluidValveBlock extends HbmFluidNodeBlock {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    private final Kind kind;
    private static final net.minecraft.world.phys.shapes.VoxelShape FULL_SHAPE =
            box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public FluidValveBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(defaultBlockState().setValue(OPEN, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean open = kind == Kind.SWITCH && context.getLevel().hasNeighborSignal(context.getClickedPos());
        return getConnectionState(defaultBlockState().setValue(OPEN, open), context.getLevel(), context.getClickedPos());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return kind == Kind.COUNTER
                ? new FluidCounterValveBlockEntity(pos, state)
                : new FluidValveBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || kind != Kind.COUNTER) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.FLUID_COUNTER_VALVE.get(), FluidCounterValveBlockEntity::serverTick);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos,
            boolean movedByPiston) {
        if (kind == Kind.SWITCH) {
            boolean powered = level.hasNeighborSignal(pos);
            if (state.getValue(OPEN) != powered) {
                setOpen(level, pos, state, powered, true);
                return;
            }
        }
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        InteractionResult identifierResult = useFluidIdentifier(state, level, pos, player, hand);
        if (identifierResult.consumesAction()) {
            return identifierResult;
        }
        if (kind != Kind.VALVE || player.isShiftKeyDown()) {
            return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            setOpen(level, pos, state, !state.getValue(OPEN), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public void setOpen(Level level, BlockPos pos, BlockState state, boolean open, boolean playSound) {
        if (level == null || level.isClientSide || !state.is(this) || state.getValue(OPEN) == open) {
            return;
        }
        BlockState updated = state.setValue(OPEN, open);
        level.setBlock(pos, updated, Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
        if (playSound) {
            LegacySoundPlayer.playLegacyReactorStart(level, pos, 1.0F, open ? 1.0F : 0.85F);
        }
        if (level.getBlockEntity(pos) instanceof FluidValveBlockEntity valve) {
            valve.onValveStateChanged();
        } else {
            updateFluidConnectionGraph(level, pos);
        }
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
    public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            net.minecraft.world.phys.shapes.CollisionContext context) {
        return FULL_SHAPE;
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState state, BlockGetter level,
            BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return FULL_SHAPE;
    }

    private InteractionResult useFluidIdentifier(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof IFluidIdentifierItem identifier)
                || !(level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        FluidType target = identifier.getIdentifiedFluid(level, pos, held);
        boolean toolAlt = player instanceof ServerPlayer serverPlayer
                && HbmServerKeybinds.isPressed(serverPlayer, HbmKeybind.TOOL_ALT);
        boolean toolCtrl = player instanceof ServerPlayer serverPlayer
                && HbmServerKeybinds.isPressed(serverPlayer, HbmKeybind.TOOL_CTRL);

        if (toolAlt && target != pipe.getFluidType()
                && identifier.setIdentifiedFluid(held, pipe.getFluidType(), true)) {
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                    0.25F, 0.75F);
            return InteractionResult.CONSUME;
        }

        if (toolCtrl || player.isShiftKeyDown()) {
            FluidPipeBlockEntity.changeConnectedPipeTypes(level, pos, pipe.getFluidType(), target, 64);
        } else {
            pipe.setFluidType(target);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN);
    }

    public enum Kind {
        VALVE,
        SWITCH,
        COUNTER
    }

}
