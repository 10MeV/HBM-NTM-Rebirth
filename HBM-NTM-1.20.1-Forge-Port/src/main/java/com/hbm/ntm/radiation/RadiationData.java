package com.hbm.ntm.radiation;

import com.hbm.ntm.config.RadiationConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RadiationData {
    public static final UUID DIGAMMA_UUID = UUID.fromString("2a3d8aec-5ab9-4218-9b8b-ca812bdf378b");
    public static final int MAX_ASBESTOS = 60 * 60 * 20;
    public static final int MAX_BLACK_LUNG = 2 * 60 * 60 * 20;
    private static final String TAG_ROOT = "HbmLivingProps";
    private static final String TAG_PREVIOUS_ROOT = "hbm_radiation";
    private static final String TAG_RADIATION = "hfr_radiation";
    private static final String TAG_DIGAMMA = "hfr_digamma";
    private static final String TAG_RAD_ENV = "hfr_rad_env";
    private static final String TAG_RAD_BUF = "hfr_rad_buf";
    private static final String TAG_ASBESTOS = "hfr_asbestos";
    private static final String TAG_BOMB_TIMER = "hfr_bomb";
    private static final String TAG_CONTAGION = "hfr_contagion";
    private static final String TAG_BLACK_LUNG = "hfr_blacklung";
    private static final String TAG_OIL = "hfr_oil";
    private static final String TAG_FIRE = "hfr_fire";
    private static final String TAG_PHOSPHORUS = "hfr_phosphorus";
    private static final String TAG_BALEFIRE = "hfr_balefire";
    private static final String TAG_BLACK_FIRE = "hfr_blackfire";
    private static final String TAG_CONTAMINATION = "hfr_contamination";
    private static final String TAG_LEGACY_CONTAMINATION_COUNT = "hfr_cont_count";
    private static final String TAG_CONTAMINATION_MAX_RAD = "maxRad";
    private static final String TAG_CONTAMINATION_MAX_TIME = "maxTime";
    private static final String TAG_CONTAMINATION_TIME = "time";
    private static final String TAG_CONTAMINATION_IGNORE_ARMOR = "ignoreArmor";

    public static float getRadiation(LivingEntity entity) {
        if (!RadiationConfig.ENABLE_CONTAMINATION.get()) {
            return 0.0F;
        }
        return getTag(entity).getFloat(TAG_RADIATION);
    }

    public static void setRadiation(LivingEntity entity, float radiation) {
        if (RadiationConfig.ENABLE_CONTAMINATION.get()) {
            getTag(entity).putFloat(TAG_RADIATION, clampPlayerRadiation(radiation));
        }
    }

    public static void incrementRadiation(LivingEntity entity, float amount) {
        if (!RadiationConfig.ENABLE_CONTAMINATION.get()) {
            return;
        }
        setRadiation(entity, getRadiation(entity) + amount);
    }

    public static float getDigamma(LivingEntity entity) {
        return getTag(entity).getFloat(TAG_DIGAMMA);
    }

    public static void setDigamma(LivingEntity entity, float digamma) {
        getTag(entity).putFloat(TAG_DIGAMMA, Mth.clamp(digamma, 0.0F, 10.0F));
        applyDigammaModifier(entity);
    }

    public static void incrementDigamma(LivingEntity entity, float amount) {
        setDigamma(entity, getDigamma(entity) + amount);
    }

    public static void applyDigammaModifier(LivingEntity entity) {
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        maxHealth.removeModifier(DIGAMMA_UUID);

        float digamma = getDigamma(entity);
        if (digamma <= 0.0F) {
            return;
        }

        double healthModifier = Math.pow(0.5D, digamma) - 1.0D;
        maxHealth.addTransientModifier(new AttributeModifier(DIGAMMA_UUID, "digamma", healthModifier, AttributeModifier.Operation.MULTIPLY_TOTAL));
        if (entity.getHealth() > entity.getMaxHealth() && entity.getMaxHealth() > 0.0F) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    public static float getRadEnv(LivingEntity entity) {
        return getTag(entity).getFloat(TAG_RAD_ENV);
    }

    public static void setRadEnv(LivingEntity entity, float radiation) {
        getTag(entity).putFloat(TAG_RAD_ENV, radiation);
    }

    public static float getRadBuf(LivingEntity entity) {
        return getTag(entity).getFloat(TAG_RAD_BUF);
    }

    public static void setRadBuf(LivingEntity entity, float radiation) {
        getTag(entity).putFloat(TAG_RAD_BUF, radiation);
    }

    public static void flushEnvironmentBuffer(LivingEntity entity) {
        setRadBuf(entity, getRadEnv(entity));
        setRadEnv(entity, 0.0F);
    }

    public static int getAsbestos(LivingEntity entity) {
        if (RadiationConfig.DISABLE_ASBESTOS.get()) {
            return 0;
        }
        return getTag(entity).getInt(TAG_ASBESTOS);
    }

    public static void setAsbestos(LivingEntity entity, int asbestos) {
        if (RadiationConfig.DISABLE_ASBESTOS.get()) {
            return;
        }
        int value = Math.max(0, asbestos);
        if (value >= MAX_ASBESTOS) {
            getTag(entity).putInt(TAG_ASBESTOS, 0);
            entity.hurt(ModDamageSources.asbestos(entity.level()), 1000.0F);
        } else {
            getTag(entity).putInt(TAG_ASBESTOS, value);
        }
    }

    public static void incrementAsbestos(LivingEntity entity, int amount) {
        setAsbestos(entity, getAsbestos(entity) + amount);
    }

    public static int getBombTimer(LivingEntity entity) {
        return getTag(entity).getInt(TAG_BOMB_TIMER);
    }

    public static void setBombTimer(LivingEntity entity, int bombTimer) {
        getTag(entity).putInt(TAG_BOMB_TIMER, bombTimer);
    }

    public static int getContagion(LivingEntity entity) {
        return getTag(entity).getInt(TAG_CONTAGION);
    }

    public static void setContagion(LivingEntity entity, int contagion) {
        getTag(entity).putInt(TAG_CONTAGION, Math.max(0, contagion));
    }

    public static int getBlackLung(LivingEntity entity) {
        if (RadiationConfig.DISABLE_COAL.get()) {
            return 0;
        }
        return getTag(entity).getInt(TAG_BLACK_LUNG);
    }

    public static void setBlackLung(LivingEntity entity, int blackLung) {
        if (RadiationConfig.DISABLE_COAL.get()) {
            return;
        }
        int value = Math.max(0, blackLung);
        if (value >= MAX_BLACK_LUNG) {
            getTag(entity).putInt(TAG_BLACK_LUNG, 0);
            entity.hurt(ModDamageSources.blackLung(entity.level()), 1000.0F);
        } else {
            getTag(entity).putInt(TAG_BLACK_LUNG, value);
        }
    }

    public static void incrementBlackLung(LivingEntity entity, int amount) {
        setBlackLung(entity, getBlackLung(entity) + amount);
    }

    public static int getOil(LivingEntity entity) {
        return getTag(entity).getInt(TAG_OIL);
    }

    public static void setOil(LivingEntity entity, int oil) {
        getTag(entity).putInt(TAG_OIL, Math.max(0, oil));
    }

    public static int getFire(LivingEntity entity) {
        return getTag(entity).getInt(TAG_FIRE);
    }

    public static void setFire(LivingEntity entity, int fire) {
        getTag(entity).putInt(TAG_FIRE, Math.max(0, fire));
    }

    public static int getPhosphorus(LivingEntity entity) {
        return getTag(entity).getInt(TAG_PHOSPHORUS);
    }

    public static void setPhosphorus(LivingEntity entity, int phosphorus) {
        getTag(entity).putInt(TAG_PHOSPHORUS, Math.max(0, phosphorus));
    }

    public static int getBalefire(LivingEntity entity) {
        return getTag(entity).getInt(TAG_BALEFIRE);
    }

    public static void setBalefire(LivingEntity entity, int balefire) {
        getTag(entity).putInt(TAG_BALEFIRE, Math.max(0, balefire));
    }

    public static int getBlackFire(LivingEntity entity) {
        return getTag(entity).getInt(TAG_BLACK_FIRE);
    }

    public static void setBlackFire(LivingEntity entity, int blackFire) {
        getTag(entity).putInt(TAG_BLACK_FIRE, Math.max(0, blackFire));
    }

    public static ListTag getContamination(LivingEntity entity) {
        CompoundTag tag = getTag(entity);
        ListTag contamination = new ListTag();
        int count = tag.getInt(TAG_LEGACY_CONTAMINATION_COUNT);
        for (int i = 0; i < count; i++) {
            String key = "cont_" + i;
            if (tag.contains(key, Tag.TAG_COMPOUND)) {
                contamination.add(tag.getCompound(key).copy());
            }
        }
        return contamination;
    }

    public static void setContamination(LivingEntity entity, ListTag contamination) {
        CompoundTag tag = getTag(entity);
        clearLegacyContamination(tag);
        tag.putInt(TAG_LEGACY_CONTAMINATION_COUNT, contamination.size());
        for (int i = 0; i < contamination.size(); i++) {
            tag.put("cont_" + i, contamination.getCompound(i).copy());
        }
    }

    public static void addContamination(LivingEntity entity, float maxRad, int maxTime, int time, boolean ignoreArmor) {
        CompoundTag effect = new CompoundTag();
        effect.putFloat(TAG_CONTAMINATION_MAX_RAD, maxRad);
        effect.putInt(TAG_CONTAMINATION_MAX_TIME, Math.max(1, maxTime));
        effect.putInt(TAG_CONTAMINATION_TIME, Mth.clamp(time, 0, Math.max(1, maxTime)));
        effect.putBoolean(TAG_CONTAMINATION_IGNORE_ARMOR, ignoreArmor);
        ListTag effects = getContamination(entity).copy();
        effects.add(effect);
        setContamination(entity, effects);
    }

    public static List<ContaminationEffect> getContaminationEffects(LivingEntity entity) {
        ListTag contamination = getContamination(entity);
        List<ContaminationEffect> effects = new ArrayList<>(contamination.size());
        for (int i = 0; i < contamination.size(); i++) {
            CompoundTag effect = contamination.getCompound(i);
            effects.add(new ContaminationEffect(
                    effect.getFloat(TAG_CONTAMINATION_MAX_RAD),
                    Math.max(1, effect.getInt(TAG_CONTAMINATION_MAX_TIME)),
                    Math.max(0, effect.getInt(TAG_CONTAMINATION_TIME)),
                    effect.getBoolean(TAG_CONTAMINATION_IGNORE_ARMOR)));
        }
        return effects;
    }

    public static int getContaminationCount(LivingEntity entity) {
        return getContamination(entity).size();
    }

    public static boolean removeContamination(LivingEntity entity, int index) {
        ListTag contamination = getContamination(entity).copy();
        if (index < 0 || index >= contamination.size()) {
            return false;
        }
        contamination.remove(index);
        setContamination(entity, contamination);
        return true;
    }

    public static int clearContamination(LivingEntity entity) {
        int count = getContaminationCount(entity);
        setContamination(entity, new ListTag());
        return count;
    }

    public static void copyForRespawn(LivingEntity original, LivingEntity replacement) {
        CompoundTag originalData = original.getPersistentData();
        if (originalData.contains(TAG_ROOT, Tag.TAG_COMPOUND)) {
            replacement.getPersistentData().put(TAG_ROOT, originalData.getCompound(TAG_ROOT).copy());
        } else if (originalData.contains(TAG_PREVIOUS_ROOT, Tag.TAG_COMPOUND)) {
            replacement.getPersistentData().put(TAG_ROOT, originalData.getCompound(TAG_PREVIOUS_ROOT).copy());
        }
    }

    private static CompoundTag getTag(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        if (!persistentData.contains(TAG_ROOT, Tag.TAG_COMPOUND)) {
            CompoundTag tag = persistentData.contains(TAG_PREVIOUS_ROOT, Tag.TAG_COMPOUND)
                    ? persistentData.getCompound(TAG_PREVIOUS_ROOT).copy()
                    : new CompoundTag();
            migrateTemporaryContaminationList(tag);
            persistentData.put(TAG_ROOT, tag);
        } else {
            migrateTemporaryContaminationList(persistentData.getCompound(TAG_ROOT));
        }
        return persistentData.getCompound(TAG_ROOT);
    }

    private static void migrateTemporaryContaminationList(CompoundTag tag) {
        if (!tag.contains(TAG_CONTAMINATION, Tag.TAG_LIST)) {
            return;
        }
        if (tag.getInt(TAG_LEGACY_CONTAMINATION_COUNT) > 0) {
            tag.remove(TAG_CONTAMINATION);
            return;
        }

        ListTag contamination = tag.getList(TAG_CONTAMINATION, Tag.TAG_COMPOUND);
        tag.putInt(TAG_LEGACY_CONTAMINATION_COUNT, contamination.size());
        for (int i = 0; i < contamination.size(); i++) {
            tag.put("cont_" + i, contamination.getCompound(i).copy());
        }
        tag.remove(TAG_CONTAMINATION);
    }

    private static void clearLegacyContamination(CompoundTag tag) {
        int count = tag.getInt(TAG_LEGACY_CONTAMINATION_COUNT);
        for (int i = 0; i < count; i++) {
            tag.remove("cont_" + i);
        }
        tag.putInt(TAG_LEGACY_CONTAMINATION_COUNT, 0);
        tag.remove(TAG_CONTAMINATION);
    }

    private static float clampPlayerRadiation(float value) {
        return Mth.clamp(value, 0.0F, RadiationConstants.MAX_PLAYER_RADIATION);
    }

    public record ContaminationEffect(float maxRad, int maxTime, int time, boolean ignoreArmor) {
        public float currentRadiation() {
            return maxRad * ((float) time / (float) Math.max(1, maxTime));
        }
    }

    private RadiationData() {
    }
}
