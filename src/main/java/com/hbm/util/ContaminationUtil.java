package com.hbm.util;


import com.hbm.ntm.radiation.RadiationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;

/**
 * Legacy-name contamination/radiation facade.
 */
@Deprecated(forRemoval = false)
public final class ContaminationUtil {
    @SuppressWarnings("rawtypes")
    public static final HashSet<Class> immuneEntities = new HashSet<>();

    private ContaminationUtil() {
    }

    public static float calculateRadiationMod(LivingEntity entity) {
        return RadiationUtil.calculateRadiationMod(entity);
    }

    public static float getRads(Entity entity) {
        return entity instanceof LivingEntity living ? RadiationUtil.getRads(living) : 0.0F;
    }

    public static boolean isRadImmune(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        return isRegisteredRadImmune(entity) || RadiationUtil.isRadImmune(living);
    }

    @SuppressWarnings("rawtypes")
    public static void registerRadImmune(Class type) {
        if (type != null) {
            immuneEntities.add(type);
        }
    }

    public static boolean isRegisteredRadImmune(Entity entity) {
        if (entity == null) {
            return false;
        }
        for (Class<?> type : immuneEntities) {
            if (type.isAssignableFrom(entity.getClass())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRadiationImmuneMarker(Entity entity) {
        return RadiationUtil.isRadiationImmuneMarker(entity);
    }

    public static void applyAsbestos(Entity entity, int amount) {
        if (entity instanceof LivingEntity living) {
            RadiationUtil.applyAsbestosExposure(living, amount, amount);
        }
    }

    public static void applyDigammaData(Entity entity, float amount) {
        if (entity instanceof LivingEntity living) {
            RadiationUtil.applyDigammaData(living, amount);
        }
    }

    public static void applyDigammaDirect(Entity entity, float amount) {
        if (entity instanceof LivingEntity living) {
            RadiationUtil.applyDigammaDirect(living, amount);
        }
    }

    public static float getDigamma(Entity entity) {
        return entity instanceof LivingEntity living ? RadiationUtil.getDigamma(living) : 0.0F;
    }

    public static void printGeigerData(Player player) {
        RadiationUtil.printGeigerData(player);
    }

    public static void printDosimeterData(Player player) {
        RadiationUtil.printDosimeterData(player);
    }

    public static String getPreffixFromRad(double rads) {
        ChatFormatting format = RadiationUtil.getPreffixFromRad(rads);
        return format.toString();
    }

    public static void printDiagnosticData(Player player) {
        RadiationUtil.printDiagnosticData(player);
    }

    public static boolean contaminate(LivingEntity entity, HazardType hazard, ContaminationType contamination, float amount) {
        return RadiationUtil.contaminate(entity, map(hazard), map(contamination), amount);
    }

    private static com.hbm.ntm.radiation.HazardType map(HazardType hazard) {
        return switch (hazard) {
            case DIGAMMA -> com.hbm.ntm.radiation.HazardType.DIGAMMA;
            case RADIATION -> com.hbm.ntm.radiation.HazardType.RADIATION;
        };
    }

    private static RadiationUtil.ContaminationType map(ContaminationType contamination) {
        return switch (contamination) {
            case FARADAY -> RadiationUtil.ContaminationType.FARADAY;
            case HAZMAT -> RadiationUtil.ContaminationType.HAZMAT;
            case HAZMAT2 -> RadiationUtil.ContaminationType.HAZMAT2;
            case DIGAMMA -> RadiationUtil.ContaminationType.DIGAMMA;
            case DIGAMMA2 -> RadiationUtil.ContaminationType.DIGAMMA2;
            case CREATIVE -> RadiationUtil.ContaminationType.CREATIVE;
            case RAD_BYPASS -> RadiationUtil.ContaminationType.RAD_BYPASS;
            case NONE -> RadiationUtil.ContaminationType.NONE;
        };
    }

    public enum HazardType {
        RADIATION,
        DIGAMMA
    }

    public enum ContaminationType {
        FARADAY,
        HAZMAT,
        HAZMAT2,
        DIGAMMA,
        DIGAMMA2,
        CREATIVE,
        RAD_BYPASS,
        NONE
    }
}
