package com.hbm.ntm.bullet;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class BulletTauTrailUtil {
    public static TauTrailAppend appendClientNode(BulletConfig config, boolean clientSide,
            boolean hasExistingNodes, Vec3 motion) {
        if (config == null || !clientSide || config.style() != BulletStyle.TAU || motion == null) {
            return TauTrailAppend.NONE;
        }

        if (!hasExistingNodes) {
            return new TauTrailAppend(new TauTrailNode(motion.scale(-2.0D), 0.0D), true);
        }
        return new TauTrailAppend(new TauTrailNode(Vec3.ZERO, 1.0D), false);
    }

    public record TauTrailAppend(@Nullable TauTrailNode node, boolean requestIgnoreFrustum) {
        public static final TauTrailAppend NONE = new TauTrailAppend(null, false);

        public boolean appended() {
            return node != null;
        }
    }

    public record TauTrailNode(Vec3 offset, double weight) {
    }

    private BulletTauTrailUtil() {
    }
}
