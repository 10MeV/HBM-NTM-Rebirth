package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.api.block.Toolable.ToolType;
import com.hbm.ntm.blockentity.AutosawBlockEntity;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class AutosawBlock extends HorizontalMachineBlock implements EntityBlock, Toolable {
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
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        // MachineAutosaw#addInformation delegates to BlockDummyable's standard info tooltip.
        LegacyStandardInfoTooltip.append(tooltip, "machine_autosaw");
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        // TileEntityMachineAutosaw#onBlockActivated first accepts every
        // client prediction. Server-side identifier selection is allowed only
        // while not crouching; crouching itself is consumed without action.
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof AutosawBlockEntity autosaw) {
            if (!player.isShiftKeyDown()) {
                var report = HbmFluidItemTransfer.setTankTypeFromIdentifierStackReport(
                        player.getItemInHand(hand), autosaw.getTank(), level, pos);
                if (report.changed() && autosaw.getTank().getTankType() == report.identifier().selectedType()) {
                    autosaw.markFluidSettingsChanged();
                    player.displayClientMessage(Component.literal("Changed type to ")
                            .append(report.identifier().selectedType().getDisplayName())
                            .append(Component.literal("!")), false);
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.PASS;
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /** 1.7.10 MachineAutosaw#onScrew toggles suspension through the screwdriver item path. */
    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER || !(level.getBlockEntity(pos) instanceof AutosawBlockEntity autosaw)) {
            return false;
        }
        if (!level.isClientSide) {
            autosaw.toggleSuspended();
        }
        return true;
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
