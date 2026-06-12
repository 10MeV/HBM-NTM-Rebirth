package com.hbm.ntm.block;

import com.hbm.ntm.turret.TurretBlockEntityBase;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class TurretBaseBlock extends LegacyXrMultiblockBlock implements EntityBlock {
    private static final VoxelShape HALF_HEIGHT = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    public static final int[] STANDARD_DIMENSIONS = new int[] { 0, 0, 1, 0, 1, 0 };
    public static final int[] ARTILLERY_DIMENSIONS = new int[] { 1, 0, 2, 1, 2, 1 };
    private final BiFunction<BlockPos, BlockState, ? extends TurretBlockEntityBase> blockEntityFactory;
    private final Supplier<? extends BlockEntityType<?>> blockEntityType;
    private final int[] legacyDimensions;
    private final int legacyOffset;
    private final LegacyProxyMode proxyMode;
    private final boolean opensGui;
    private final boolean dropsInventory;

    public TurretBaseBlock(Properties properties,
            BiFunction<BlockPos, BlockState, ? extends TurretBlockEntityBase> blockEntityFactory,
            Supplier<? extends BlockEntityType<?>> blockEntityType) {
        this(properties, blockEntityFactory, blockEntityType, STANDARD_DIMENSIONS, 0,
                LegacyProxyMode.combo(true, true, false), true, true);
    }

    public TurretBaseBlock(Properties properties,
            BiFunction<BlockPos, BlockState, ? extends TurretBlockEntityBase> blockEntityFactory,
            Supplier<? extends BlockEntityType<?>> blockEntityType,
            int[] legacyDimensions,
            int legacyOffset,
            LegacyProxyMode proxyMode) {
        this(properties, blockEntityFactory, blockEntityType, legacyDimensions, legacyOffset, proxyMode, true, true);
    }

    public TurretBaseBlock(Properties properties,
            BiFunction<BlockPos, BlockState, ? extends TurretBlockEntityBase> blockEntityFactory,
            Supplier<? extends BlockEntityType<?>> blockEntityType,
            int[] legacyDimensions,
            int legacyOffset,
            LegacyProxyMode proxyMode,
            boolean opensGui,
            boolean dropsInventory) {
        super(properties);
        this.blockEntityFactory = blockEntityFactory;
        this.blockEntityType = blockEntityType;
        this.legacyDimensions = legacyDimensions.clone();
        this.legacyOffset = legacyOffset;
        this.proxyMode = proxyMode;
        this.opensGui = opensGui;
        this.dropsInventory = dropsInventory;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityFactory.apply(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!opensGui) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof TurretBlockEntityBase turret) {
            NetworkHooks.openScreen(serverPlayer, turret, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != blockEntityType.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                TurretBlockEntityBase.clientTick(tickLevel, tickPos, tickState, (TurretBlockEntityBase) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                TurretBlockEntityBase.serverTick(tickLevel, tickPos, tickState, (TurretBlockEntityBase) blockEntity);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return HALF_HEIGHT;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return HALF_HEIGHT;
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return legacyDimensions;
    }

    @Override
    protected int getLegacyOffset() {
        return legacyOffset;
    }

    @Override
    protected Predicate<BlockPos> proxyOffsets(BlockState state) {
        return offset -> !offset.equals(BlockPos.ZERO);
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        return LegacyMultiblockLayout.ofLegacyXrChecked(
                getLegacyXrDimensions(), state.getValue(FACING), proxyOffsets(state), proxyMode);
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return getLayout(state).shape(0.5D);
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return getLayout(state).shape(0.5D);
    }

    @Override
    public boolean usesForwardedDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesForwardedDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (dropsInventory && !level.isClientSide && level.getBlockEntity(pos) instanceof TurretBlockEntityBase turret) {
                for (ItemStack stack : turret.getDrops()) {
                    Block.popResource(level, pos, stack);
                }
            }
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
