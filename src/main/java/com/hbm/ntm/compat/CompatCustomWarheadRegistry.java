package com.hbm.ntm.compat;

import com.hbm.ntm.explosion.CustomMissileExplosion;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Modern landing point for the 1.7.10 CompatExternal custom warhead hooks.
 */
public final class CompatCustomWarheadRegistry {
    private static final Map<CustomMissileExplosion.WarheadType, String> LABELS =
            new EnumMap<>(CustomMissileExplosion.WarheadType.class);
    private static final Map<CustomMissileExplosion.WarheadType, Consumer<WarheadContext>> IMPACTS =
            new EnumMap<>(CustomMissileExplosion.WarheadType.class);
    private static final Map<CustomMissileExplosion.WarheadType, Consumer<WarheadContext>> UPDATES =
            new EnumMap<>(CustomMissileExplosion.WarheadType.class);

    public static void setLabel(CustomMissileExplosion.WarheadType type, String label) {
        if (!isCustom(type)) {
            return;
        }
        if (label == null || label.isBlank()) {
            LABELS.remove(type);
        } else {
            LABELS.put(type, label);
        }
    }

    @Nullable
    public static String label(CustomMissileExplosion.WarheadType type) {
        return LABELS.get(type);
    }

    public static void setImpact(CustomMissileExplosion.WarheadType type, Consumer<WarheadContext> impact) {
        setHandler(type, impact, IMPACTS);
    }

    public static void setUpdate(CustomMissileExplosion.WarheadType type, Consumer<WarheadContext> update) {
        setHandler(type, update, UPDATES);
    }

    public static boolean tryImpact(Level level, double x, double y, double z, Vec3 motion, float strength,
            CustomMissileExplosion.WarheadType type, @Nullable Entity source) {
        Consumer<WarheadContext> impact = IMPACTS.get(type);
        if (impact == null) {
            return false;
        }
        impact.accept(new WarheadContext(level, x, y, z, motion, strength, type, source));
        return true;
    }

    public static boolean runUpdate(WarheadContext context) {
        if (context == null) {
            return false;
        }
        Consumer<WarheadContext> update = UPDATES.get(context.type());
        if (update == null) {
            return false;
        }
        update.accept(context);
        return true;
    }

    public static Diagnostics diagnostics() {
        return new Diagnostics(LABELS.size(), IMPACTS.size(), UPDATES.size());
    }

    public static boolean isCustom(CustomMissileExplosion.WarheadType type) {
        return type != null && type.name().startsWith("CUSTOM");
    }

    private static void setHandler(CustomMissileExplosion.WarheadType type, Consumer<WarheadContext> handler,
            Map<CustomMissileExplosion.WarheadType, Consumer<WarheadContext>> handlers) {
        if (!isCustom(type)) {
            return;
        }
        if (handler == null) {
            handlers.remove(type);
        } else {
            handlers.put(type, handler);
        }
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
