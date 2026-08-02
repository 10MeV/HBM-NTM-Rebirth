package com.hbm.ntm.item;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Direct 1.7.10 {@code RedstoneSword} behavior and client item-renderer bridge. */
public final class LegacyRedstoneSwordItem extends SwordItem {
    public LegacyRedstoneSwordItem(Properties properties) {
        super(Tiers.STONE, 3, -2.4F, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Direction face = context.getClickedFace();
        BlockPos target = context.getClickedPos().relative(face);
        if (player == null || !player.mayUseItemAt(target, face, stack)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide && level.isEmptyBlock(target)) {
            RandomSource random = level.getRandom();
            level.playSound(null, target, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F,
                    random.nextFloat() * 0.4F + 0.8F);
            level.setBlock(target, Blocks.REDSTONE_WIRE.defaultBlockState(), 3);
        }
        if (!level.isClientSide && !player.getAbilities().instabuild) {
            stack.hurtAndBreak(14, player, owner -> owner.broadcastBreakEvent(context.getHand()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptRedstoneSword", consumer);
    }
}
