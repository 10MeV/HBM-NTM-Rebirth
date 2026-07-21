package com.hbm.ntm.item;

import com.hbm.ntm.entity.projectile.DisperserCanisterEntity;
import com.hbm.ntm.fluid.HbmFluidContainerRules;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Source carrier for 1.7.10 {@code ItemDisperser}. */
public final class DisperserCanisterItem extends HbmFluidContainerItem {
    public DisperserCanisterItem(Properties properties) {
        super(properties, HbmFluidContainerRules.ContainerKind.DISPERSER_CANISTER,
                HbmFluidContainerRules.DISPERSER_CAPACITY);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack thrown = stack.copy();
        thrown.setCount(1);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            DisperserCanisterEntity canister = new DisperserCanisterEntity(level, player);
            canister.setItem(thrown);
            canister.setFluidType(getFirstFluidType(thrown));
            canister.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(canister);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
