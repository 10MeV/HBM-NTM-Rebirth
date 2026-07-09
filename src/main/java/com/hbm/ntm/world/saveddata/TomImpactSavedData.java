package com.hbm.ntm.world.saveddata;

import com.hbm.ntm.HbmNtm;
import net.minecraft.nbt.CompoundTag;
import com.hbm.util.fauxpointtwelve.NBTTagCompound;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import com.hbm.util.fauxpointtwelve.WorldSavedData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TomImpactSavedData extends WorldSavedData {
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
        super(DATA_NAME);
    }

    public TomImpactSavedData(String tagName) {
        super(tagName);
    }

    private static TomImpactSavedData createData() {
        return new com.hbm.saveddata.TomSaveData(DATA_NAME);
    }

    public static TomImpactSavedData load(CompoundTag tag) {
        TomImpactSavedData data = createData();
        NBTTagCompound legacyTag = NBTTagCompound.copyOf(Objects.requireNonNull(tag, "tag"));
        try {
            data.readFromNBT(legacyTag);
        } catch (Exception exception) {
            HbmNtm.LOGGER.warn(
                    "Keeping partially loaded TOM impact SavedData after legacy root read failure, matching 1.7.10 MapStorage.",
                    exception);
        }
        return data;
    }

    public static TomImpactSavedData forLevel(ServerLevel level) {
        TomImpactSavedData data = WorldSavedDataHelper.get(level, DATA_NAME, TomImpactSavedData::load,
                TomImpactSavedData::createData);
        lastCachedUnsafe = data;
        return data;
    }

    public static Optional<TomImpactSavedData> forLevel(Level level) {
        Optional<TomImpactSavedData> data = WorldSavedDataHelper.get(level, DATA_NAME, TomImpactSavedData::load,
                TomImpactSavedData::createData);
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
        return WorldSavedDataHelper.getExisting(level, DATA_NAME, TomImpactSavedData::load);
    }

    public static Optional<TomImpactSavedData> getExisting(MinecraftServer server) {
        return WorldSavedDataHelper.getExisting(server, DATA_NAME, TomImpactSavedData::load);
    }

    public static Optional<TomImpactSavedData> getExisting(MinecraftServer server, ResourceKey<Level> dimension) {
        return WorldSavedDataHelper.getExisting(server, dimension, DATA_NAME,
                TomImpactSavedData::load);
    }

    public static Optional<TomImpactSavedData> getExisting(Level level) {
        return WorldSavedDataHelper.getExisting(level, DATA_NAME, TomImpactSavedData::load);
    }

    public static TomImpactSavedData getData(ServerLevel level) {
        return forLevel(level);
    }

    public static Optional<TomImpactSavedData> getData(Level level) {
        return forLevel(level);
    }

    public static TomImpactSavedData getData(MinecraftServer server) {
        TomImpactSavedData data = WorldSavedDataHelper.get(server, DATA_NAME, TomImpactSavedData::load,
                TomImpactSavedData::createData);
        lastCachedUnsafe = data;
        return data;
    }

    public static Optional<TomImpactSavedData> getData(MinecraftServer server, ResourceKey<Level> dimension) {
        Optional<TomImpactSavedData> data = WorldSavedDataHelper.get(server, dimension, DATA_NAME,
                TomImpactSavedData::load, TomImpactSavedData::createData);
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
        return super.save(tag);
    }

    public void readFromNBT(NBTTagCompound tag) {
        readLegacyImpactFields(Objects.requireNonNull(tag, "tag"));
        loadDiagnostics = LoadDiagnostics.inspect(tag, snapshot());
    }

    public void writeToNBT(NBTTagCompound tag) {
        writeLegacyImpactFields(Objects.requireNonNull(tag, "tag"));
    }

    public boolean readLegacyImpact(CompoundTag tag) {
        NBTTagCompound legacyTag = NBTTagCompound.copyOf(Objects.requireNonNull(tag, "tag"));
        Snapshot loaded = readLegacyImpactTag(legacyTag);
        loadDiagnostics = LoadDiagnostics.inspect(legacyTag, loaded);
        return setImpactState(loaded.dust, loaded.fire, loaded.impact);
    }

    public void writeLegacyImpact(CompoundTag tag) {
        if (tag != null) {
            tag.putFloat(TAG_DUST, dust);
            tag.putFloat(TAG_FIRE, fire);
            tag.putBoolean(TAG_IMPACT, impact);
        }
    }

    public CompoundTag writeLegacyImpactTag() {
        CompoundTag tag = new CompoundTag();
        writeLegacyImpact(tag);
        return tag;
    }

    public static boolean hasAnyLegacyImpactTag(CompoundTag tag) {
        if (tag == null) {
            return false;
        }
        NBTTagCompound legacyTag = NBTTagCompound.copyOf(tag);
        return legacyTag.hasKey(TAG_DUST) || legacyTag.hasKey(TAG_FIRE) || legacyTag.hasKey(TAG_IMPACT);
    }

    public static boolean hasCompleteLegacyImpactTag(CompoundTag tag) {
        if (tag == null) {
            return false;
        }
        NBTTagCompound legacyTag = NBTTagCompound.copyOf(tag);
        return legacyTag.hasKey(TAG_DUST) && legacyTag.hasKey(TAG_FIRE) && legacyTag.hasKey(TAG_IMPACT);
    }

    public static LoadDiagnostics inspectLegacyImpactTag(CompoundTag tag) {
        return LoadDiagnostics.inspect(tag, readLegacyImpactTag(tag));
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

    public boolean applyLegacyImpactTag(CompoundTag tag) {
        Snapshot snapshot = readLegacyImpactTag(tag);
        boolean changed = Float.compare(dust, snapshot.dust) != 0
                || Float.compare(fire, snapshot.fire) != 0
                || impact != snapshot.impact;
        applySnapshot(snapshot);
        return changed;
    }

    public CompoundTag writeSnapshotTag() {
        return writeSnapshotTag(snapshot());
    }

    public CompoundTag writeLegacySnapshotTag() {
        return writeSnapshotTag();
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

    public static CompoundTag writeLegacyImpactTag(Snapshot snapshot) {
        return writeSnapshotTag(snapshot);
    }

    public static Snapshot readSnapshotTag(CompoundTag tag) {
        CompoundTag source = tag == null ? new CompoundTag() : tag;
        return readLegacyImpactTag(source);
    }

    public static Snapshot readLegacyImpactTag(CompoundTag tag) {
        NBTTagCompound source = NBTTagCompound.copyOf(Objects.requireNonNull(tag, "tag"));
        return new Snapshot(source.getFloat(TAG_DUST), source.getFloat(TAG_FIRE), source.getBoolean(TAG_IMPACT));
    }

    public static Snapshot readPermaSyncData(CompoundTag data) {
        CompoundTag source = data == null ? new CompoundTag() : data.getCompound(TAG_PERMA_SYNC);
        return readSnapshotTag(source);
    }

    public void markDirty() {
        setDirty();
    }

    private void readLegacyImpactFields(NBTTagCompound tag) {
        this.dust = tag.getFloat(TAG_DUST);
        this.fire = tag.getFloat(TAG_FIRE);
        this.impact = tag.getBoolean(TAG_IMPACT);
    }

    private void writeLegacyImpactFields(NBTTagCompound tag) {
        tag.setFloat(TAG_DUST, dust);
        tag.setFloat(TAG_FIRE, fire);
        tag.setBoolean(TAG_IMPACT, impact);
    }

    public record Snapshot(float dust, float fire, boolean impact) {
        public static final Snapshot EMPTY = new Snapshot(0.0F, 0.0F, false);

        public void writeLegacyImpact(CompoundTag tag) {
            if (tag != null) {
                tag.putFloat(TAG_DUST, dust);
                tag.putFloat(TAG_FIRE, fire);
                tag.putBoolean(TAG_IMPACT, impact);
            }
        }

        public CompoundTag writeLegacyImpactTag() {
            return TomImpactSavedData.writeLegacyImpactTag(this);
        }

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
            Snapshot value = snapshot == null ? Snapshot.EMPTY : snapshot;
            if (tag instanceof NBTTagCompound legacyTag) {
                return new LoadDiagnostics(legacyTag.hasKey(TAG_DUST), legacyTag.hasKey(TAG_FIRE),
                        legacyTag.hasKey(TAG_IMPACT), Float.isFinite(value.dust()), Float.isFinite(value.fire()));
            }
            CompoundTag source = tag == null ? new CompoundTag() : tag;
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
