package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.HbmLegacyWireNodeBlockEntity;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.util.ColorUtil;
import net.minecraft.core.BlockPos;
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

@SuppressWarnings("deprecation")
public abstract class LegacyPylonBlock extends LegacyXrMultiblockBlock implements EntityBlock {
    private final int[] dimensions;

    protected LegacyPylonBlock(Properties properties, int[] dimensions) {
        super(properties);
        this.dimensions = dimensions;
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return dimensions;
    }

    @Override
    protected int getLegacyOffset() {
        return 0;
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        return LegacyMultiblockLayout.ofLegacyXrChecked(dimensions, state.getValue(FACING));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
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
        if (!(player instanceof ServerPlayer) || !(level.getBlockEntity(pos) instanceof HbmLegacyWireNodeBlockEntity pylon)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        int color = ColorUtil.getColorFromDye(stack);
        if (!pylon.setWireColor(color)) {
            return InteractionResult.PASS;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof HbmLegacyWireNodeBlockEntity pylon) {
            pylon.disconnectAllWires();
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != getPylonBlockEntityType()) {
            return null;
        }
        return level.isClientSide ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        HbmLegacyWireNodeBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (HbmLegacyWireNodeBlockEntity) blockEntity);
    }

    protected abstract BlockEntityType<?> getPylonBlockEntityType();
}
