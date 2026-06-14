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
public class CustomMissileLauncherBlock extends LegacyXrMultiblockBlock implements EntityBlock {
    private static final int[] COMPACT_DIMENSIONS = new int[] { 0, 0, 1, 1, 1, 1 };
    private static final int[] TABLE_DIMENSIONS = new int[] { 0, 0, 4, 4, 4, 4 };
    private static final VoxelShape COMPACT_SHAPE = Shapes.box(-1.0D, 0.0D, -1.0D, 2.0D, 1.0D, 2.0D).optimize();
    private static final VoxelShape TABLE_SHAPE = Shapes.box(-4.0D, 0.0D, -4.0D, 5.0D, 1.0D, 5.0D).optimize();

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
                    .withProxyOffsets(squareRingOffsets(4), LegacyProxyMode.combo(true, true, true));
        }
        return LegacyMultiblockLayout.ofLegacyXrChecked(COMPACT_DIMENSIONS, state.getValue(FACING))
                .withProxyOffsets(squareRingOffsets(1), LegacyProxyMode.combo(true, true, true));
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
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof CustomMissileLauncherBlockEntity launcher) {
            NetworkHooks.openScreen(serverPlayer, launcher, pos);
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
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return kind == Kind.LAUNCH_TABLE ? TABLE_SHAPE : COMPACT_SHAPE;
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return kind == Kind.LAUNCH_TABLE ? TABLE_SHAPE : Shapes.empty();
    }

    @Override
    public boolean usesForwardedDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesForwardedDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
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

    public enum Kind {
        LAUNCH_TABLE,
        COMPACT_LAUNCHER
    }
}
