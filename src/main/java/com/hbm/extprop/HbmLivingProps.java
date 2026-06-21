package com.hbm.extprop;

import com.hbm.ntm.player.HbmLivingProperties;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;

import java.util.AbstractList;
import java.util.List;
import java.util.UUID;

/**
 * Legacy package facade for source migrations. Real state is owned by
 * {@link HbmLivingProperties}; this class must not grow a parallel store.
 */
public final class HbmLivingProps {
    public static final String key = HbmLivingProperties.key;
    public static final UUID digamma_UUID = HbmLivingProperties.digamma_UUID;
    public static final int MKU_CONTAGION_TICKS = HbmLivingProperties.MKU_CONTAGION_TICKS;
    public static final int maxAsbestos = HbmLivingProperties.maxAsbestos;
    public static final int maxBlacklung = HbmLivingProperties.maxBlacklung;

    public final LivingEntity entity;

    public HbmLivingProps(LivingEntity entity) {
        this.entity = entity;
    }

    public static HbmLivingProps registerData(LivingEntity entity) {
        return getData(entity);
    }

    public static HbmLivingProps getData(LivingEntity entity) {
        return new HbmLivingProps(entity);
    }

    public LivingEntity entity() {
        return entity;
    }

    public static float getRadiation(LivingEntity entity) {
        return HbmLivingProperties.getRadiation(entity);
    }

    public static void setRadiation(LivingEntity entity, float radiation) {
        HbmLivingProperties.setRadiation(entity, radiation);
    }

    public static void incrementRadiation(LivingEntity entity, float radiation) {
        HbmLivingProperties.incrementRadiation(entity, radiation);
    }

    public static float addRadiation(LivingEntity entity, float radiation) {
        return HbmLivingProperties.addRadiation(entity, radiation);
    }

    public static float reduceRadiation(LivingEntity entity, float radiation) {
        return HbmLivingProperties.reduceRadiation(entity, radiation);
    }

    public static void clearRadiation(LivingEntity entity) {
        HbmLivingProperties.clearRadiation(entity);
    }

    public static float getRadEnv(LivingEntity entity) {
        return HbmLivingProperties.getRadEnv(entity);
    }

    public static void setRadEnv(LivingEntity entity, float radiation) {
        HbmLivingProperties.setRadEnv(entity, radiation);
    }

    public static float getRadBuf(LivingEntity entity) {
        return HbmLivingProperties.getRadBuf(entity);
    }

    public static void setRadBuf(LivingEntity entity, float radiation) {
        HbmLivingProperties.setRadBuf(entity, radiation);
    }

    public static void flushEnvironmentBuffer(LivingEntity entity) {
        HbmLivingProperties.flushEnvironmentBuffer(entity);
    }

    public static List<ContaminationEffect> getCont(LivingEntity entity) {
        return new ContaminationList(entity);
    }

    public static int getContaminationCount(LivingEntity entity) {
        return HbmLivingProperties.getContaminationCount(entity);
    }

    public static void addCont(LivingEntity entity, ContaminationEffect contamination) {
        if (contamination != null) {
            HbmLivingProperties.addCont(entity, contamination.toModern());
        }
    }

    public static void addCont(LivingEntity entity, float radiation, int time, boolean ignoreArmor) {
        HbmLivingProperties.addCont(entity, radiation, time, ignoreArmor);
    }

    public static int clearCont(LivingEntity entity) {
        return HbmLivingProperties.clearCont(entity);
    }

    public static boolean removeCont(LivingEntity entity, int index) {
        return HbmLivingProperties.removeCont(entity, index);
    }

    public static float getDigamma(LivingEntity entity) {
        return HbmLivingProperties.getDigamma(entity);
    }

    public static void setDigamma(LivingEntity entity, float digamma) {
        HbmLivingProperties.setDigamma(entity, digamma);
    }

    public static void incrementDigamma(LivingEntity entity, float digamma) {
        HbmLivingProperties.incrementDigamma(entity, digamma);
    }

    public static float addDigamma(LivingEntity entity, float digamma) {
        return HbmLivingProperties.addDigamma(entity, digamma);
    }

