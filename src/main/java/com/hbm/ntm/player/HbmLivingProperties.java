package com.hbm.ntm.player;

import com.hbm.ntm.config.ServerConfig;
import com.hbm.ntm.radiation.RadiationData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;

import java.util.AbstractList;
import java.util.List;

public final class HbmLivingProperties {
    public static final String TAG_ROOT = "HbmLivingProps";
    public static final int MAX_ASBESTOS = RadiationData.MAX_ASBESTOS;
    public static final int MAX_BLACK_LUNG = RadiationData.MAX_BLACK_LUNG;
    @Deprecated
    public static final int maxAsbestos = MAX_ASBESTOS;
    @Deprecated
    public static final int maxBlacklung = MAX_BLACK_LUNG;

    public static float getRadiation(LivingEntity entity) {
        return RadiationData.getRadiation(entity);
    }

    public static void setRadiation(LivingEntity entity, float radiation) {
        RadiationData.setRadiation(entity, radiation);
    }

    public static void incrementRadiation(LivingEntity entity, float amount) {
        RadiationData.incrementRadiation(entity, amount);
    }

    public static float getDigamma(LivingEntity entity) {
        return RadiationData.getDigamma(entity);
    }

    public static void setDigamma(LivingEntity entity, float digamma) {
        RadiationData.setDigamma(entity, digamma);
    }

    public static void incrementDigamma(LivingEntity entity, float amount) {
        RadiationData.incrementDigamma(entity, amount);
    }

    public static float getRadEnv(LivingEntity entity) {
        return RadiationData.getRadEnv(entity);
    }

    public static void setRadEnv(LivingEntity entity, float radiation) {
        RadiationData.setRadEnv(entity, radiation);
    }

    public static float getRadBuf(LivingEntity entity) {
        return RadiationData.getRadBuf(entity);
    }

    public static void setRadBuf(LivingEntity entity, float radiation) {
        RadiationData.setRadBuf(entity, radiation);
    }

    public static List<ContaminationEffect> getCont(LivingEntity entity) {
        return new ContaminationList(entity);
    }

    public static void addCont(LivingEntity entity, ContaminationEffect effect) {
        if (effect != null) {
            RadiationData.addContamination(entity, effect.maxRad, effect.maxTime, effect.time, effect.ignoreArmor);
        }
    }

    public static void addCont(LivingEntity entity, float radiation, int time, boolean ignoreArmor) {
        addCont(entity, new ContaminationEffect(radiation, time, ignoreArmor));
    }

    public static int clearCont(LivingEntity entity) {
        return RadiationData.clearContamination(entity);
    }

    public static int getAsbestos(LivingEntity entity) {
        return RadiationData.getAsbestos(entity);
    }

    public static void setAsbestos(LivingEntity entity, int asbestos) {
        RadiationData.setAsbestos(entity, asbestos);
    }

    public static void incrementAsbestos(LivingEntity entity, int amount) {
        RadiationData.incrementAsbestos(entity, amount);
    }

    public static int getBlackLung(LivingEntity entity) {
        return RadiationData.getBlackLung(entity);
    }

    public static void setBlackLung(LivingEntity entity, int blackLung) {
        RadiationData.setBlackLung(entity, blackLung);
    }

    public static void incrementBlackLung(LivingEntity entity, int amount) {
        RadiationData.incrementBlackLung(entity, amount);
    }

    public static int getTimer(LivingEntity entity) {
        return RadiationData.getBombTimer(entity);
    }

    public static void setTimer(LivingEntity entity, int bombTimer) {
        RadiationData.setBombTimer(entity, bombTimer);
    }

    public static int getBombTimer(LivingEntity entity) {
        return RadiationData.getBombTimer(entity);
    }

    public static void setBombTimer(LivingEntity entity, int bombTimer) {
        RadiationData.setBombTimer(entity, bombTimer);
    }

    public static int getContagion(LivingEntity entity) {
        if (!ServerConfig.ENABLE_MKU.get()) {
            return 0;
        }
        return RadiationData.getContagion(entity);
    }

    public static void setContagion(LivingEntity entity, int contagion) {
        RadiationData.setContagion(entity, contagion);
    }

    public static int getOil(LivingEntity entity) {
        return RadiationData.getOil(entity);
    }

    public static void setOil(LivingEntity entity, int oil) {
        RadiationData.setOil(entity, oil);
    }

    public static void addOil(LivingEntity entity, int amount) {
        setOil(entity, getOil(entity) + amount);
    }

    public static void ensureOil(LivingEntity entity, int minimum) {
        if (getOil(entity) < minimum) {
            setOil(entity, minimum);
        }
    }

    public static int getFire(LivingEntity entity) {
        return RadiationData.getFire(entity);
    }

    public static void setFire(LivingEntity entity, int fire) {
        RadiationData.setFire(entity, fire);
    }

