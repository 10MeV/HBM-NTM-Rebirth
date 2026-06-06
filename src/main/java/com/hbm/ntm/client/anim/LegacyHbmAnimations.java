package com.hbm.ntm.client.anim;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class LegacyHbmAnimations {
    public static final int HOTBAR_SLOTS = 9;
    public static final int PARALLEL_RAILS = 8;
    public static final Animation[][] HOTBAR = new Animation[HOTBAR_SLOTS][PARALLEL_RAILS];

    public static void tick() {
        long now = System.currentTimeMillis();
        for (int slot = 0; slot < HOTBAR.length; slot++) {
            for (int rail = 0; rail < HOTBAR[slot].length; rail++) {
                Animation animation = HOTBAR[slot][rail];
                if (animation == null || animation.holdLastFrame()) {
                    continue;
                }
                if (now - animation.startMillis() > animation.animation().getDuration()) {
                    HOTBAR[slot][rail] = null;
                }
            }
        }
    }

    public static void startForSelectedSlot(ItemStack stack, int rail, LegacyBusAnimation animation) {
        startForSelectedSlot(stack, rail, animation, false);
    }

    public static void startForSelectedSlot(ItemStack stack, int rail, LegacyBusAnimation animation, boolean holdLastFrame) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        start(player.getInventory().selected, rail, stack.getItem().getDescriptionId(), animation, holdLastFrame);
    }

    public static void start(int slot, int rail, String key, LegacyBusAnimation animation, boolean holdLastFrame) {
        if (slot < 0 || slot >= HOTBAR.length || rail < 0 || rail >= HOTBAR[slot].length || animation == null) {
            return;
        }
        HOTBAR[slot][rail] = new Animation(key, System.currentTimeMillis(), animation, holdLastFrame);
    }

    public static void clear(int slot, int rail) {
        if (slot < 0 || slot >= HOTBAR.length || rail < 0 || rail >= HOTBAR[slot].length) {
            return;
        }
        HOTBAR[slot][rail] = null;
    }

    public static void clearAll() {
        for (int slot = 0; slot < HOTBAR.length; slot++) {
            for (int rail = 0; rail < HOTBAR[slot].length; rail++) {
                HOTBAR[slot][rail] = null;
            }
        }
    }

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
