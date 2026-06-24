package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.StorageCrateBlockEntity;
import com.hbm.ntm.item.PadlockItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CrateBlock extends Block implements EntityBlock {
    private final StorageCrateBlockEntity.Kind kind;

    public CrateBlock(Properties properties, StorageCrateBlockEntity.Kind kind) {
        super(properties);
        this.kind = kind;
    }

    public StorageCrateBlockEntity.Kind kind() {
        return kind;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StorageCrateBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return (tickLevel, tickPos, tickState, blockEntity) -> {
            if (blockEntity instanceof StorageCrateBlockEntity crate) {
                crate.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof StorageCrateBlockEntity crate) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof PadlockItem) {
                return crate.tryApplyPadlock(player, held) ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
            if (held.is(ModItems.KEY_KIT.get())) {
                return crate.tryCreateCounterfeitKeys(player, hand) ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
            if (crate.canAccess(player, held)) {
                NetworkHooks.openScreen(serverPlayer, crate, pos);
                crate.triggerSpiders(player);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof StorageCrateBlockEntity crate) {
            crate.loadFromPlacedStack(stack);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof StorageCrateBlockEntity crate) {
            if (player.getAbilities().instabuild) {
                crate.clearItems();
            } else {
                ItemStack drop = crate.createDroppedStack();
                crate.clearItems();
                Block.popResource(level, pos, drop);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof StorageCrateBlockEntity crate) {
                crate.getDrops().forEach(stack -> Block.popResource(level, pos, stack));
            }
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof StorageCrateBlockEntity crate) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer(crate.asContainerView());
        }
        return 0;
    }

    @Override
    public java.util.List<ItemStack> getDrops(BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        return java.util.List.of();
    }

}