    public static float reduceDigamma(LivingEntity entity, float digamma) {
        return HbmLivingProperties.reduceDigamma(entity, digamma);
    }

    public static float capDigamma(LivingEntity entity, float maximum) {
        return HbmLivingProperties.capDigamma(entity, maximum);
    }

    public static void clearDigamma(LivingEntity entity) {
        HbmLivingProperties.clearDigamma(entity);
    }

    public static void applyDigammaModifier(LivingEntity entity) {
        HbmLivingProperties.applyDigammaModifier(entity);
    }

    public static int getAsbestos(LivingEntity entity) {
        return HbmLivingProperties.getAsbestos(entity);
    }

    public static void setAsbestos(LivingEntity entity, int asbestos) {
        HbmLivingProperties.setAsbestos(entity, asbestos);
    }

    public static void incrementAsbestos(LivingEntity entity, int asbestos) {
        HbmLivingProperties.incrementAsbestos(entity, asbestos);
    }

    public static void ensureAsbestos(LivingEntity entity, int minimum) {
        HbmLivingProperties.ensureAsbestos(entity, minimum);
    }

    public static int reduceAsbestos(LivingEntity entity, int amount) {
        return HbmLivingProperties.reduceAsbestos(entity, amount);
    }

    public static int capAsbestos(LivingEntity entity, int maximum) {
        return HbmLivingProperties.capAsbestos(entity, maximum);
    }

    public static void clearAsbestos(LivingEntity entity) {
        HbmLivingProperties.clearAsbestos(entity);
    }

    public static int getBlackLung(LivingEntity entity) {
        return HbmLivingProperties.getBlackLung(entity);
    }

    public static void setBlackLung(LivingEntity entity, int blacklung) {
        HbmLivingProperties.setBlackLung(entity, blacklung);
    }

    public static void incrementBlackLung(LivingEntity entity, int blacklung) {
        HbmLivingProperties.incrementBlackLung(entity, blacklung);
    }

    public static void ensureBlackLung(LivingEntity entity, int minimum) {
        HbmLivingProperties.ensureBlackLung(entity, minimum);
    }

    public static int reduceBlackLung(LivingEntity entity, int amount) {
        return HbmLivingProperties.reduceBlackLung(entity, amount);
    }

    public static int capBlackLung(LivingEntity entity, int maximum) {
        return HbmLivingProperties.capBlackLung(entity, maximum);
    }

    public static void clearBlackLung(LivingEntity entity) {
        HbmLivingProperties.clearBlackLung(entity);
    }

    public static int getTimer(LivingEntity entity) {
        return HbmLivingProperties.getTimer(entity);
    }

    public static void setTimer(LivingEntity entity, int bombTimer) {
        HbmLivingProperties.setTimer(entity, bombTimer);
    }

    public static int getBombTimer(LivingEntity entity) {
        return HbmLivingProperties.getBombTimer(entity);
    }

    public static void setBombTimer(LivingEntity entity, int bombTimer) {
        HbmLivingProperties.setBombTimer(entity, bombTimer);
    }

    public static int decrementBombTimer(LivingEntity entity) {
        return HbmLivingProperties.decrementBombTimer(entity);
    }

    public static void clearBombTimer(LivingEntity entity) {
        HbmLivingProperties.clearBombTimer(entity);
    }

    public static int addTimer(LivingEntity entity, int amount) {
        return HbmLivingProperties.addTimer(entity, amount);
    }

    public static int decrementTimer(LivingEntity entity) {
        return HbmLivingProperties.decrementTimer(entity);
    }

    public static void ensureTimer(LivingEntity entity, int minimum) {
        HbmLivingProperties.ensureTimer(entity, minimum);
    }

    public static void clearTimer(LivingEntity entity) {
        HbmLivingProperties.clearTimer(entity);
    }

    public static int getContagion(LivingEntity entity) {
        return HbmLivingProperties.getContagion(entity);
    }

    public static void setContagion(LivingEntity entity, int contagion) {
        HbmLivingProperties.setContagion(entity, contagion);
    }

    public static int addContagion(LivingEntity entity, int amount) {
        return HbmLivingProperties.addContagion(entity, amount);
    }

