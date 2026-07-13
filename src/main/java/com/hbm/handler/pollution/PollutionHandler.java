package com.hbm.handler.pollution;

import com.hbm.config.MobConfig;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionSavedData;
import com.hbm.util.fauxpointtwelve.ChunkCoordIntPair;
import com.hbm.util.fauxpointtwelve.NBTTagCompound;
import com.hbm.util.fauxpointtwelve.NBTTagList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

/**
 * Legacy package facade for the 1.7.10 pollution handler.
 */
@Deprecated(forRemoval = false)
public class PollutionHandler {
    public static final String fileName = PollutionSavedData.DATA_NAME + ".dat";
    public static final float SOOT_PER_SECOND = PollutionManager.SOOT_PER_SECOND;
    public static final float HEAVY_METAL_PER_SECOND = PollutionManager.HEAVY_METAL_PER_SECOND;
    public static final float POISON_PER_SECOND = PollutionManager.POISON_PER_SECOND;

    public static Vec3 targetCoords;

    public static void incrementPollution(Level level, int x, int y, int z, PollutionType type, float amount) {
        incrementPollution(level, new BlockPos(x, y, z), type, amount);
    }

    public static void incrementPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        PollutionManager.incrementPollution(level, pos, toModern(type), amount);
    }

    public static void decrementPollution(Level level, int x, int y, int z, PollutionType type, float amount) {
        decrementPollution(level, new BlockPos(x, y, z), type, amount);
    }

    public static void decrementPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        PollutionManager.decrementPollution(level, pos, toModern(type), amount);
    }

    public static boolean applyPollutionDelta(Level level, int x, int y, int z, PollutionType type, float amount) {
        return applyPollutionDelta(level, new BlockPos(x, y, z), type, amount);
    }

    public static boolean applyPollutionDelta(Level level, BlockPos pos, PollutionType type, float amount) {
        return PollutionManager.applyPollutionDelta(level, pos, toModern(type), amount);
    }

    public static void setPollution(Level level, int x, int y, int z, PollutionType type, float amount) {
        setPollution(level, new BlockPos(x, y, z), type, amount);
    }

    public static void setPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        PollutionManager.setPollution(level, pos, toModern(type), amount);
    }

    public static float getPollution(Level level, int x, int y, int z, PollutionType type) {
        return getPollution(level, new BlockPos(x, y, z), type);
    }

    public static float getPollution(Level level, BlockPos pos, PollutionType type) {
        return PollutionManager.getPollution(level, pos, toModern(type));
    }

    public static PollutionData getPollutionData(Level level, int x, int y, int z) {
        return getPollutionData(level, new BlockPos(x, y, z));
    }

    public static PollutionData getPollutionData(Level level, BlockPos pos) {
        PollutionSavedData.PollutionSample sample = PollutionManager.getPollutionDataOrNull(level, pos);
        return sample == null ? null : PollutionData.fromModern(sample);
    }

    public static void setRampantTarget(Level level, BlockPos pos) {
        if (pos != null) {
            targetCoords = Vec3.atLowerCornerOf(pos);
            PollutionManager.setRampantTarget(level, pos);
        }
    }

    public static PollutionType fromModern(com.hbm.ntm.pollution.PollutionType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case SOOT -> PollutionType.SOOT;
            case POISON -> PollutionType.POISON;
            case HEAVYMETAL -> PollutionType.HEAVYMETAL;
            case FALLOUT -> PollutionType.FALLOUT;
        };
    }

    public static com.hbm.ntm.pollution.PollutionType toModern(PollutionType type) {
        return type == null ? null : type.modern();
    }

    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load event) {
        // Modern SavedData is loaded lazily by PollutionManager; no old file IO is restored here.
    }

    @SubscribeEvent
    public void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            PollutionManager.unloadLevel(level);
        }
    }

    @SubscribeEvent
    public void onWorldSave(LevelEvent.Save event) {
        // Modern SavedData writes through DataStorage; this method only preserves the old event shape.
    }

    @SubscribeEvent
    public void updateSystem(TickEvent.ServerTickEvent event) {
        if (event.getServer() != null && event.phase == TickEvent.Phase.END) {
            PollutionManager.tick(event.getServer().getAllLevels());
        }
    }

    @SubscribeEvent
    public void decorateMob(MobSpawnEvent.FinalizeSpawn event) {
        Mob mob = event.getEntity();
        PollutionManager.decorateMob(mob);
    }

    @SubscribeEvent
    public void rampantScoutPopulator(LevelEvent.PotentialSpawns event) {
        if (!event.isCanceled() && event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
            PollutionManager.trySpawnRampantScout(level, event.getPos());
        }
    }

    @SubscribeEvent
    public void rampantTargetSetter(PlayerSleepInBedEvent event) {
        if (!MobConfig.rampantGlyphidGuidance() || event.getEntity().level().isClientSide) {
            return;
        }
        event.getOptionalPos().ifPresent(pos -> {
            targetCoords = Vec3.atLowerCornerOf(pos);
            PollutionManager.setRampantTarget(event.getEntity().level(), pos);
        });
    }

    public static class PollutionPerWorld {
        public HashMap<ChunkCoordIntPair, PollutionData> pollution = new HashMap<>();

        public PollutionPerWorld() {
        }

        public PollutionPerWorld(NBTTagCompound data) {
            NBTTagList list = data.getTagList("entries", Tag.TAG_COMPOUND);

            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbt = list.getCompoundTagAt(i);
                int chunkX = nbt.getInteger("chunkX");
                int chunkZ = nbt.getInteger("chunkZ");
                pollution.put(new ChunkCoordIntPair(chunkX, chunkZ), PollutionData.fromNBT(nbt));
            }
        }

        public PollutionPerWorld(CompoundTag data) {
            this(asLegacy(data));
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound data = new NBTTagCompound();
            NBTTagList list = new NBTTagList();

            for (Entry<ChunkCoordIntPair, PollutionData> entry : pollution.entrySet()) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setInteger("chunkX", entry.getKey().chunkXPos);
                nbt.setInteger("chunkZ", entry.getKey().chunkZPos);
                entry.getValue().toNBT(nbt);
                list.appendTag(nbt);
            }

            data.setTag("entries", list);
            return data;
        }
    }

    public static class PollutionData {
        public float[] pollution = new float[PollutionType.values().length];

        public static PollutionData fromModern(PollutionSavedData.PollutionSample sample) {
            PollutionData data = new PollutionData();
            if (sample != null) {
                for (PollutionType type : PollutionType.values()) {
                    data.pollution[type.ordinal()] = sample.get(type.modern());
                }
            }
            return data;
        }

        public PollutionSavedData.PollutionSample toModern() {
            PollutionSavedData.PollutionSample sample = new PollutionSavedData.PollutionSample();
            for (PollutionType type : PollutionType.values()) {
                sample.set(type.modern(), pollution[type.ordinal()]);
            }
            return sample;
        }

        public static PollutionData fromNBT(NBTTagCompound tag) {
            PollutionData data = new PollutionData();
            for (PollutionType type : PollutionType.values()) {
                data.pollution[type.ordinal()] = tag.getFloat(PollutionSavedData.tagName(type.modern()));
            }
            return data;
        }

        public static PollutionData fromNBT(CompoundTag tag) {
            if (tag instanceof NBTTagCompound legacyTag) {
                return fromNBT(legacyTag);
            }
            PollutionData data = new PollutionData();
            for (PollutionType type : PollutionType.values()) {
                data.pollution[type.ordinal()] = tag.getFloat(PollutionSavedData.tagName(type.modern()));
            }
            return data;
        }

        public void toNBT(NBTTagCompound tag) {
            for (PollutionType type : PollutionType.values()) {
                tag.setFloat(PollutionSavedData.tagName(type.modern()), pollution[type.ordinal()]);
            }
        }

        public void toNBT(CompoundTag tag) {
            if (tag instanceof NBTTagCompound legacyTag) {
                toNBT(legacyTag);
                return;
            }
            for (PollutionType type : PollutionType.values()) {
                tag.putFloat(PollutionSavedData.tagName(type.modern()), pollution[type.ordinal()]);
            }
        }
    }

    public enum PollutionType {
        SOOT,
        POISON,
        HEAVYMETAL,
        FALLOUT;

        public com.hbm.ntm.pollution.PollutionType modern() {
            return switch (this) {
                case SOOT -> com.hbm.ntm.pollution.PollutionType.SOOT;
                case POISON -> com.hbm.ntm.pollution.PollutionType.POISON;
                case HEAVYMETAL -> com.hbm.ntm.pollution.PollutionType.HEAVYMETAL;
                case FALLOUT -> com.hbm.ntm.pollution.PollutionType.FALLOUT;
            };
        }

        public static PollutionType byName(String name) {
            if (name == null) {
                return null;
            }
            String normalized = name.toUpperCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
            for (PollutionType type : values()) {
                if (type.name().replace("_", "").equals(normalized)) {
                    return type;
                }
            }
            return null;
        }
    }

    public PollutionHandler() {
    }

    private static NBTTagCompound asLegacy(CompoundTag data) {
        if (data == null) {
            throw new NullPointerException();
        }
        return NBTTagCompound.copyOf(data);
    }
}
