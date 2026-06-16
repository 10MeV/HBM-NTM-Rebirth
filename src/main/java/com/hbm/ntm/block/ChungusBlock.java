package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ChungusBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ChungusBlock extends LegacyVisibleMultiblockMachineBlock implements EntityBlock {
    public ChungusBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChungusBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core == null) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!isLeverColumn(core.pos(), core.state(), pos)) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(core.pos()) instanceof ChungusBlockEntity chungus)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            if (!chungus.isOperational()) {
                LegacySoundPlayer.playSoundEffect(level, pos, "hbm:block.chungusLever", 1.5F, 1.0F);
                chungus.onLeverPull();
            } else {
                player.displayClientMessage(
                        Component.literal("Cannot change compressor setting while operational!")
                                .withStyle(ChatFormatting.RED),
                        false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.CHUNGUS.get()) {
            return null;
        }
        if (level.isClientSide) {
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    ChungusBlockEntity.clientTick(tickLevel, tickPos, tickState, (ChungusBlockEntity) blockEntity);
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                ChungusBlockEntity.serverTick(tickLevel, tickPos, tickState, (ChungusBlockEntity) blockEntity);
    }

    private static boolean isLeverColumn(BlockPos corePos, BlockState coreState, BlockPos clickedPos) {
        if (!coreState.hasProperty(HorizontalMachineBlock.FACING)) {
            return false;
        }
        Direction facing = coreState.getValue(HorizontalMachineBlock.FACING);
        Direction turn = facing.getCounterClockWise();
        BlockPos leverA = corePos.relative(facing).relative(turn, 2);
        BlockPos leverB = corePos.relative(facing, 2).relative(turn, 2);
        return clickedPos.getY() < corePos.getY() + 2
                && ((clickedPos.getX() == leverA.getX() && clickedPos.getZ() == leverA.getZ())
                        || (clickedPos.getX() == leverB.getX() && clickedPos.getZ() == leverB.getZ()));
    }
}