    public static void ensureContagion(LivingEntity entity, int minimum) {
        HbmLivingProperties.ensureContagion(entity, minimum);
    }

    public static void applyMkuContagion(LivingEntity entity) {
        HbmLivingProperties.applyMkuContagion(entity);
    }

    public static int decrementContagion(LivingEntity entity) {
        return HbmLivingProperties.decrementContagion(entity);
    }

    public static int reduceContagion(LivingEntity entity, int amount) {
        return HbmLivingProperties.reduceContagion(entity, amount);
    }

    public static void clearContagion(LivingEntity entity) {
        HbmLivingProperties.clearContagion(entity);
    }

    public static int getOil(LivingEntity entity) {
        return HbmLivingProperties.getOil(entity);
    }

    public static void setOil(LivingEntity entity, int oil) {
        HbmLivingProperties.setOil(entity, oil);
    }

    public static void addOil(LivingEntity entity, int amount) {
        HbmLivingProperties.addOil(entity, amount);
    }

    public static void ensureOil(LivingEntity entity, int minimum) {
        HbmLivingProperties.ensureOil(entity, minimum);
    }

    public static int decrementOil(LivingEntity entity) {
        return HbmLivingProperties.decrementOil(entity);
    }

    public static int reduceOil(LivingEntity entity, int amount) {
        return HbmLivingProperties.reduceOil(entity, amount);
    }

    public static void clearOil(LivingEntity entity) {
        HbmLivingProperties.clearOil(entity);
    }

    public static int getFire(LivingEntity entity) {
        return HbmLivingProperties.getFire(entity);
    }

    public static void setFire(LivingEntity entity, int fire) {
        HbmLivingProperties.setFire(entity, fire);
    }

    public static void addFire(LivingEntity entity, int amount) {
        HbmLivingProperties.addFire(entity, amount);
    }

    public static void ensureFire(LivingEntity entity, int minimum) {
        HbmLivingProperties.ensureFire(entity, minimum);
    }

    public static int decrementFire(LivingEntity entity) {
        return HbmLivingProperties.decrementFire(entity);
    }

    public static int reduceFire(LivingEntity entity, int amount) {
        return HbmLivingProperties.reduceFire(entity, amount);
    }

    public static void clearFire(LivingEntity entity) {
        HbmLivingProperties.clearFire(entity);
    }

    public static int getPhosphorus(LivingEntity entity) {
        return HbmLivingProperties.getPhosphorus(entity);
    }

    public static void setPhosphorus(LivingEntity entity, int phosphorus) {
        HbmLivingProperties.setPhosphorus(entity, phosphorus);
    }

    public static void addPhosphorus(LivingEntity entity, int amount) {
        HbmLivingProperties.addPhosphorus(entity, amount);
    }

    public static void ensurePhosphorus(LivingEntity entity, int minimum) {
        HbmLivingProperties.ensurePhosphorus(entity, minimum);
    }

    public static int decrementPhosphorus(LivingEntity entity) {
        return HbmLivingProperties.decrementPhosphorus(entity);
    }

    public static int reducePhosphorus(LivingEntity entity, int amount) {
        return HbmLivingProperties.reducePhosphorus(entity, amount);
    }

    public static void clearPhosphorus(LivingEntity entity) {
        HbmLivingProperties.clearPhosphorus(entity);
    }

    public static int getBalefire(LivingEntity entity) {
        return HbmLivingProperties.getBalefire(entity);
    }

    public static void setBalefire(LivingEntity entity, int balefire) {
        HbmLivingProperties.setBalefire(entity, balefire);
    }

    public static void addBalefire(LivingEntity entity, int amount) {
        HbmLivingProperties.addBalefire(entity, amount);
    }

    public static void ensureBalefire(LivingEntity entity, int minimum) {
        HbmLivingProperties.ensureBalefire(entity, minimum);
    }

    public static int decrementBalefire(LivingEntity entity) {
        return HbmLivingProperties.decrementBalefire(entity);
    }

    public static int reduceBalefire(LivingEntity entity, int amount) {
        return HbmLivingProperties.reduceBalefire(entity, amount);
    }

