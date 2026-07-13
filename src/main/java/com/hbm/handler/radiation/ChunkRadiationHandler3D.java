package com.hbm.handler.radiation;

import com.hbm.interfaces.Untested;
import com.hbm.util.fauxpointtwelve.ChunkCoordIntPair;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Legacy 3D chunk-radiation handler. This preserves the 1.7.10 optional
 * handler shape and chunk-NBT format; it is not selected by any modern config.
 */
@Deprecated(forRemoval = false)
public class ChunkRadiationHandler3D extends ChunkRadiationHandler {
    private final Map<Level, ThreeDimRadiationPerWorld> perWorld = new HashMap<>();

    @Override
    @Untested
    public float getRadiation(Level level, int x, int y, int z) {
        ThreeDimRadiationPerWorld radWorld = perWorld.get(level);

        if (radWorld != null) {
            ChunkCoordIntPair coords = new ChunkCoordIntPair(x >> 4, z >> 4);
            int yReg = legacyYRegion(level, y);

            Float rad = radWorld.radiation.get(coords)[yReg];
            return rad == null ? 0.0F : rad;
        }

        return 0.0F;
    }

    @Override
    public void setRadiation(Level level, int x, int y, int z, float rad) {
        ThreeDimRadiationPerWorld radWorld = perWorld.get(level);

        if (radWorld != null) {
            if (level != null && level.hasChunk(x >> 4, z >> 4)) {
                ChunkCoordIntPair coords = new ChunkCoordIntPair(x >> 4, z >> 4);
                int yReg = legacyYRegion(level, y);

                if (radWorld.radiation.containsKey(coords)) {
                    radWorld.radiation.get(coords)[yReg] = rad;
                }

                level.getChunk(x >> 4, z >> 4).setUnsaved(true);
            }
        }
    }

    @Override
    public void incrementRad(Level level, int x, int y, int z, float rad) {
        setRadiation(level, x, y, z, getRadiation(level, x, y, z) + rad);
    }

    @Override
    public void decrementRad(Level level, int x, int y, int z, float rad) {
        setRadiation(level, x, y, z, Math.max(getRadiation(level, x, y, z) - rad, 0.0F));
    }

    @Override
    @Untested
    public void updateSystem() {
        for (Entry<Level, ThreeDimRadiationPerWorld> entry : perWorld.entrySet()) {
            Map<ChunkCoordIntPair, Float[]> radiation = entry.getValue().radiation;
            Map<ChunkCoordIntPair, Float[]> buff = new HashMap<>(radiation);
            radiation.clear();

            for (Entry<ChunkCoordIntPair, Float[]> chunk : buff.entrySet()) {
                ChunkCoordIntPair coord = chunk.getKey();

                for (int y = 0; y < 16; y++) {
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            for (int k = -1; k <= 1; k++) {
                                int type = Math.abs(i) + Math.abs(j) + Math.abs(k);

                                if (type == 3) {
                                    continue;
                                }

                                float percent = type == 0 ? 0.6F : type == 1 ? 0.075F : 0.025F;
                                ChunkCoordIntPair newCoord =
                                        new ChunkCoordIntPair(coord.chunkXPos + i, coord.chunkZPos + k);

                                if (buff.containsKey(newCoord)) {
                                    int newY = Mth.clamp(y + j, 0, 15);
                                    Float[] vals = radiation.get(newCoord);
                                    float newRad = vals[newY] + chunk.getValue()[newY] * percent;
                                    vals[newY] = Math.max(0.0F, newRad * 0.999F - 0.05F);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void clearSystem(Level level) {
        ThreeDimRadiationPerWorld radWorld = perWorld.get(level);

        if (radWorld != null) {
            radWorld.radiation.clear();
        }
    }

    @Override
    public void receiveWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            perWorld.put(level, new ThreeDimRadiationPerWorld());
        }
    }

    @Override
    public void receiveWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            perWorld.remove(level);
        }
    }

    private static final String NBT_KEY_CHUNK_RADIATION = "hfr_3d_radiation_";

    @Override
    public void receiveChunkLoad(ChunkDataEvent.Load event) {
        if (event.getChunk().getWorldForge() instanceof Level level && !level.isClientSide()) {
            ThreeDimRadiationPerWorld radWorld = perWorld.get(level);

            if (radWorld != null) {
                Float[] vals = new Float[16];

                for (int i = 0; i < 16; i++) {
                    vals[i] = event.getData().getFloat(NBT_KEY_CHUNK_RADIATION + i);
                }

                radWorld.radiation.put(legacyPos(event.getChunk().getPos()), vals);
            }
        }
    }

    @Override
    public void receiveChunkSave(ChunkDataEvent.Save event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            ThreeDimRadiationPerWorld radWorld = perWorld.get(level);

            if (radWorld != null) {
                Float[] vals = radWorld.radiation.get(legacyPos(event.getChunk().getPos()));

                for (int i = 0; i < 16; i++) {
                    float rad = vals[i] == null ? 0.0F : vals[i];
                    event.getData().putFloat(NBT_KEY_CHUNK_RADIATION + i, rad);
                }
            }
        }
    }

    @Override
    public void receiveChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            ThreeDimRadiationPerWorld radWorld = perWorld.get(level);

            if (radWorld != null) {
                radWorld.radiation.remove(event.getChunk());
            }
        }
    }

    private static int legacyYRegion(Level level, int y) {
        int minBuildHeight = level == null ? 0 : level.getMinBuildHeight();
        return Mth.clamp((y - minBuildHeight) >> 4, 0, 15);
    }

    private static ChunkCoordIntPair legacyPos(ChunkPos pos) {
        return new ChunkCoordIntPair(pos.x, pos.z);
    }

    public static class ThreeDimRadiationPerWorld {
        public Map<ChunkCoordIntPair, Float[]> radiation = new HashMap<>();
    }
}
