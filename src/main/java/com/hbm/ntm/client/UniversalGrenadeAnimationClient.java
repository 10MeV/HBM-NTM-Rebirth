package com.hbm.ntm.client;

import com.hbm.ntm.client.anim.LegacyBusAnimation;
import com.hbm.ntm.client.anim.LegacyBusAnimationKeyframe.IType;
import com.hbm.ntm.client.anim.LegacyBusAnimationSequence;
import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import com.hbm.ntm.item.UniversalGrenadeItem;
import com.hbm.ntm.item.UniversalGrenadeItem.Shell;
import net.minecraft.world.item.ItemStack;

/** Exact {@code ItemGrenadeUniversal#getAnimation(EQUIP)} rails. */
public final class UniversalGrenadeAnimationClient {
    public static final short EQUIP = 1;

    public static void handle(ItemStack stack, int selectedSlot, short type, int itemIndex) {
        if (type != EQUIP) {
            return;
        }
        LegacyHbmAnimations.start(selectedSlot, itemIndex, stack.getItem().getDescriptionId(),
                equipAnimation(UniversalGrenadeItem.getShell(stack)), false);
    }

    private static LegacyBusAnimation equipAnimation(Shell shell) {
        return switch (shell) {
            case FRAG, TECH -> new LegacyBusAnimation()
                    .addBus("BODYMOVE", new LegacyBusAnimationSequence().setPos(0.0D, -5.0D, 0.0D)
                            .addPos(0.0D, -3.0D, 0.0D, 350)
                            .addPos(0.0D, 0.0D, 0.0D, 350, IType.SIN_DOWN))
                    .addBus("BODYTURN", new LegacyBusAnimationSequence().addPos(0.0D, 0.0D, 45.0D, 350)
                            .addPos(0.0D, 0.0D, -15.0D, 350, IType.SIN_DOWN).hold(200)
                            .addPos(0.0D, 0.0D, -20.0D, 100, IType.SIN_DOWN)
                            .addPos(0.0D, 0.0D, 0.0D, 500, IType.SIN_FULL))
                    .addBus("RINGMOVE", new LegacyBusAnimationSequence().hold(900)
                            .addPos(0.0D, 0.0D, 1.0D, 150).addPos(0.0D, -3.0D, 3.0D, 300))
                    .addBus("RINGTURN", new LegacyBusAnimationSequence().hold(900)
                            .addPos(0.0D, 0.0D, 45.0D, 300))
                    .addBus("RENDERRING", new LegacyBusAnimationSequence().setPos(1.0D, 1.0D, 1.0D)
                            .hold(1350).setPos(0.0D, 0.0D, 0.0D));
            case STICK -> new LegacyBusAnimation()
                    .addBus("BODYMOVE", new LegacyBusAnimationSequence().setPos(0.0D, -7.0D, 0.0D)
                            .addPos(0.0D, 3.0D, 0.0D, 750, IType.SIN_DOWN).holdUntil(1900)
                            .addPos(0.0D, 0.0D, 0.0D, 250, IType.SIN_FULL))
                    .addBus("BODYTURN", new LegacyBusAnimationSequence().setPos(0.0D, 0.0D, 90.0D)
                            .addPos(0.0D, 0.0D, -45.0D, 750, IType.SIN_DOWN).holdUntil(1900)
                            .addPos(0.0D, 0.0D, 0.0D, 250, IType.SIN_FULL))
                    .addBus("RINGMOVE", new LegacyBusAnimationSequence().hold(800)
                            .addPos(0.0D, -0.25D, 0.0D, 200, IType.SIN_FULL).hold(250)
                            .addPos(0.0D, -0.5D, 0.0D, 200, IType.SIN_FULL)
                            .addPos(2.0D, -5.0D, 0.0D, 350, IType.SIN_UP))
                    .addBus("RINGTURN", new LegacyBusAnimationSequence().hold(800)
                            .addPos(0.0D, 360.0D, 0.0D, 200, IType.SIN_FULL).hold(250)
                            .addPos(0.0D, 720.0D, 0.0D, 200, IType.SIN_FULL))
                    .addBus("RENDERRING", new LegacyBusAnimationSequence().setPos(1.0D, 1.0D, 1.0D)
                            .hold(2100).setPos(0.0D, 0.0D, 0.0D));
            case NUKE -> new LegacyBusAnimation()
                    .addBus("BODYMOVE", new LegacyBusAnimationSequence().setPos(0.0D, -5.0D, 0.0D).hold(250)
                            .addPos(0.0D, 0.0D, 0.0D, 850, IType.SIN_DOWN))
                    .addBus("BODYTURN", new LegacyBusAnimationSequence().setPos(0.0D, 0.0D, 90.0D).hold(250)
                            .addPos(0.0D, 0.0D, -25.0D, 850, IType.SIN_DOWN).hold(200)
                            .addPos(0.0D, 0.0D, -30.0D, 100, IType.SIN_DOWN)
                            .addPos(0.0D, 0.0D, 0.0D, 750, IType.SIN_FULL))
                    .addBus("RINGMOVE", new LegacyBusAnimationSequence().hold(1300)
                            .addPos(0.0D, 0.0D, 1.0D, 150).addPos(0.0D, -3.0D, 3.0D, 300))
                    .addBus("RINGTURN", new LegacyBusAnimationSequence().hold(1300)
                            .addPos(0.0D, 0.0D, 720.0D, 500))
                    .addBus("RENDERRING", new LegacyBusAnimationSequence().setPos(1.0D, 1.0D, 1.0D)
                            .hold(1750).setPos(0.0D, 0.0D, 0.0D));
        };
    }

    private UniversalGrenadeAnimationClient() {
    }
}
