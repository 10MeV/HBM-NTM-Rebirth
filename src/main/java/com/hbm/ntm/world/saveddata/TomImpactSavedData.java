package com.hbm.ntm.world.saveddata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TomImpactSavedData extends SavedData {
    public static final String DATA_NAME = "impactData";
    public static final String KEY = DATA_NAME;
    public static final String key = DATA_NAME;
    public static final String TAG_PERMA_SYNC = "tomImpact";
    public static final String TAG_DUST = "dust";
    public static final String TAG_FIRE = "fire";
    public static final String TAG_IMPACT = "impact";
    public static final float DUST_SETTLE_PER_TICK = 1.0F / 14_400_000.0F;
    public static final float FIRE_COOL_PER_TICK = 1.0F / 24_000.0F;

    private static TomImpactSavedData lastCachedUnsafe;

    public float dust;
    public float fire;
    public boolean impact;
    private LoadDiagnostics loadDiagnostics = LoadDiagnostics.empty();

    public TomImpactSavedData() {
    }

    public TomImpactSavedData(String tagName) {
        this();
    }

    public static TomImpactSavedData load(CompoundTag tag) {
        TomImpactSavedData data = new TomImpactSavedData();
        data.dust = tag.getFloat(TAG_DUST);
        data.fire = tag.getFloat(TAG_FIRE);
        data.impact = tag.getBoolean(TAG_IMPACT);
        data.loadDiagnostics = LoadDiagnostics.inspect(tag, data.snapshot());
        return data;
    }

    public static TomImpactSavedData forLevel(ServerLevel level) {
        TomImpactSavedData data = WorldSavedDataHelper.get(level, DATA_NAME, TomImpactSavedData::load,
                TomImpactSavedData::new);
        lastCachedUnsafe = data;
        return data;
    }

    public static Optional<TomImpactSavedData> forLevel(Level level) {
        Optional<TomImpactSavedData> data = WorldSavedDataHelper.get(level, DATA_NAME, TomImpactSavedData::load,
                TomImpactSavedData::new);
        data.ifPresent(value -> lastCachedUnsafe = value);
        return data;
    }

    public static TomImpactSavedData forWorld(ServerLevel level) {
        return forLevel(level);
    }

    public static Optional<TomImpactSavedData> forWorld(Level level) {
        return forLevel(level);
    }

    public static TomImpactSavedData forWorld(MinecraftServer server) {
        return getData(server);
    }

    public static Optional<TomImpactSavedData> forWorld(MinecraftServer server, ResourceKey<Level> dimension) {
        return getData(server, dimension);
    }

    public static Optional<TomImpactSavedData> getExisting(ServerLevel level) {
        Optional<TomImpactSavedData> data = WorldSavedDataHelper.getExisting(level, DATA_NAME, TomImpactSavedData::load);
        data.ifPresent(value -> lastCachedUnsafe = value);
        return data;
    }

    public static Optional<TomImpactSavedData> getExisting(MinecraftServer server) {
        Optional<TomImpactSavedData> data = WorldSavedDataHelper.getExisting(server, DATA_NAME, TomImpactSavedData::load);
        data.ifPresent(value -> lastCachedUnsafe = value);
        return data;
    }

    public static Optional<TomImpactSavedData> getExisting(MinecraftServer server, ResourceKey<Level> dimension) {
        Optional<TomImpactSavedData> data = WorldSavedDataHelper.getExisting(server, dimension, DATA_NAME,
                TomImpactSavedData::load);
        data.ifPresent(value -> lastCachedUnsafe = value);
        return data;
    }

    public static Optional<TomImpactSavedData> getExisting(Level level) {
        Optional<TomImpactSavedData> data = WorldSavedDataHelper.getExisting(level, DATA_NAME, TomImpactSavedData::load);
        data.ifPresent(value -> lastCachedUnsafe = value);
        return data;
    }

    public static TomImpactSavedData getData(ServerLevel level) {
        return forLevel(level);
    }

    public static Optional<TomImpactSavedData> getData(Level level) {
        return forLevel(level);
    }

    public static TomImpactSavedData getData(MinecraftServer server) {
        TomImpactSavedData data = WorldSavedDataHelper.get(server, DATA_NAME, TomImpactSavedData::load,
                TomImpactSavedData::new);
        lastCachedUnsafe = data;
        return data;
    }

    public static Optional<TomImpactSavedData> getData(MinecraftServer server, ResourceKey<Level> dimension) {
        Optional<TomImpactSavedData> data = WorldSavedDataHelper.get(server, dimension, DATA_NAME,
                TomImpactSavedData::load, TomImpactSavedData::new);
        data.ifPresent(value -> lastCachedUnsafe = value);
        return data;
    }

    public static TomImpactSavedData getLastCachedOrNull() {
        return lastCachedUnsafe;
    }

    public static void resetLastCached() {
        lastCachedUnsafe = null;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putFloat(TAG_DUST, dust);
        tag.putFloat(TAG_FIRE, fire);
        tag.putBoolean(TAG_IMPACT, impact);
        return tag;
    }

    public void readFromNBT(CompoundTag tag) {
        TomImpactSavedData loaded = load(tag == null ? new CompoundTag() : tag);
        dust = loaded.dust;
        fire = loaded.fire;
        impact = loaded.impact;
        loadDiagnostics = loaded.loadDiagnostics;
        setDirty(false);
    }

    public void writeToNBT(CompoundTag tag) {
        save(tag);
    }

    public float dust() {
        return dust;
    }

    public void setDust(float dust) {
        if (Float.compare(this.dust, dust) != 0) {
            this.dust = dust;
            setDirty();
        }
    }

    public float fire() {
        return fire;
    }

    public void setFire(float fire) {
        if (Float.compare(this.fire, fire) != 0) {
            this.fire = fire;
            setDirty();
        }
    }

    public boolean impact() {
        return impact;
    }

    public void setImpact(boolean impact) {
        if (this.impact != impact) {
            this.impact = impact;
            setDirty();
        }
    }

    public boolean setImpactState(float dust, float fire, boolean impact) {
        boolean changed = Float.compare(this.dust, dust) != 0
                || Float.compare(this.fire, fire) != 0
                || this.impact != impact;
        this.dust = dust;
        this.fire = fire;
        this.impact = impact;
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean setClimate(float dust, float fire) {
        return setImpactState(dust, fire, impact);
    }

    public boolean beginTomImpactFire() {
        return setImpactState(dust, 1.0F, true);
    }

    public boolean clearImpactState() {
        return setImpactState(0.0F, 0.0F, false);
    }

    public boolean tickImpactClimate() {
        float oldDust = dust;
        float oldFire = fire;
        if (dust > 0.0F && fire == 0.0F) {
            dust = Math.max(0.0F, dust - DUST_SETTLE_PER_TICK);
        }
        if (fire > 0.0F) {
            fire = Math.max(0.0F, fire - FIRE_COOL_PER_TICK);
            dust = Math.min(1.0F, dust + FIRE_COOL_PER_TICK);
        }
        boolean changed = Float.compare(oldDust, dust) != 0 || Float.compare(oldFire, fire) != 0;
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public static ClimateTickResult tickExistingImpactClimate(ServerLevel level) {
        Optional<TomImpactSavedData> existing = getExisting(level);
        if (existing.isEmpty()) {
            return ClimateTickResult.noData();
        }
        TomImpactSavedData data = existing.get();
        boolean changed = data.tickImpactClimate();
        return new ClimateTickResult(true, changed, data.snapshot());
    }

    public Snapshot snapshot() {
        return new Snapshot(dust, fire, impact);
    }

    public LoadDiagnostics loadDiagnostics() {
        return loadDiagnostics;
    }

    public void applySnapshot(Snapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        boolean changed = Float.compare(dust, snapshot.dust) != 0
                || Float.compare(fire, snapshot.fire) != 0
                || impact != snapshot.impact;
        dust = snapshot.dust;
        fire = snapshot.fire;
        impact = snapshot.impact;
        if (changed) {
            setDirty();
        }
    }

    public CompoundTag writeSnapshotTag() {
        return writeSnapshotTag(snapshot());
    }

    public void appendPermaSyncData(CompoundTag data) {
        data.put(TAG_PERMA_SYNC, writeSnapshotTag());
    }

    public static void appendPermaSyncData(ServerLevel level, CompoundTag data) {
        forLevel(level).appendPermaSyncData(data);
    }

    public static CompoundTag writeSnapshotTag(Snapshot snapshot) {
        CompoundTag tag = new CompoundTag();
        if (snapshot != null) {
            tag.putFloat(TAG_DUST, snapshot.dust);
            tag.putFloat(TAG_FIRE, snapshot.fire);
            tag.putBoolean(TAG_IMPACT, snapshot.impact);
        }
        return tag;
    }

    public static Snapshot readSnapshotTag(CompoundTag tag) {
        CompoundTag source = tag == null ? new CompoundTag() : tag;
        return new Snapshot(source.getFloat(TAG_DUST), source.getFloat(TAG_FIRE), source.getBoolean(TAG_IMPACT));
    }

    public static Snapshot readPermaSyncData(CompoundTag data) {
        CompoundTag source = data == null ? new CompoundTag() : data.getCompound(TAG_PERMA_SYNC);
        return readSnapshotTag(source);
    }

    public void markDirty() {
        setDirty();
    }

    public record Snapshot(float dust, float fire, boolean impact) {
        public static final Snapshot EMPTY = new Snapshot(0.0F, 0.0F, false);

        public boolean hasClimate() {
            return dust > 0.0F || fire > 0.0F;
        }

        public boolean hasFire() {
            return fire > 0.0F;
        }

        public boolean hasDust() {
            return dust > 0.0F;
        }

        public String stage() {
            if (fire > 0.0F) {
                return impact ? "impact_fire" : "fire";
            }
            if (dust > 0.0F) {
                return impact ? "impact_dust" : "dust";
            }
            return impact ? "impact" : "clear";
        }

        public String summary() {
            return "dust=" + dust
                    + " fire=" + fire
                    + " impact=" + impact
                    + " climate=" + hasClimate()
                    + " stage=" + stage();
        }
    }

    public record ClimateTickResult(boolean hadData, boolean changed, Snapshot snapshot) {
        public static ClimateTickResult noData() {
            return new ClimateTickResult(false, false, Snapshot.EMPTY);
        }
    }

    public record LoadDiagnostics(boolean hasDustTag, boolean hasFireTag, boolean hasImpactTag,
                                  boolean finiteDust, boolean finiteFire) {
        public static LoadDiagnostics empty() {
            return new LoadDiagnostics(false, false, false, true, true);
        }

        public static LoadDiagnostics inspect(CompoundTag tag, Snapshot snapshot) {
            CompoundTag source = tag == null ? new CompoundTag() : tag;
            Snapshot value = snapshot == null ? Snapshot.EMPTY : snapshot;
            return new LoadDiagnostics(source.contains(TAG_DUST), source.contains(TAG_FIRE),
                    source.contains(TAG_IMPACT), Float.isFinite(value.dust()), Float.isFinite(value.fire()));
        }

        public boolean clean() {
            return hasDustTag && hasFireTag && hasImpactTag && finiteDust && finiteFire;
        }

        public int problemCount() {
            return (hasDustTag ? 0 : 1)
                    + (hasFireTag ? 0 : 1)
                    + (hasImpactTag ? 0 : 1)
                    + (finiteDust ? 0 : 1)
                    + (finiteFire ? 0 : 1);
        }

        public List<String> issues() {
            List<String> issues = new ArrayList<>();
            if (!hasDustTag) {
                issues.add("missing_dust");
            }
            if (!hasFireTag) {
                issues.add("missing_fire");
            }
            if (!hasImpactTag) {
                issues.add("missing_impact");
            }
            if (!finiteDust) {
                issues.add("non_finite_dust");
            }
            if (!finiteFire) {
                issues.add("non_finite_fire");
            }
            return List.copyOf(issues);
        }

        public String summary() {
            return "hasDust=" + hasDustTag
                    + " hasFire=" + hasFireTag
                    + " hasImpact=" + hasImpactTag
                    + " finiteDust=" + finiteDust
                    + " finiteFire=" + finiteFire
                    + " problems=" + problemCount()
                    + " issues=" + issues()
                    + " clean=" + clean();
        }
    }
}
