package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.PneumaticStorageMonoBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/** Legacy PneumoStorageMono: filters and their stored amounts travel with the block item. */
public class PneumaticStorageMonoBlock extends BaseEntityBlock {
    public PneumaticStorageMonoBlock(Properties properties) { super(properties); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PneumaticStorageMonoBlockEntity(pos, state); }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.PNEUMATIC_STORAGE_MONO.get(),
                PneumaticStorageMonoBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof PneumaticStorageMonoBlockEntity storage) {
            NetworkHooks.openScreen(serverPlayer, storage, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PneumaticStorageMonoBlockEntity storage) {
            storage.loadFromPlacedStack(stack);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PneumaticStorageMonoBlockEntity storage) {
            if (player.getAbilities().instabuild) storage.clearForRemoval();
            else {
                ItemStack drop = storage.createDroppedStack(asItem());
                storage.clearForRemoval();
                popResource(level, pos, drop);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public java.util.List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        // Legacy removedByPlayer created the populated block item itself; never add a second vanilla loot drop.
        return java.util.List.of();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
}
