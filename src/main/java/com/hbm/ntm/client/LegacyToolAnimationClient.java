package com.hbm.ntm.client;

import com.hbm.ntm.client.anim.LegacyBusAnimation;
import com.hbm.ntm.client.anim.LegacyBusAnimationKeyframe.IType;
import com.hbm.ntm.client.anim.LegacyBusAnimationSequence;
import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.item.ItemStack;

/** Exact non-Sedna {@code IAnimatedItem<ToolAnimation>} receiver rails. */
public final class LegacyToolAnimationClient {
    private static final short SWING = 0;

    public static void handleBoltgun(ItemStack stack, int selectedSlot, short type, int itemIndex) {
        if (type != SWING) {
            return;
        }
        LegacyHbmAnimations.start(selectedSlot, itemIndex, stack.getItem().getDescriptionId(),
                new LegacyBusAnimation().addBus("RECOIL", new LegacyBusAnimationSequence()
                        .addPos(1.0D, 0.0D, 1.0D, 50)
                        .addPos(0.0D, 0.0D, 1.0D, 100)), false);
    }

    public static void handleChainsaw(ItemStack stack, int selectedSlot, short type, int itemIndex) {
        if (type != SWING) {
            return;
        }

        var active = LegacyHbmAnimations.getRelevantAnim(itemIndex);
        if (active != null && System.currentTimeMillis() - active.startMillis() < 50L) {
            return;
        }

        double[] rotation = active == null ? new double[] { 0.0D, 0.0D, 0.0D }
                : LegacyHbmAnimations.getRelevantTransformation("SWING_ROT", itemIndex);
        double[] translation = active == null ? new double[] { 0.0D, 0.0D, 0.0D }
                : LegacyHbmAnimations.getRelevantTransformation("SWING_TRANS", itemIndex);
        LegacyBusAnimation animation = new LegacyBusAnimation()
                .addBus("SWING_ROT", new LegacyBusAnimationSequence()
                        .setPos(rotation[0], rotation[1], rotation[2])
                        .addPos(0.0D, 0.0D, 90.0D, 150)
                        .addPos(45.0D, 0.0D, 90.0D, 100)
                        .addPos(0.0D, 0.0D, 0.0D, 200))
                .addBus("SWING_TRANS", new LegacyBusAnimationSequence()
                        .setPos(translation[0], translation[1], translation[2])
                        .addPos(0.0D, 0.0D, 3.0D, 150)
                        .addPos(2.0D, 0.0D, 2.0D, 100)
                        .addPos(0.0D, 0.0D, 0.0D, 200));
        LegacyHbmAnimations.start(selectedSlot, itemIndex, stack.getItem().getDescriptionId(), animation, false);
    }

    public static void handleCrucible(ItemStack stack, int selectedSlot, short type, int itemIndex) {
        if (type == 1) {
            LegacyHbmAnimations.start(selectedSlot, itemIndex, stack.getItem().getDescriptionId(),
                    new LegacyBusAnimation().addBus("GUARD_ROT", new LegacyBusAnimationSequence()
                            .addPos(90.0D, 0.0D, 1.0D, 0)
                            .addPos(90.0D, 0.0D, 1.0D, 800)
                            .addPos(0.0D, 0.0D, 1.0D, 50)), false);
            return;
        }
        if (type != SWING || LegacyHbmAnimations.getRelevantTransformation("SWING_ROT", itemIndex)[0] != 0.0D) {
            return;
        }

        int offset = Minecraft.getInstance().player == null ? 0
                : Minecraft.getInstance().player.getRandom().nextInt(80) - 20;
        float pitch = Minecraft.getInstance().player == null ? 0.8F
                : 0.8F + Minecraft.getInstance().player.getRandom().nextFloat() * 0.2F;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.WEAPON_C_SWING.get(), pitch));
        LegacyHbmAnimations.start(selectedSlot, itemIndex, stack.getItem().getDescriptionId(),
                new LegacyBusAnimation()
                        .addBus("SWING_ROT", new LegacyBusAnimationSequence()
                                .addPos(90.0D - offset, 90.0D - offset, 35.0D, 75)
                                .addPos(90.0D + offset, 90.0D - offset, -45.0D, 150)
                                .addPos(0.0D, 0.0D, 0.0D, 500))
                        .addBus("SWING_TRANS", new LegacyBusAnimationSequence()
                                .addPos(-3.0D, 0.0D, 0.0D, 75)
                                .addPos(8.0D, 0.0D, 0.0D, 150)
                                .addPos(0.0D, 0.0D, 0.0D, 500)), false);
    }

    private LegacyToolAnimationClient() {
    }
}