    public static void clearBalefire(LivingEntity entity) {
        HbmLivingProperties.clearBalefire(entity);
    }

    public static int getBlackFire(LivingEntity entity) {
        return HbmLivingProperties.getBlackFire(entity);
    }

    public static void setBlackFire(LivingEntity entity, int blackFire) {
        HbmLivingProperties.setBlackFire(entity, blackFire);
    }

    public static void addBlackFire(LivingEntity entity, int amount) {
        HbmLivingProperties.addBlackFire(entity, amount);
    }

    public static void ensureBlackFire(LivingEntity entity, int minimum) {
        HbmLivingProperties.ensureBlackFire(entity, minimum);
    }

    public static int decrementBlackFire(LivingEntity entity) {
        return HbmLivingProperties.decrementBlackFire(entity);
    }

    public static int reduceBlackFire(LivingEntity entity, int amount) {
        return HbmLivingProperties.reduceBlackFire(entity, amount);
    }

    public static void clearBlackFire(LivingEntity entity) {
        HbmLivingProperties.clearBlackFire(entity);
    }

    public static int ensureBlackFireOrAdd(LivingEntity entity, int minimum, int amount) {
        return HbmLivingProperties.ensureBlackFireOrAdd(entity, minimum, amount);
    }

    public static boolean hasTemperatureEffects(LivingEntity entity) {
        return HbmLivingProperties.hasTemperatureEffects(entity);
    }

    public static void clearTemperatureEffects(LivingEntity entity) {
        HbmLivingProperties.clearTemperatureEffects(entity);
    }

    public static List<ContaminationEffect> tickContamination(LivingEntity entity) {
        List<ContaminationEffect> effects = new java.util.ArrayList<>();
        for (HbmLivingProperties.ContaminationEffect effect : HbmLivingProperties.tickContamination(entity)) {
            effects.add(ContaminationEffect.fromModern(effect));
        }
        return effects;
    }

    public static List<ContaminationEffect> getContaminationEffectsForSync(LivingEntity entity) {
        List<ContaminationEffect> effects = new java.util.ArrayList<>();
        for (HbmLivingProperties.ContaminationEffect effect : HbmLivingProperties.getContaminationEffectsForSync(entity)) {
            effects.add(ContaminationEffect.fromModern(effect));
        }
        return effects;
    }

    public static void copyForRespawn(LivingEntity original, LivingEntity replacement) {
        HbmLivingProperties.copyForRespawn(original, replacement);
    }

    public static CompoundTag writePersistentData(LivingEntity entity) {
        return HbmLivingProperties.writePersistentData(entity);
    }

    public static void readPersistentData(LivingEntity entity, CompoundTag data) {
        HbmLivingProperties.readPersistentData(entity, data);
    }

    public static void serialize(LivingEntity entity, FriendlyByteBuf buffer) {
        HbmLivingProperties.serializeLegacySyncedData(entity, buffer);
    }

    public static void serialize(LivingEntity entity, ByteBuf buffer) {
        serialize(entity, new FriendlyByteBuf(buffer));
    }

    public static void deserialize(LivingEntity entity, FriendlyByteBuf buffer) {
        HbmLivingProperties.deserializeLegacySyncedData(entity, buffer);
    }

    public static void deserialize(LivingEntity entity, ByteBuf buffer) {
        deserialize(entity, new FriendlyByteBuf(buffer));
    }

    public static void saveNBTData(LivingEntity entity, CompoundTag nbt) {
        HbmLivingProperties.saveNBTData(entity, nbt);
    }

    public static void loadNBTData(LivingEntity entity, CompoundTag nbt) {
        HbmLivingProperties.loadNBTData(entity, nbt);
    }

    public float getRadiation() {
        return getRadiation(entity);
    }

    public void setRadiation(float radiation) {
        setRadiation(entity, radiation);
    }

    public void incrementRadiation(float radiation) {
        incrementRadiation(entity, radiation);
    }

    public float addRadiation(float radiation) {
        return addRadiation(entity, radiation);
    }

    public float reduceRadiation(float radiation) {
        return reduceRadiation(entity, radiation);
    }

    public void clearRadiation() {
        clearRadiation(entity);
    }

