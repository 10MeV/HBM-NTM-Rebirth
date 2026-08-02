package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.SatelliteLinkBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Source-backed 1.7.10 {@code MachineSatLink}: the satellite ground station. */
@SuppressWarnings("deprecation")
public class SatelliteLinkBlock extends LegacyVisibleMultiblockMachineBlock {
    public SatelliteLinkBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SatelliteLinkBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide || player.isShiftKeyDown()) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!(player.getItemInHand(hand).getItem() instanceof ISatelliteChip)) {
            return InteractionResult.PASS;
        }
        BlockEntity core = MultiblockHelper.resolveCoreBlockEntity(level, pos);
        if (!(core instanceof SatelliteLinkBlockEntity link)) {
            return InteractionResult.PASS;
        }
        int frequency = ISatelliteChip.getFreqS(player.getItemInHand(hand));
        link.setFrequency(frequency);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal("Set frequency to " + frequency)
                    .withStyle(ChatFormatting.YELLOW));
        }
        LegacySoundPlayer.playLegacyTechBleep(player, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.MACHINE_SATLINK.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) -> SatelliteLinkBlockEntity.clientTick(tickLevel,
                        tickPos, tickState, (SatelliteLinkBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) -> SatelliteLinkBlockEntity.serverTick(tickLevel,
                        tickPos, tickState, (SatelliteLinkBlockEntity) blockEntity);
    }
}
