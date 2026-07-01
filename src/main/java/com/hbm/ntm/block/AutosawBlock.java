package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable.ToolType;
import com.hbm.ntm.blockentity.AutosawBlockEntity;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
public class AutosawBlock extends HorizontalMachineBlock implements EntityBlock {
    public AutosawBlock(Properties properties) {
        super(properties, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AutosawBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof AutosawBlockEntity autosaw) {
            if (ToolType.getType(player.getItemInHand(hand)) == ToolType.SCREWDRIVER) {
                if (!level.isClientSide) {
                    autosaw.toggleSuspended();
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!level.isClientSide && player.isShiftKeyDown()) {
                var report = HbmFluidItemTransfer.setTankTypeFromIdentifierStackReport(
                        player.getItemInHand(hand), autosaw.getTank(), level, pos);
                if (report.changed()) {
                    autosaw.markFluidSettingsChanged();
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.AUTOSAW.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                AutosawBlockEntity.clientTick(tickLevel, tickPos, tickState, (AutosawBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                AutosawBlockEntity.serverTick(tickLevel, tickPos, tickState, (AutosawBlockEntity) blockEntity);
    }
}
