package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ArcFurnaceBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ArcFurnaceBlock extends LegacyVisibleMultiblockMachineBlock {
    public ArcFurnaceBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcFurnaceBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown()
                && resolveCoreBlockEntity(level, pos) instanceof ArcFurnaceBlockEntity furnace) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof ShovelItem && furnace.getLiquidAmount() > 0) {
                HbmInventoryMenuHelper.giveOrDropAll(player, furnace.drainLiquidsAsScraps(), level,
                        hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
                return InteractionResult.CONSUME;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, furnace, furnace.getBlockPos());
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.ARC_FURNACE.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) -> ArcFurnaceBlockEntity.clientTick(
                        tickLevel, tickPos, tickState, (ArcFurnaceBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) -> ArcFurnaceBlockEntity.serverTick(
                        tickLevel, tickPos, tickState, (ArcFurnaceBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ArcFurnaceBlockEntity furnace) {
            for (ItemStack stack : furnace.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
