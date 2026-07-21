package com.hbm.ntm.client;

import com.hbm.ntm.item.StingerGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Direct client-side equivalent of ItemGunStinger.prevLockon/lockon. */
public final class LegacyStingerLockonProgress {
    private static final float LOCKON_STEP = 1.0F / 60.0F;

    private static float previous;
    private static float current;

    private LegacyStingerLockonProgress() {
    }

    /** Mirrors the client branch of ItemGunStinger#onUpdate. */
    public static void tick(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null) {
            clear();
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof StingerGunItem stinger)) {
            clear();
            return;
        }
        previous = current;
        if (stinger.legacyLockonProgress(stack) > 1) {
            current += LOCKON_STEP;
        } else {
            current = 0.0F;
        }
    }

    public static float previous() {
        return previous;
    }

    public static float current() {
        return current;
    }

    private static void clear() {
        previous = 0.0F;
        current = 0.0F;
    }
}