    public float getDigamma() {
        return getDigamma(entity);
    }

    public void setDigamma(float digamma) {
        setDigamma(entity, digamma);
    }

    public void incrementDigamma(float digamma) {
        incrementDigamma(entity, digamma);
    }

    public float addDigamma(float digamma) {
        return addDigamma(entity, digamma);
    }

    public float reduceDigamma(float digamma) {
        return reduceDigamma(entity, digamma);
    }

    public float capDigamma(float maximum) {
        return capDigamma(entity, maximum);
    }

    public void clearDigamma() {
        clearDigamma(entity);
    }

    public void applyDigammaModifier() {
        applyDigammaModifier(entity);
    }

    public float getRadEnv() {
        return getRadEnv(entity);
    }

    public void setRadEnv(float radiation) {
        setRadEnv(entity, radiation);
    }

    public float getRadBuf() {
        return getRadBuf(entity);
    }

    public void setRadBuf(float radiation) {
        setRadBuf(entity, radiation);
    }

    public void flushEnvironmentBuffer() {
        flushEnvironmentBuffer(entity);
    }

    public List<ContaminationEffect> getCont() {
        return getCont(entity);
    }

    public int getContaminationCount() {
        return getContaminationCount(entity);
    }

    public void addCont(ContaminationEffect contamination) {
        addCont(entity, contamination);
    }

    public void addCont(float radiation, int time, boolean ignoreArmor) {
        addCont(entity, radiation, time, ignoreArmor);
    }

    public int clearCont() {
        return clearCont(entity);
    }

    public boolean removeCont(int index) {
        return removeCont(entity, index);
    }

    public int getAsbestos() {
        return getAsbestos(entity);
    }

    public void setAsbestos(int asbestos) {
        setAsbestos(entity, asbestos);
    }

    public void incrementAsbestos(int asbestos) {
        incrementAsbestos(entity, asbestos);
    }

    public void ensureAsbestos(int minimum) {
        ensureAsbestos(entity, minimum);
    }

    public int reduceAsbestos(int amount) {
        return reduceAsbestos(entity, amount);
    }

    public int capAsbestos(int maximum) {
        return capAsbestos(entity, maximum);
    }

    public void clearAsbestos() {
        clearAsbestos(entity);
    }

    public int getBlackLung() {
        return getBlackLung(entity);
    }

    public void setBlackLung(int blackLung) {
        setBlackLung(entity, blackLung);
    }

    public void incrementBlackLung(int blackLung) {
        incrementBlackLung(entity, blackLung);
    }

    public void ensureBlackLung(int minimum) {
        ensureBlackLung(entity, minimum);
    }

    public int reduceBlackLung(int amount) {
        return reduceBlackLung(entity, amount);
    }

    public int capBlackLung(int maximum) {
        return capBlackLung(entity, maximum);
    }

    public void clearBlackLung() {
        clearBlackLung(entity);
    }

    public int getTimer() {
        return getTimer(entity);
    }

    public void setTimer(int timer) {
        setTimer(entity, timer);
    }

    public int getBombTimer() {
        return getBombTimer(entity);
    }

    public void setBombTimer(int bombTimer) {
        setBombTimer(entity, bombTimer);
    }

    public int decrementBombTimer() {
        return decrementBombTimer(entity);
    }

    public void clearBombTimer() {
        clearBombTimer(entity);
    }

    public int addTimer(int amount) {
        return addTimer(entity, amount);
    }

    public int decrementTimer() {
        return decrementTimer(entity);
    }

    public void ensureTimer(int minimum) {
        ensureTimer(entity, minimum);
    }

    public void clearTimer() {
        clearTimer(entity);
    }

    public int getContagion() {
        return getContagion(entity);
    }

    public void setContagion(int contagion) {
        setContagion(entity, contagion);
    }

    public int addContagion(int amount) {
        return addContagion(entity, amount);
    }

    public void ensureContagion(int minimum) {
        ensureContagion(entity, minimum);
    }

    public void applyMkuContagion() {
        applyMkuContagion(entity);
    }

    public int decrementContagion() {
        return decrementContagion(entity);
    }

