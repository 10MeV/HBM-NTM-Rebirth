package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.IndustrialSteamTurbineBlockEntity;
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
public class SteamTurbineMultiblockBlock extends LegacyVisibleMultiblockMachineBlock implements EntityBlock {
    private final Kind kind;

    public SteamTurbineMultiblockBlock(Properties properties, LegacyMachineDefinition definition, Kind kind) {
        super(properties, definition);
        this.kind = kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IndustrialSteamTurbineBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (kind != Kind.INDUSTRIAL || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core == null) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!isIndustrialLever(core.pos(), core.state(), pos)) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(core.pos()) instanceof IndustrialSteamTurbineBlockEntity turbine)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            if (!turbine.isOperational()) {
                LegacySoundPlayer.playSoundEffect(level, pos, "hbm:block.chungusLever", 1.5F, 1.0F);
                turbine.onLeverPull();
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
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (kind == Kind.INDUSTRIAL && type == ModBlockEntities.INDUSTRIAL_STEAM_TURBINE.get()) {
            if (level.isClientSide) {
                return (tickLevel, tickPos, tickState, blockEntity) ->
                        IndustrialSteamTurbineBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (IndustrialSteamTurbineBlockEntity) blockEntity);
            }
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    IndustrialSteamTurbineBlockEntity.serverTick(tickLevel, tickPos, tickState,
                            (IndustrialSteamTurbineBlockEntity) blockEntity);
        }
        return null;
    }

    public enum Kind {
        INDUSTRIAL
    }

    private static boolean isIndustrialLever(BlockPos corePos, BlockState coreState, BlockPos clickedPos) {
        if (!coreState.hasProperty(HorizontalMachineBlock.FACING)) {
            return false;
        }
        Direction facing = coreState.getValue(HorizontalMachineBlock.FACING);
        BlockPos lever = corePos.relative(facing, 3).above();
        return clickedPos.getX() == lever.getX()
                && clickedPos.getY() == lever.getY()
                && clickedPos.getZ() == lever.getZ();
    }
}
