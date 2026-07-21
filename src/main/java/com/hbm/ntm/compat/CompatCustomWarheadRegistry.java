package com.hbm.ntm.compat;

import com.hbm.ntm.explosion.CustomMissileExplosion;
import java.util.function.Consumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Modern landing point for the 1.7.10 CompatExternal custom warhead hooks.
 */
public final class CompatCustomWarheadRegistry {
    public static void setLabel(CustomMissileExplosion.WarheadType type, String label) {
        // Legacy external custom-warhead callbacks are a non-tag mod integration.
        // Keep this source facade inert while that integration remains frozen.
    }

    @Nullable
    public static String label(CustomMissileExplosion.WarheadType type) {
        return null;
    }

    public static void setImpact(CustomMissileExplosion.WarheadType type, Consumer<WarheadContext> impact) {
        // See setLabel: source compatibility only, no runtime registration.
    }

    public static void setUpdate(CustomMissileExplosion.WarheadType type, Consumer<WarheadContext> update) {
        // See setLabel: source compatibility only, no runtime registration.
    }

    public static boolean tryImpact(Level level, double x, double y, double z, Vec3 motion, float strength,
            CustomMissileExplosion.WarheadType type, @Nullable Entity source) {
        return false;
    }

    public static boolean runUpdate(WarheadContext context) {
        return false;
    }

    public static Diagnostics diagnostics() {
        return new Diagnostics(0, 0, 0);
    }

    public static boolean isCustom(CustomMissileExplosion.WarheadType type) {
        return type != null && type.name().startsWith("CUSTOM");
    }

    public record WarheadContext(Level level, double x, double y, double z, Vec3 motion, float strength,
                                 CustomMissileExplosion.WarheadType type, @Nullable Entity source) {
    }

    public record Diagnostics(int labels, int impactHandlers, int updateHandlers) {
        public int totalRegistrations() {
            return labels + impactHandlers + updateHandlers;
        }

        public String summary() {
            return "custom warheads labels=" + labels
                    + " impacts=" + impactHandlers
                    + " updates=" + updateHandlers;
        }
    }

    private CompatCustomWarheadRegistry() {
    }
}
