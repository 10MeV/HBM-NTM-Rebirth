package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.RBMKConsoleBlockEntity;
import com.hbm.ntm.item.GuideBookItem;
import com.hbm.ntm.neutron.RBMKPanelBlockPlanner;
import com.hbm.ntm.neutron.RBMKPanelBlockPlanner.ConsoleGuideBookPlan;
import com.hbm.ntm.neutron.RBMKBlockPlanner;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.menu.RBMKConsoleMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class RBMKConsoleBlock extends LegacyXrMultiblockBlock implements EntityBlock, Toolable {
    private static final int[] LEGACY_DIMENSIONS = new int[] { 3, 0, 0, 0, 2, 2 };
    private static final int[] LEGACY_EXTRA_DIMENSIONS = new int[] { 0, 0, 0, 1, 2, 2 };

    public RBMKConsoleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return LEGACY_DIMENSIONS;
    }

    @Override
    protected int getLegacyOffset() {
        return 1;
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
        BlockPos extraOrigin = LegacyMultiblockLayout.relative(facing);
        return super.getLayout(state)
                .withLegacyXrProxyFill(LEGACY_EXTRA_DIMENSIONS, facing, extraOrigin, LegacyProxyMode.passive())
                .withLegacyXrCheckOnly(LEGACY_EXTRA_DIMENSIONS, facing, extraOrigin)
                .withLegacyExtraOffsets(LegacyMultiblockLayout.legacyXrFillOffsets(
                        LEGACY_EXTRA_DIMENSIONS, facing, extraOrigin));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.block();
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
        return getLayout(state).shape(1.0D);
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return getMultiblockShape(state, level, corePos, context);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !oldState.is(state.getBlock())) {
            fillConsoleLayout(level, pos, state);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ToolType tool = ToolType.getType(player.getItemInHand(hand));
        if (tool == ToolType.SCREWDRIVER) {
            return onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(), tool)
                    ? InteractionResult.sidedSuccess(level.isClientSide)
                    : InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide) {
            return MultiblockHelper.isOperationalCoreLayoutComplete(level, pos)
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity blockEntity = MultiblockHelper.resolveOperationalCoreBlockEntity(level, pos);
            if (blockEntity instanceof RBMKConsoleBlockEntity console) {
                ConsoleGuideBookPlan guidePlan = RBMKPanelBlockPlanner.planConsoleGuideBookClick(
                        console.getBlockPos(),
                        pos,
                        coreMetaForGuideBook(console.getBlockState()),
                        hit.getDirection().ordinal(),
                        hit.getLocation().x - pos.getX(),
                        hit.getLocation().z - pos.getZ(),
                        playerHasGuideBook(player, GuideBookItem.BookType.RBMK));
                if (guidePlan.grantGuideBook()) {
                    ItemStack stack = GuideBookItem.stack(ModItems.BOOK_GUIDE.get(), GuideBookItem.BookType.RBMK);
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                    player.inventoryMenu.broadcastChanges();
                    return InteractionResult.SUCCESS;
                }
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (containerId, inventory, opener) -> new RBMKConsoleMenu(containerId, inventory, console),
                        Component.translatable("container.rbmkConsole")), console.getBlockPos());
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }
        if (level.isClientSide) {
            return MultiblockHelper.isOperationalCoreLayoutComplete(level, pos);
        }
        if (!level.isClientSide) {
            BlockEntity blockEntity = MultiblockHelper.resolveOperationalCoreBlockEntity(level, pos);
            if (!(blockEntity instanceof RBMKConsoleBlockEntity console)) {
                return false;
            }
            console.rotateConsole();
        }
        return true;
    }

    private boolean fillConsoleLayout(Level level, BlockPos corePos, BlockState state) {
        LegacyMultiblockLayout layout = getLayout(state);
        boolean filled = MultiblockHelper.fillLayout(level, corePos, layout);
        boolean complete = filled && MultiblockHelper.isLayoutComplete(level, corePos, layout);
        if (!complete) {
            onIncompleteLegacyLayout(level, corePos, state);
        }
        return complete;
    }

    private static int coreMetaForGuideBook(BlockState state) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
        return RBMKBlockPlanner.CORE_METADATA_OFFSET + facing.ordinal();
    }

    private static boolean playerHasGuideBook(Player player, GuideBookItem.BookType type) {
        return player.getInventory().items.stream().anyMatch(stack -> GuideBookItem.isType(stack, type));
    }

    @Override
    protected boolean requiresCompleteLegacyLayout(BlockState state) {
        return true;
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
    public boolean usesMultiblockHighlightShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }

    @Override
    public boolean requiresCompleteOperationalLayout(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public VoxelShape getMultiblockDummyShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getMultiblockDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return Shapes.block();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RBMKConsoleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.RBMK_CONSOLE.get()) {
            return null;
        }
        return level.isClientSide
                ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        RBMKConsoleBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (RBMKConsoleBlockEntity) blockEntity);
    }
}
