package com.hbm.ntm.block;

import com.hbm.ntm.api.block.IBomb;
import com.hbm.ntm.blockentity.BlastDoorBlockEntity;
import com.hbm.ntm.blockentity.BlastDoorDummyBlockEntity;
import com.hbm.ntm.item.PadlockItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Controller-owned vertical blast-door cell. Its local full cube remains the old dummy collision/outline. */
@SuppressWarnings("deprecation")
public class BlastDoorDummyBlock extends BaseEntityBlock implements IBomb {
    public BlastDoorDummyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof PadlockItem || held.is(ModItems.KEY_KIT.get())) {
            return InteractionResult.PASS;
        }
        if (!player.isShiftKeyDown() && resolveCore(level, pos) instanceof BlastDoorBlockEntity door) {
            door.tryToggle(player);
            return InteractionResult.CONSUME;
        }
        // Old DummyBlockBlast consumed a crouching click after server-side handling.
        return InteractionResult.CONSUME;
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        if (resolveCore(level, pos) instanceof BlastDoorBlockEntity door && !door.isLocked()) {
            door.tryToggle(null);
            return BombReturnCode.TRIGGERED;
        }
        return BombReturnCode.ERROR_INCOMPATIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlastDoorDummyBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide && !BlastDoorBlockEntity.isClearingDummies()
                && resolveCore(level, pos) instanceof BlastDoorBlockEntity door) {
            level.destroyBlock(door.getBlockPos(), true);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    private static BlockEntity resolveCore(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BlastDoorDummyBlockEntity dummy
                ? level.getBlockEntity(dummy.corePos()) : null;
    }
}
