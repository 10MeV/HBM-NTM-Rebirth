package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.RBMKAutoloaderBlockEntity;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyMultiblockPlaceable;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RBMKAutoloaderBlock extends BaseEntityBlock implements MultiblockCoreBlock, LegacyMultiblockPlaceable {
    public static final int HEIGHT_ABOVE = 8;
    private static final LegacyMultiblockLayout LAYOUT = createLayout();
    private static final VoxelShape DETAILED_SHAPE = Shapes.or(
            Shapes.box(0.375D, 0.0D, 0.375D, 0.625D, 4.0D, 0.625D),
            Shapes.box(0.0D, 4.0D, 0.0D, 1.0D, 9.0D, 1.0D)).optimize();

    public RBMKAutoloaderBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = getDirectPlacementState(context);
        if (state == null) {
            return null;
        }
        BlockPos corePos = getDirectPlacementCore(context, state);
        return canPlaceDirectMultiblock(context.getLevel(), corePos, context.getClickedPos(), state) ? state : null;
    }

    @Nullable
    @Override
    public BlockState getDirectPlacementState(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    public BlockPos getDirectPlacementCore(BlockPlaceContext context, BlockState state) {
        return context.getClickedPos();
    }

    @Override
    public boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos) {
        return canPlaceDirectMultiblock(level, corePos, temporaryPos, defaultBlockState());
    }

    @Override
    public boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos, BlockState state) {
        return MultiblockHelper.checkLayout(level, corePos, LAYOUT, temporaryPos);
    }

    @Override
    public void afterDirectCorePlaced(Level level, BlockPos corePos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, corePos, state, placer, stack);
    }

    @Override
    public void completeDirectMultiblockPlacement(Level level, BlockPos corePos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        fillLayout(level, corePos);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        fillLayout(level, pos);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(state.getBlock())) {
            fillLayout(level, pos);
        }
    }

    @Nullable
    @Override
    public LegacyMultiblockLayout getMultiblockLayout(BlockState state, BlockGetter level, BlockPos corePos) {
        return LAYOUT;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return localDetailedShape(BlockPos.ZERO);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return localDetailedShape(BlockPos.ZERO);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return DETAILED_SHAPE;
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return DETAILED_SHAPE;
    }

    @Override
    public boolean usesForwardedDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }

    @Override
    public boolean usesForwardedDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }

    @Override
    public boolean usesLocalDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesLocalDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean requiresCompleteOperationalLayout(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public VoxelShape getMultiblockDummyShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return localDetailedShape(dummyPos.subtract(corePos));
    }

    @Override
    public VoxelShape getMultiblockDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return localDetailedShape(dummyPos.subtract(corePos));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide) {
            return MultiblockHelper.isOperationalCoreLayoutComplete(level, pos)
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }
        BlockEntity blockEntity = MultiblockHelper.resolveOperationalCoreBlockEntity(level, pos);
        if (player instanceof ServerPlayer serverPlayer
                && blockEntity instanceof RBMKAutoloaderBlockEntity autoloader) {
            if (player.isShiftKeyDown()) {
                return InteractionResult.SUCCESS;
            }
            NetworkHooks.openScreen(serverPlayer, autoloader, autoloader.getBlockPos());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.getAbilities().instabuild
                && MultiblockHelper.resolveCoreBlockEntity(level, pos) instanceof RBMKAutoloaderBlockEntity autoloader) {
            HbmItemStackUtil.dropStacks(level, autoloader.getBlockPos(), autoloader.removeItemsForDrop());
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void beforeMultiblockDummyDestroysCore(Level level, BlockPos corePos, BlockState coreState,
            BlockPos dummyPos, boolean drop) {
        if (drop && !level.isClientSide
                && level.getBlockEntity(corePos) instanceof RBMKAutoloaderBlockEntity autoloader) {
            HbmItemStackUtil.dropStacks(level, corePos, autoloader.removeItemsForDrop());
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            MultiblockHelper.removeLayout(level, pos, LAYOUT);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RBMKAutoloaderBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.RBMK_AUTOLOADER.get(),
                RBMKAutoloaderBlockEntity::serverTick);
    }

    private static void fillLayout(Level level, BlockPos corePos) {
        if (!level.isClientSide) {
            boolean filled = MultiblockHelper.fillLayout(level, corePos, LAYOUT);
            if (!filled || !MultiblockHelper.isLayoutComplete(level, corePos, LAYOUT)) {
                level.destroyBlock(corePos, false);
            }
        }
    }

    private static LegacyMultiblockLayout createLayout() {
        List<BlockPos> offsets = new ArrayList<>(HEIGHT_ABOVE + 1);
        offsets.add(BlockPos.ZERO);
        for (int y = 1; y <= HEIGHT_ABOVE; y++) {
            offsets.add(new BlockPos(0, y, 0));
        }
        return LegacyMultiblockLayout.ofOffsets(offsets)
                .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO),
                        LegacyProxyMode.passive().withInventory(true))
                .withLegacyExtraOffsets(List.of(new BlockPos(0, HEIGHT_ABOVE, 0)));
    }

    private static VoxelShape localDetailedShape(BlockPos offset) {
        VoxelShape shape = Shapes.empty();
        for (AABB box : DETAILED_SHAPE.toAabbs()) {
            double minX = Math.max(0.0D, box.minX - offset.getX());
            double minY = Math.max(0.0D, box.minY - offset.getY());
            double minZ = Math.max(0.0D, box.minZ - offset.getZ());
            double maxX = Math.min(1.0D, box.maxX - offset.getX());
            double maxY = Math.min(1.0D, box.maxY - offset.getY());
            double maxZ = Math.min(1.0D, box.maxZ - offset.getZ());
            if (minX < maxX && minY < maxY && minZ < maxZ) {
                shape = Shapes.or(shape, Shapes.box(minX, minY, minZ, maxX, maxY, maxZ));
            }
        }
        return shape.optimize();
    }
}
