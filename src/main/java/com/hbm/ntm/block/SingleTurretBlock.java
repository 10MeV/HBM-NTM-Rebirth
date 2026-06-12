package com.hbm.ntm.block;

import com.hbm.ntm.turret.TurretBlockEntityBase;
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
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class SingleTurretBlock extends HorizontalMachineBlock implements EntityBlock {
    private static final VoxelShape HALF_HEIGHT = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);

    private final BiFunction<BlockPos, BlockState, ? extends TurretBlockEntityBase> blockEntityFactory;
    private final Supplier<? extends BlockEntityType<?>> blockEntityType;
    private final boolean opensGui;
    private final boolean dropsInventory;
    private final boolean halfHeight;

    public SingleTurretBlock(Properties properties,
            BiFunction<BlockPos, BlockState, ? extends TurretBlockEntityBase> blockEntityFactory,
            Supplier<? extends BlockEntityType<?>> blockEntityType,
            boolean opensGui,
            boolean dropsInventory,
            boolean halfHeight) {
        super(properties, false);
        this.blockEntityFactory = blockEntityFactory;
        this.blockEntityType = blockEntityType;
        this.opensGui = opensGui;
        this.dropsInventory = dropsInventory;
        this.halfHeight = halfHeight;
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
        if (!level.isClientSide && !player.isShiftKeyDown()
                && player instanceof ServerPlayer serverPlayer
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
        return halfHeight ? HALF_HEIGHT : super.getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return halfHeight ? HALF_HEIGHT : super.getCollisionShape(state, level, pos, context);
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
