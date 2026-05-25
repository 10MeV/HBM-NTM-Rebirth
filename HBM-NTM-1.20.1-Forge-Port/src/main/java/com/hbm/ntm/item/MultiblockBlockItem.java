package com.hbm.ntm.item;

import com.hbm.ntm.client.renderer.LegacyItemRendererBridge;
import com.hbm.ntm.client.renderer.LegacyVisibleMachineItemRenderer;
import com.hbm.ntm.multiblock.LegacyMultiblockPlaceable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class MultiblockBlockItem extends BlockItem {
    public MultiblockBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> LegacyVisibleMachineItemRenderer.INSTANCE);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (!(getBlock() instanceof LegacyMultiblockPlaceable multiblock)) {
            return super.place(context);
        }
        if (!getBlock().isEnabled(context.getLevel().enabledFeatures()) || !context.canPlace()) {
            return InteractionResult.FAIL;
        }

        BlockState state = multiblock.getDirectPlacementState(context);
        if (state == null) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockPos temporaryPos = context.getClickedPos();
        BlockPos corePos = multiblock.getDirectPlacementCore(context, state);
        if (!multiblock.canPlaceDirectMultiblock(level, corePos, temporaryPos, state)) {
            return InteractionResult.FAIL;
        }
        if (!level.setBlock(corePos, state, 11)) {
            return InteractionResult.FAIL;
        }

        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState placedState = level.getBlockState(corePos);
        if (placedState.is(state.getBlock())) {
            updateCustomBlockEntityTag(level, player, corePos, stack);
            multiblock.afterDirectCorePlaced(level, corePos, placedState, player, stack);
            multiblock.completeDirectMultiblockPlacement(level, corePos, placedState, player, stack);
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, corePos, stack);
            }
        }

        SoundType sound = placedState.getSoundType(level, corePos, player);
        level.playSound(player, corePos, getPlaceSound(placedState, level, corePos, player), SoundSource.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, corePos, GameEvent.Context.of(player, placedState));
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
