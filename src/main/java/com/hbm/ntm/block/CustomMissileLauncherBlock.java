package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.CompactLauncherBlockEntity;
import com.hbm.ntm.blockentity.CustomMissileLauncherBlockEntity;
import com.hbm.ntm.blockentity.LaunchTableBlockEntity;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
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

@SuppressWarnings("deprecation")
public class CustomMissileLauncherBlock extends LegacyXrMultiblockBlock implements EntityBlock, RemoteDetonatableBlock {
    private static final int[] COMPACT_DIMENSIONS = new int[] { 0, 0, 1, 1, 1, 1 };
    private static final int[] TABLE_DIMENSIONS = new int[] { 0, 0, 4, 4, 4, 4 };
    private final Kind kind;

    public CustomMissileLauncherBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return kind == Kind.LAUNCH_TABLE ? TABLE_DIMENSIONS : COMPACT_DIMENSIONS;
    }

    @Override
    protected int getLegacyOffset() {
        return 0;
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        if (kind == Kind.LAUNCH_TABLE) {
            return LegacyMultiblockLayout.ofLegacyXrChecked(TABLE_DIMENSIONS, state.getValue(FACING))
                    .withProxyOffsets(launchTableProxyOffsets(state.getValue(FACING)),
                            LegacyProxyMode.combo(true, true, true));
        }
        return LegacyMultiblockLayout.ofLegacyXrChecked(COMPACT_DIMENSIONS, state.getValue(FACING))
                .withProxyOffsets(compactLauncherProxyOffsets(), LegacyProxyMode.combo(true, true, true));
    }

    @Override
    public boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos, BlockState state) {
        if (kind != Kind.LAUNCH_TABLE) {
            return super.canPlaceDirectMultiblock(level, corePos, temporaryPos, state);
        }

        // LaunchTable#onBlockPlacedBy only probes the 8x8 non-cross positions
        // before it overwrites the two centre axes with plate/port dummies.  Do
        // not turn those source-backed destructive placements into a modern
        // placement rejection.
        if (!isReplaceableOrTemporary(level, corePos, temporaryPos)) {
            return false;
        }
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                if (x != 0 && z != 0
                        && !isReplaceableOrTemporary(level, corePos.offset(x, 0, z), temporaryPos)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void completeDirectMultiblockPlacement(Level level, BlockPos corePos, BlockState state,
            @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide && kind == Kind.LAUNCH_TABLE) {
            // LaunchTable#onBlockPlacedBy only rejects the 8x8 non-cross cells,
            // then overwrites both centre axes with its plate/port dummy blocks.
            // MultiblockHelper deliberately does not overwrite non-replaceable
            // blocks, so clear those source-authorized destructive cells first.
            clearLegacyLaunchCross(level, corePos);
            clearLegacyLaunchColumn(level, corePos, state.getValue(FACING));
        }
        super.completeDirectMultiblockPlacement(level, corePos, state, placer, stack);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return kind == Kind.LAUNCH_TABLE
                ? new LaunchTableBlockEntity(pos, state)
                : new CompactLauncherBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        // LaunchTable/CompactLauncher only opened their GUI for non-sneaking
        // players.  Preserve the server-side false branch as a modern PASS so
        // a held item can receive the interaction.
        if (player.isShiftKeyDown()) {
            // LaunchTable/CompactLauncher consume client activation first;
            // only the legacy server sneaking branch returned false.
            return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && resolveCoreBlockEntity(level, pos) instanceof CustomMissileLauncherBlockEntity launcher) {
            NetworkHooks.openScreen(serverPlayer, launcher, launcher.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (kind == Kind.LAUNCH_TABLE && type != ModBlockEntities.LAUNCH_TABLE.get()) {
            return null;
        }
        if (kind == Kind.COMPACT_LAUNCHER && type != ModBlockEntities.COMPACT_LAUNCHER.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) -> CustomMissileLauncherBlockEntity.clientTick(
                        tickLevel, tickPos, tickState, (CustomMissileLauncherBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) -> CustomMissileLauncherBlockEntity.serverTick(
                        tickLevel, tickPos, tickState, (CustomMissileLauncherBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof CustomMissileLauncherBlockEntity launcher) {
            for (ItemStack stack : launcher.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    @Override
    public BombReturnCode detonateFromRemote(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        if (!(resolveCoreBlockEntity(level, pos) instanceof CustomMissileLauncherBlockEntity launcher)) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }
        return launcher.launchFromDesignator() ? BombReturnCode.LAUNCHED : BombReturnCode.ERROR_MISSING_COMPONENT;
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        // LaunchTable keeps BlockContainer's local one-block bounds.  CompactLauncher
        // explicitly sets its core bounds to y=1..1; its visible footprint is made
        // of local plate/port dummies, not a forwarded 3x3 selection box.
        return kind == Kind.LAUNCH_TABLE ? Shapes.block() : Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // LegacyXrMultiblockBlock inherits HorizontalMachineBlock's full selection
        // shape.  CompactLauncher overrides that inherited shape in 1.7.10 with
        // setBlockBounds(0, 1, 0, 1, 1, 1), which is an empty modern VoxelShape.
        return getMultiblockShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return kind == Kind.LAUNCH_TABLE ? Shapes.block() : Shapes.empty();
    }

    @Override
    public boolean usesForwardedDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }

    @Override
    public boolean usesForwardedDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }

    @Override
    public boolean usesLocalDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesLocalDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public VoxelShape getMultiblockDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return isLegacyPlateDummy(state, dummyPos.subtract(corePos)) ? Shapes.empty() : Shapes.block();
    }

    @Override
    public VoxelShape getMultiblockDummyShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return getMultiblockDummyCollisionShape(state, level, corePos, dummyPos, context);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    private boolean isLegacyPlateDummy(BlockState state, BlockPos offset) {
        if (offset.equals(BlockPos.ZERO)) {
            return false;
        }
        if (kind == Kind.COMPACT_LAUNCHER) {
            return offset.getX() == 0 || offset.getZ() == 0;
        }
        return isLaunchTablePlateOffset(state.getValue(FACING), offset);
    }

    private static List<BlockPos> squareRingOffsets(int radius) {
        List<BlockPos> offsets = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x != 0 || z != 0) {
                    offsets.add(new BlockPos(x, 0, z));
                }
            }
        }
        return offsets;
    }

    private static boolean isReplaceableOrTemporary(Level level, BlockPos pos, BlockPos temporaryPos) {
        return level.hasChunkAt(pos) && (pos.equals(temporaryPos) || level.getBlockState(pos).canBeReplaced());
    }

    private static void clearLegacyLaunchColumn(Level level, BlockPos corePos, Direction facing) {
        // Old yaw directions 0/1/2/3 become modern core facings N/E/S/W.  The
        // legacy +X/+Z/-X/-Z clearance column is therefore facing.clockWise().
        BlockPos column = corePos.relative(facing.getClockWise(), 3);
        int minBuildHeight = level.getMinBuildHeight();
        int maxBuildHeight = level.getMaxBuildHeight();
        for (int offset = 1; offset < 12; offset++) {
            int y = corePos.getY() + offset;
            if (y >= minBuildHeight && y < maxBuildHeight) {
                level.setBlock(new BlockPos(column.getX(), y, column.getZ()), Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL);
            }
        }
    }

    private static void clearLegacyLaunchCross(Level level, BlockPos corePos) {
        for (int offset = -4; offset <= 4; offset++) {
            if (offset == 0) {
                continue;
            }
            level.setBlock(corePos.offset(offset, 0, 0), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(corePos.offset(0, 0, offset), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static List<BlockPos> compactLauncherProxyOffsets() {
        return List.of(
                new BlockPos(1, 0, 1),
                new BlockPos(1, 0, -1),
                new BlockPos(-1, 0, 1),
                new BlockPos(-1, 0, -1));
    }

    private static List<BlockPos> launchTableProxyOffsets(Direction facing) {
        List<BlockPos> offsets = new ArrayList<>();
        for (BlockPos offset : squareRingOffsets(4)) {
            if (!isLaunchTablePlateOffset(facing, offset)) {
                offsets.add(offset);
            }
        }
        return offsets;
    }

    private static boolean isLaunchTablePlateOffset(Direction facing, BlockPos offset) {
        // LaunchTable#onBlockPlacedBy: legacy d=0/2 (modern NORTH/SOUTH)
        // places ports on the X axis and zero-height plates on the Z axis;
        // d=1/3 (modern EAST/WEST) swaps those axes.  This predicate also
        // controls which dummies get the proxy I/O mode, so it must not merely
        // be visually correct.
        boolean portsAlongX = facing.getAxis() == Direction.Axis.Z;
        return portsAlongX
                ? offset.getX() == 0 && offset.getZ() != 0
                : offset.getX() != 0 && offset.getZ() == 0;
    }

    public enum Kind {
        LAUNCH_TABLE,
        COMPACT_LAUNCHER
    }
}
