package com.hbm.ntm.item;

import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.network.HbmKeybind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FollyGunItem extends SednaGunItem {
    private static final int CONFIG_INDEX = 0;
    private static final String KEY_TERTIARY = "mouse3_";
    private static final String KEY_SPINUP_TIMER = "folly_spinup_timer";
    private static final int SPINUP_TICKS = 100;

    public FollyGunItem(Properties properties, SednaGunConfig gunConfig) {
        super(properties, gunConfig);
    }

    @Override
    public void handleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind, boolean pressed) {
        if (keybind == HbmKeybind.GUN_TERTIARY) {
            handleTertiary(stack, pressed);
            return;
        }
        super.handleKeybind(player, stack, keybind, pressed);
    }

    private void handleTertiary(ItemStack stack, boolean pressed) {
        if (!handleEdgeKey(stack, KEY_TERTIARY, CONFIG_INDEX, pressed) || !pressed) {
            return;
        }
        if (gunState(stack, CONFIG_INDEX) != SednaGunConfig.GunState.IDLE) {
            return;
        }
        boolean wasAiming = isAiming(stack);
        setAiming(stack, !wasAiming);
        stack.getOrCreateTag().putInt(KEY_SPINUP_TIMER, wasAiming ? 0 : 1);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide) {
            return;
        }
        if (!selected || !isAiming(stack)) {
            stack.getOrCreateTag().putInt(KEY_SPINUP_TIMER, 0);
            return;
        }
        stack.getOrCreateTag().putInt(KEY_SPINUP_TIMER, Math.min(SPINUP_TICKS,
                spinupTimer(stack) + 1));
    }

    @Override
    protected void clickPrimary(ServerPlayer player, ItemStack stack, GunParts gun) {
        if (!isAiming(stack) || spinupTimer(stack) < SPINUP_TICKS) {
            return;
        }
        super.clickPrimary(player, stack, gun);
    }

    @Override
    protected boolean shouldApplyWear(GunParts gun) {
        return false;
    }

    private int spinupTimer(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(KEY_SPINUP_TIMER);
    }
}
