package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.MassStorageBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class MassStorageBlock extends HorizontalMachineBlock implements EntityBlock {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 3);

    public MassStorageBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(VARIANT, 0));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(VARIANT, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VARIANT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MassStorageBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.MASS_STORAGE.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) -> {
            if (blockEntity instanceof MassStorageBlockEntity storage) {
                storage.tick();
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
                && level.getBlockEntity(pos) instanceof MassStorageBlockEntity storage) {
            NetworkHooks.openScreen(serverPlayer, storage, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MassStorageBlockEntity storage) {
            storage.loadFromPlacedStack(stack);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MassStorageBlockEntity storage) {
            if (player.getAbilities().instabuild) {
                storage.clearForRemoval();
            } else {
                ItemStack drop = storage.createDroppedStack();
                storage.clearForRemoval();
                Block.popResource(level, pos, drop);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof MassStorageBlockEntity storage) {
                storage.getLooseDrops().forEach(stack -> Block.popResource(level, pos, stack));
            }
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public java.util.List<ItemStack> getDrops(BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        return java.util.List.of();
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MassStorageBlockEntity storage) {
            return storage.redstone();
        }
        return 0;
    }

    public static int capacity(int variant) {
        return switch (variant) {
            case 1 -> 100_000;
            case 2 -> 1_000_000;
            case 3 -> 100;
            default -> 10_000;
        };
    }
}
