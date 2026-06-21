package com.hbm.ntm.util;

import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class LegacyUpgradeSlotSound {
    private LegacyUpgradeSlotSound() {
    }

    public static void playIfUpgrade(BlockEntity blockEntity, int slot, ItemStack stack, int minSlot, int maxSlot,
            double yOffset, float volume, float pitch) {
        if (slot < minSlot || slot > maxSlot || stack.isEmpty()
                || !(stack.getItem() instanceof ItemMachineUpgrade)) {
            return;
        }
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        LegacySoundPlayer.playSoundEffect(level,
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + yOffset,
                blockEntity.getBlockPos().getZ() + 0.5D,
                "hbm:item.upgradePlug", SoundSource.BLOCKS, volume, pitch);
    }
}
