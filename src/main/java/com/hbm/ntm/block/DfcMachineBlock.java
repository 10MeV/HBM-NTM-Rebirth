package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.DfcCoreBlockEntity;
import com.hbm.ntm.blockentity.DfcEmitterBlockEntity;
import com.hbm.ntm.blockentity.DfcInjectorBlockEntity;
import com.hbm.ntm.blockentity.DfcReceiverBlockEntity;
import com.hbm.ntm.blockentity.DfcStabilizerBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class DfcMachineBlock extends DirectionalBlock implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape FULL_BLOCK = box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private final Kind kind;

    public DfcMachineBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return usesChunkBakedStaticModel()
                ? LegacyMachineRenderShapes.chunkBakedStaticOrEntity()
                : super.getRenderShape(state);
    }

    public boolean usesChunkBakedStaticModel() {
        return kind == Kind.EMITTER
                || kind == Kind.RECEIVER
                || kind == Kind.INJECTOR
                || kind == Kind.STABILIZER;
    }

    @Nullable
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FULL_BLOCK;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (kind) {
            case CORE -> new DfcCoreBlockEntity(pos, state);
            case EMITTER -> new DfcEmitterBlockEntity(pos, state);
            case RECEIVER -> new DfcReceiverBlockEntity(pos, state);
            case INJECTOR -> new DfcInjectorBlockEntity(pos, state);
            case STABILIZER -> new DfcStabilizerBlockEntity(pos, state);
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof net.minecraft.world.MenuProvider provider) {
            NetworkHooks.openScreen(serverPlayer, provider, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        if (kind == Kind.CORE && type == ModBlockEntities.DFC_CORE.get()) {
            return ticker(DfcCoreBlockEntity.class, DfcCoreBlockEntity::serverTick);
        }
        if (kind == Kind.EMITTER && type == ModBlockEntities.DFC_EMITTER.get()) {
            return ticker(DfcEmitterBlockEntity.class, DfcEmitterBlockEntity::serverTick);
        }
        if (kind == Kind.RECEIVER && type == ModBlockEntities.DFC_RECEIVER.get()) {
            return ticker(DfcReceiverBlockEntity.class, DfcReceiverBlockEntity::serverTick);
        }
        if (kind == Kind.INJECTOR && type == ModBlockEntities.DFC_INJECTOR.get()) {
            return ticker(DfcInjectorBlockEntity.class, DfcInjectorBlockEntity::serverTick);
        }
        if (kind == Kind.STABILIZER && type == ModBlockEntities.DFC_STABILIZER.get()) {
            return ticker(DfcStabilizerBlockEntity.class, DfcStabilizerBlockEntity::serverTick);
        }
        return null;
    }

    private static <E extends BlockEntity, T extends BlockEntity> BlockEntityTicker<T> ticker(
            Class<E> expectedType, BlockEntityTicker<E> serverTicker) {
        return (tickLevel, tickPos, tickState, blockEntity) -> {
            if (expectedType.isInstance(blockEntity)) {
                serverTicker.tick(tickLevel, tickPos, tickState, expectedType.cast(blockEntity));
            }
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            List<ItemStack> drops;
            if (blockEntity instanceof DfcCoreBlockEntity core) {
                drops = core.getDrops();
            } else if (blockEntity instanceof DfcInjectorBlockEntity injector) {
                drops = injector.getDrops();
            } else if (blockEntity instanceof DfcStabilizerBlockEntity stabilizer) {
                drops = stabilizer.getDrops();
            } else {
                drops = List.of();
            }
            for (ItemStack stack : drops) {
                Block.popResource(level, pos, stack);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public enum Kind {
        CORE,
        EMITTER,
        RECEIVER,
        INJECTOR,
        STABILIZER
    }
}
