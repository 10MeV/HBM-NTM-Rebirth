package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.blockentity.RadarScreenBlockEntity;
import com.hbm.ntm.api.entity.RadarScreenDisplayProfile;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class RadarScreenBlock extends LegacyVisibleMultiblockMachineBlock {
    public RadarScreenBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadarScreenBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.getItemInHand(hand).is(ModItems.RADAR_LINKER.get())) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide
                && player instanceof ServerPlayer serverPlayer
                && MultiblockHelper.resolveCoreBlockEntity(level, pos) instanceof RadarScreenBlockEntity screen) {
            var radarPos = RadarScreenDisplayProfile.linkedRadarPos(screen.getSnapshot());
            if (radarPos.isPresent()
                    && level.getBlockEntity(radarPos.get()) instanceof RadarBlockEntity radar) {
                NetworkHooks.openScreen(serverPlayer, radar, radarPos.get());
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return type == ModBlockEntities.MACHINE_RADAR_SCREEN.get() && !level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                RadarScreenBlockEntity.serverTick(tickLevel, tickPos, tickState, (RadarScreenBlockEntity) blockEntity)
                : null;
    }
}
