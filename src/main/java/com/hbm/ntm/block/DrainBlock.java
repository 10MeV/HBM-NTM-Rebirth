package com.hbm.ntm.block;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.DrainBlockEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class DrainBlock extends LegacyVisibleMultiblockMachineBlock {
    public DrainBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DrainBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() || !(held.getItem() instanceof IFluidIdentifierItem identifier)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
            BlockPos corePos = core == null ? pos : core.pos();
            if (level.getBlockEntity(corePos) instanceof DrainBlockEntity drain
                    && player instanceof ServerPlayer serverPlayer) {
                FluidType type = identifier.getIdentifiedFluid(level, corePos, held);
                drain.setTankType(type);
                serverPlayer.displayClientMessage(Component.literal("Changed type to ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(type.getDisplayName())
                        .append(Component.literal("!").withStyle(ChatFormatting.YELLOW)), true);
                level.sendBlockUpdated(corePos, level.getBlockState(corePos), level.getBlockState(corePos),
                        Block.UPDATE_CLIENTS);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.DRAIN.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        DrainBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (DrainBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        DrainBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (DrainBlockEntity) blockEntity);
    }
}
