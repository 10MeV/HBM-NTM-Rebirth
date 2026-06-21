package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.DiFurnaceBlockEntity;
import com.hbm.ntm.blockentity.DiFurnaceExtensionBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class DiFurnaceExtensionBlock extends HorizontalMachineBlock implements EntityBlock {
    public DiFurnaceExtensionBlock(Properties properties) {
        super(properties, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DiFurnaceExtensionBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos.below()) instanceof DiFurnaceBlockEntity furnace) {
            NetworkHooks.openScreen(serverPlayer, furnace, pos.below());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
