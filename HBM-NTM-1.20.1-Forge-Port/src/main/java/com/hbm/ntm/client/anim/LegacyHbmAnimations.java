package com.hbm.ntm.client.anim;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class LegacyHbmAnimations {
    public static final Animation[][] HOTBAR = new Animation[9][8];

    public static Animation getRelevantAnim() {
        return getRelevantAnim(0);
    }

    public static Animation getRelevantAnim(int index) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || index < 0 || index >= HOTBAR[0].length) {
            return null;
        }

        int slot = player.getInventory().selected;
        if (slot < 0 || slot > 8) {
            slot = Math.abs(slot) % 9;
        }

        Animation animation = HOTBAR[slot][index];
        ItemStack stack = player.getMainHandItem();
        if (animation == null || stack.isEmpty()) {
            return null;
        }
        return animation.key().equals(stack.getItem().getDescriptionId()) ? animation : null;
    }

    public static double[] getRelevantTransformation(String bus) {
        return getRelevantTransformation(bus, 0);
    }

    public static double[] getRelevantTransformation(String bus, int index) {
        Animation animation = getRelevantAnim(index);
        if (animation == null) {
            return LegacyBusAnimationTransforms.identity();
        }

        LegacyBusAnimationSequence sequence = animation.animation().getBus(bus);
        if (sequence == null) {
            return LegacyBusAnimationTransforms.identity();
        }

        int millis = (int) (System.currentTimeMillis() - animation.startMillis());
        return sequence.getTransformation(millis);
    }

    public static void applyRelevantTransformation(String bus, PoseStack poseStack) {
        applyRelevantTransformation(bus, 0, poseStack);
    }

    public static void applyRelevantTransformation(String bus, int index, PoseStack poseStack) {
        LegacyBusAnimationTransforms.apply(poseStack, getRelevantTransformation(bus, index));
    }

    public record Animation(String key, long startMillis, LegacyBusAnimation animation, boolean holdLastFrame) {
        public Animation(String key, long startMillis, LegacyBusAnimation animation) {
            this(key, startMillis, animation, false);
        }
    }

    private LegacyHbmAnimations() {
    }
}