    public int reduceContagion(int amount) {
        return reduceContagion(entity, amount);
    }

    public void clearContagion() {
        clearContagion(entity);
    }

    public int getOil() {
        return getOil(entity);
    }

    public void setOil(int oil) {
        setOil(entity, oil);
    }

    public void addOil(int amount) {
        addOil(entity, amount);
    }

    public void ensureOil(int minimum) {
        ensureOil(entity, minimum);
    }

    public int decrementOil() {
        return decrementOil(entity);
    }

    public int reduceOil(int amount) {
        return reduceOil(entity, amount);
    }

    public void clearOil() {
        clearOil(entity);
    }

    public int getFire() {
        return getFire(entity);
    }

    public void setFire(int fire) {
        setFire(entity, fire);
    }

    public void addFire(int amount) {
        addFire(entity, amount);
    }

    public void ensureFire(int minimum) {
        ensureFire(entity, minimum);
    }

    public int decrementFire() {
        return decrementFire(entity);
    }

    public int reduceFire(int amount) {
        return reduceFire(entity, amount);
    }

    public void clearFire() {
        clearFire(entity);
    }

    public int getPhosphorus() {
        return getPhosphorus(entity);
    }

    public void setPhosphorus(int phosphorus) {
        setPhosphorus(entity, phosphorus);
    }

    public void addPhosphorus(int amount) {
        addPhosphorus(entity, amount);
    }

    public void ensurePhosphorus(int minimum) {
        ensurePhosphorus(entity, minimum);
    }

    public int decrementPhosphorus() {
        return decrementPhosphorus(entity);
    }

    public int reducePhosphorus(int amount) {
        return reducePhosphorus(entity, amount);
    }

    public void clearPhosphorus() {
        clearPhosphorus(entity);
    }

    public int getBalefire() {
        return getBalefire(entity);
    }

    public void setBalefire(int balefire) {
        setBalefire(entity, balefire);
    }

    public void addBalefire(int amount) {
        addBalefire(entity, amount);
    }

    public void ensureBalefire(int minimum) {
        ensureBalefire(entity, minimum);
    }

    public int decrementBalefire() {
        return decrementBalefire(entity);
    }

    public int reduceBalefire(int amount) {
        return reduceBalefire(entity, amount);
    }

    public void clearBalefire() {
        clearBalefire(entity);
    }

    public int getBlackFire() {
        return getBlackFire(entity);
    }

    public void setBlackFire(int blackFire) {
        setBlackFire(entity, blackFire);
    }

    public void addBlackFire(int amount) {
        addBlackFire(entity, amount);
    }

    public void ensureBlackFire(int minimum) {
        ensureBlackFire(entity, minimum);
    }

    public int decrementBlackFire() {
        return decrementBlackFire(entity);
    }

    public int reduceBlackFire(int amount) {
        return reduceBlackFire(entity, amount);
    }

    public void clearBlackFire() {
        clearBlackFire(entity);
    }

    public int ensureBlackFireOrAdd(int minimum, int amount) {
        return ensureBlackFireOrAdd(entity, minimum, amount);
    }

    public boolean hasTemperatureEffects() {
        return hasTemperatureEffects(entity);
    }

    public void clearTemperatureEffects() {
        clearTemperatureEffects(entity);
    }

    public List<ContaminationEffect> tickContamination() {
        return tickContamination(entity);
    }

    public List<ContaminationEffect> getContaminationEffectsForSync() {
        return getContaminationEffectsForSync(entity);
    }

    public CompoundTag writePersistentData() {
        return HbmLivingProperties.writePersistentData(entity);
    }

    public void readPersistentData(CompoundTag data) {
        HbmLivingProperties.readPersistentData(entity, data);
    }

    public void serialize(FriendlyByteBuf buffer) {
        HbmLivingProperties.serializeLegacySyncedData(entity, buffer);
    }

    public void serialize(ByteBuf buffer) {
        serialize(new FriendlyByteBuf(buffer));
    }

    public void deserialize(FriendlyByteBuf buffer) {
        HbmLivingProperties.deserializeLegacySyncedData(entity, buffer);
    }

