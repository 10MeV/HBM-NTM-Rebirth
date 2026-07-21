package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.SoyuzLauncherBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class SoyuzLauncherBlock extends LegacyVisibleMultiblockMachineBlock {
    public SoyuzLauncherBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoyuzLauncherBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Override
    protected boolean usesUncheckedLegacyDummyFill(BlockState state) {
        // SoyuzLauncher#fillSpace overwrites the completed structure's pad blocks.
        // The automatic struct-core conversion reaches the same source fill path.
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && resolveCoreBlockEntity(level, pos) instanceof SoyuzLauncherBlockEntity launcher) {
            NetworkHooks.openScreen(serverPlayer, launcher, launcher.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.SOYUZ_LAUNCHER.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        SoyuzLauncherBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (SoyuzLauncherBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        SoyuzLauncherBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (SoyuzLauncherBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof SoyuzLauncherBlockEntity launcher) {
            HbmInventoryMenuHelper.spillItems(level, pos, launcher.getItems());
            for (ItemStack stack : launcher.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
