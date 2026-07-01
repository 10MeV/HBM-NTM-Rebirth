package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.CrucibleBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
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
public class CrucibleBlock extends LegacyVisibleMultiblockMachineBlock {
    public CrucibleBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrucibleBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide
                && MultiblockHelper.resolveCoreBlockEntity(level, pos) instanceof CrucibleBlockEntity crucible) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof ShovelItem && crucible.getTotalMaterialAmount() > 0) {
                for (ItemStack stack : crucible.drainAllAsScraps()) {
                    HbmInventoryMenuHelper.giveOrDrop(player, stack);
                }
                return InteractionResult.CONSUME;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, crucible, crucible.getBlockPos());
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.CRUCIBLE.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) -> CrucibleBlockEntity.clientTick(
                        tickLevel, tickPos, tickState, (CrucibleBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) -> CrucibleBlockEntity.serverTick(
                        tickLevel, tickPos, tickState, (CrucibleBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
            for (ItemStack stack : crucible.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