    public void deserialize(ByteBuf buffer) {
        deserialize(new FriendlyByteBuf(buffer));
    }

    public void saveNBTData(CompoundTag nbt) {
        HbmLivingProperties.saveNBTData(entity, nbt);
    }

    public void loadNBTData(CompoundTag nbt) {
        HbmLivingProperties.loadNBTData(entity, nbt);
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
            this.time = time;
            this.ignoreArmor = ignoreArmor;
        }

        public float getRad() {
            return maxRad * ((float) time / (float) Math.max(1, maxTime));
        }

        public void serialize(FriendlyByteBuf buffer) {
            HbmLivingProperties.encodeLegacyContaminationEffect(toModern(), buffer);
        }

        public void serialize(ByteBuf buffer) {
            serialize(new FriendlyByteBuf(buffer));
        }

        public static ContaminationEffect deserialize(FriendlyByteBuf buffer) {
            return fromModern(HbmLivingProperties.decodeLegacyContaminationEffect(buffer));
        }

        public static ContaminationEffect deserialize(ByteBuf buffer) {
            return deserialize(new FriendlyByteBuf(buffer));
        }

        public void save(CompoundTag nbt, int index) {
            if (nbt != null) {
                nbt.put("cont_" + index, toTag());
            }
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("maxRad", maxRad);
            tag.putInt("maxTime", Math.max(1, maxTime));
            tag.putInt("time", time);
            tag.putBoolean("ignoreArmor", ignoreArmor);
            return tag;
        }

        public static ContaminationEffect load(CompoundTag nbt, int index) {
            if (nbt == null) {
                return new ContaminationEffect(0.0F, 1, 0, false);
            }
            String key = "cont_" + index;
            if (nbt.contains(key, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                return fromTag(nbt.getCompound(key));
            }
            return fromTag(nbt);
        }

        public static ContaminationEffect fromTag(CompoundTag tag) {
            CompoundTag safeTag = tag == null ? new CompoundTag() : tag;
            return new ContaminationEffect(
                    safeTag.getFloat("maxRad"),
                    Math.max(1, safeTag.getInt("maxTime")),
                    safeTag.getInt("time"),
                    safeTag.getBoolean("ignoreArmor"));
        }

        private HbmLivingProperties.ContaminationEffect toModern() {
            return new HbmLivingProperties.ContaminationEffect(maxRad, maxTime, time, ignoreArmor);
        }

        private static ContaminationEffect fromModern(HbmLivingProperties.ContaminationEffect effect) {
            return new ContaminationEffect(effect.maxRad, effect.maxTime, effect.time, effect.ignoreArmor);
        }
    }

    private static final class ContaminationList extends AbstractList<ContaminationEffect> {
        private final LivingEntity entity;

        private ContaminationList(LivingEntity entity) {
            this.entity = entity;
        }

        @Override
        public ContaminationEffect get(int index) {
            return ContaminationEffect.fromModern(HbmLivingProperties.getCont(entity).get(index));
        }

        @Override
        public int size() {
            return HbmLivingProperties.getContaminationCount(entity);
        }

        @Override
        public void add(int index, ContaminationEffect element) {
            List<HbmLivingProperties.ContaminationEffect> effects = HbmLivingProperties.getCont(entity);
            effects.add(index, element == null ? new HbmLivingProperties.ContaminationEffect(0.0F, 1, 0, false) : element.toModern());
        }

        @Override
        public ContaminationEffect set(int index, ContaminationEffect element) {
            List<HbmLivingProperties.ContaminationEffect> effects = HbmLivingProperties.getCont(entity);
            HbmLivingProperties.ContaminationEffect previous = effects.set(index,
                    element == null ? new HbmLivingProperties.ContaminationEffect(0.0F, 1, 0, false) : element.toModern());
            return ContaminationEffect.fromModern(previous);
        }

        @Override
        public ContaminationEffect remove(int index) {
            List<HbmLivingProperties.ContaminationEffect> effects = HbmLivingProperties.getCont(entity);
            return ContaminationEffect.fromModern(effects.remove(index));
        }

        @Override
        public void clear() {
            HbmLivingProperties.clearCont(entity);
        }
    }
}