    public static void addFire(LivingEntity entity, int amount) {
        setFire(entity, getFire(entity) + amount);
    }

    public static void ensureFire(LivingEntity entity, int minimum) {
        if (getFire(entity) < minimum) {
            setFire(entity, minimum);
        }
    }

    public static int getPhosphorus(LivingEntity entity) {
        return RadiationData.getPhosphorus(entity);
    }

    public static void setPhosphorus(LivingEntity entity, int phosphorus) {
        RadiationData.setPhosphorus(entity, phosphorus);
    }

    public static void addPhosphorus(LivingEntity entity, int amount) {
        setPhosphorus(entity, getPhosphorus(entity) + amount);
    }

    public static void ensurePhosphorus(LivingEntity entity, int minimum) {
        if (getPhosphorus(entity) < minimum) {
            setPhosphorus(entity, minimum);
        }
    }

    public static int getBalefire(LivingEntity entity) {
        return RadiationData.getBalefire(entity);
    }

    public static void setBalefire(LivingEntity entity, int balefire) {
        RadiationData.setBalefire(entity, balefire);
    }

    public static void addBalefire(LivingEntity entity, int amount) {
        setBalefire(entity, getBalefire(entity) + amount);
    }

    public static void ensureBalefire(LivingEntity entity, int minimum) {
        if (getBalefire(entity) < minimum) {
            setBalefire(entity, minimum);
        }
    }

    public static int getBlackFire(LivingEntity entity) {
        return RadiationData.getBlackFire(entity);
    }

    public static void setBlackFire(LivingEntity entity, int blackFire) {
        RadiationData.setBlackFire(entity, blackFire);
    }

    public static void addBlackFire(LivingEntity entity, int amount) {
        setBlackFire(entity, getBlackFire(entity) + amount);
    }

    public static void ensureBlackFire(LivingEntity entity, int minimum) {
        if (getBlackFire(entity) < minimum) {
            setBlackFire(entity, minimum);
        }
    }

    public static void copyForRespawn(LivingEntity original, LivingEntity replacement) {
        RadiationData.copyForRespawn(original, replacement);
    }

    private static ListTag toListTag(List<ContaminationEffect> effects) {
        ListTag tag = new ListTag();
        for (ContaminationEffect effect : effects) {
            tag.add(effect.toTag());
        }
        return tag;
    }

    private static ContaminationEffect fromData(RadiationData.ContaminationEffect effect) {
        return new ContaminationEffect(effect.maxRad(), effect.maxTime(), effect.time(), effect.ignoreArmor());
    }

    public static final class ContaminationEffect {
        public float maxRad;
        public int maxTime;
        public int time;
        public boolean ignoreArmor;

        public ContaminationEffect(float radiation, int time, boolean ignoreArmor) {
            this(radiation, time, time, ignoreArmor);
        }

        public ContaminationEffect(float maxRad, int maxTime, int time, boolean ignoreArmor) {
            this.maxRad = maxRad;
            this.maxTime = Math.max(1, maxTime);
            this.time = Math.max(0, time);
            this.ignoreArmor = ignoreArmor;
        }

        public float getRad() {
            return maxRad * ((float) time / (float) Math.max(1, maxTime));
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("maxRad", maxRad);
            tag.putInt("maxTime", Math.max(1, maxTime));
            tag.putInt("time", Math.max(0, time));
            tag.putBoolean("ignoreArmor", ignoreArmor);
            return tag;
        }
    }

    private static final class ContaminationList extends AbstractList<ContaminationEffect> {
        private final LivingEntity entity;

        private ContaminationList(LivingEntity entity) {
            this.entity = entity;
        }

        @Override
        public ContaminationEffect get(int index) {
            return fromData(RadiationData.getContaminationEffects(entity).get(index));
        }

        @Override
        public int size() {
            return RadiationData.getContaminationCount(entity);
        }

        @Override
        public void add(int index, ContaminationEffect element) {
            List<ContaminationEffect> effects = snapshot();
            effects.add(index, element);
            RadiationData.setContamination(entity, toListTag(effects));
        }

        @Override
        public ContaminationEffect set(int index, ContaminationEffect element) {
            List<ContaminationEffect> effects = snapshot();
            ContaminationEffect previous = effects.set(index, element);
            RadiationData.setContamination(entity, toListTag(effects));
            return previous;
        }

        @Override
        public ContaminationEffect remove(int index) {
            List<ContaminationEffect> effects = snapshot();
            ContaminationEffect previous = effects.remove(index);
            RadiationData.setContamination(entity, toListTag(effects));
            return previous;
        }

        @Override
        public void clear() {
            RadiationData.clearContamination(entity);
        }

        private List<ContaminationEffect> snapshot() {
            return RadiationData.getContaminationEffects(entity).stream()
                    .map(HbmLivingProperties::fromData)
                    .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        }
    }

    private HbmLivingProperties() {
    }
}
