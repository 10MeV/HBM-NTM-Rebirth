package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.HbmLegacyWireNodeBlockEntity;
import com.hbm.ntm.blockentity.SubstationBlockEntity;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.ColorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LegacySubstationBlock extends LegacyXrMultiblockBlock implements EntityBlock {
    private static final int[] LEGACY_DIMENSIONS = { 4, 0, 1, 1, 2, 2 };

    public LegacySubstationBlock(Properties properties) {
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
        Direction facing = state.getValue(FACING);
        return LegacyMultiblockLayout.ofLegacyXrChecked(LEGACY_DIMENSIONS, facing)
                .withExtraProxyOffsets(substationExtraOffsets(facing), LegacyProxyMode.passive().conductorProxy());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SubstationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.SUBSTATION.get()) {
            return null;
        }
        return level.isClientSide ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        HbmLegacyWireNodeBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (HbmLegacyWireNodeBlockEntity) blockEntity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer) || !(level.getBlockEntity(pos) instanceof HbmLegacyWireNodeBlockEntity substation)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        int color = ColorUtil.getColorFromDye(stack);
        if (!substation.setWireColor(color)) {
            return InteractionResult.PASS;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof HbmLegacyWireNodeBlockEntity substation) {
            substation.disconnectAllWires();
        }
    }

    private static List<BlockPos> substationExtraOffsets(Direction facing) {
        int ox = facing.getStepX();
        int oz = facing.getStepZ();
        return List.of(
                new BlockPos(ox + 1, 0, oz + 1),
                new BlockPos(ox + 1, 0, oz - 1),
                new BlockPos(ox - 1, 0, oz + 1),
                new BlockPos(ox - 1, 0, oz - 1));
    }
}
