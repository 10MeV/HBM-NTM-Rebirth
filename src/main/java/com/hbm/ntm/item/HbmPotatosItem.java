package com.hbm.ntm.item;

import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HbmPotatosItem extends HbmBatteryItem {
    private static final String TIMER_TAG = "timer";

    public HbmPotatosItem(Properties properties, long maxCharge, long chargeRate, long dischargeRate) {
        super(properties, maxCharge, chargeRate, dischargeRate);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || getCharge(stack) == 0L || !selected || !(entity instanceof Player player)) {
            return;
        }

        int timer = getTimer(stack);
        if (timer > 0) {
            setTimer(stack, timer - 1);
            return;
        }

        float pitch = (float) getCharge(stack) / (float) getMaxCharge(stack) * 0.5F + 0.5F;
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.POTATOS_RANDOM.get(),
                SoundSource.PLAYERS, 1.0F, pitch);
        setTimer(stack, 200 + level.random.nextInt(100));
    }

    private static int getTimer(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(TIMER_TAG);
    }

    private static void setTimer(ItemStack stack, int timer) {
        stack.getOrCreateTag().putInt(TIMER_TAG, timer);
    }
}
