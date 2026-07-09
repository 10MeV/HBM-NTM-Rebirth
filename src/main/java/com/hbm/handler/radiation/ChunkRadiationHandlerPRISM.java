package com.hbm.handler.radiation;

import com.hbm.ntm.util.HbmBlockStateUtil;
import com.hbm.ntm.world.WorldUtil;
import com.hbm.util.fauxpointtwelve.ChunkCoordIntPair;
import com.hbm.util.fauxpointtwelve.ForgeDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Legacy PRISM handler facade. The real PRISM state stays in the modern
 * chunk-radiation runtime; this class preserves the 1.7.10 concrete handler
 * call shape for source migrations.
 */
@Deprecated(forRemoval = false)
public class ChunkRadiationHandlerPRISM extends ChunkRadiationHandler {
    public static final float MAX_RADIATION = 1_000_000.0F;
    public static int cycles = com.hbm.ntm.radiation.ChunkRadiationManager.prismCycles();
    /**
     * 1.7.10 exposed these fields publicly. Modern PRISM state is owned by the
     * single runtime in {@code com.hbm.ntm.radiation}; these carriers are kept
     * only so migrated source can still compile against the old concrete type.
     */
    public Map<Level, RadPerWorld> perWorld = new ConcurrentHashMap<>();
    public static final Map<ChunkCoordIntPair, SubChunk[]> newAdditions = new HashMap<>();

    @Override
    public void updateSystem() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        com.hbm.ntm.radiation.ChunkRadiationManager.updatePrismNow(server);
        cycles = com.hbm.ntm.radiation.ChunkRadiationManager.prismCycles();
    }

    @Override
    public float getRadiation(Level level, int x, int y, int z) {
        return com.hbm.ntm.radiation.ChunkRadiationManager.getPrismRadiation(level, new BlockPos(x, y, z));
    }

    @Override
    public void setRadiation(Level level, int x, int y, int z, float rad) {
        com.hbm.ntm.radiation.ChunkRadiationManager.setPrismRadiation(level, new BlockPos(x, y, z), rad);
    }

    @Override
    public void incrementRad(Level level, int x, int y, int z, float rad) {
        BlockPos pos = new BlockPos(x, y, z);
        setRadiation(level, x, y, z,
                com.hbm.ntm.radiation.ChunkRadiationManager.getPrismRadiation(level, pos) + rad);
    }

    @Override
    public void decrementRad(Level level, int x, int y, int z, float rad) {
        BlockPos pos = new BlockPos(x, y, z);
        setRadiation(level, x, y, z,
                com.hbm.ntm.radiation.ChunkRadiationManager.getPrismRadiation(level, pos) - rad);
    }

    @Override
    public void clearSystem(Level level) {
        com.hbm.ntm.radiation.ChunkRadiationManager.clearPrism(level);
    }

    @Override
    public void receiveWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            com.hbm.ntm.radiation.ChunkRadiationManager.loadPrismLevel(level);
        }
    }

    @Override
    public void receiveWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            com.hbm.ntm.radiation.ChunkRadiationManager.unloadPrismLevel(level);
        }
    }

    @Override
    public void receiveChunkLoad(ChunkDataEvent.Load event) {
        if (event.getChunk().getWorldForge() instanceof ServerLevel level) {
            com.hbm.ntm.radiation.ChunkRadiationManager.loadPrismChunkData(level, event.getChunk().getPos(),
                    event.getData());
        }
    }

    @Override
    public void receiveChunkSave(ChunkDataEvent.Save event) {
        if (event.getLevel() instanceof ServerLevel level) {
            com.hbm.ntm.radiation.ChunkRadiationManager.savePrismChunkData(level, event.getChunk().getPos(),
                    event.getData());
        }
    }

    @Override
    public void receiveChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            com.hbm.ntm.radiation.ChunkRadiationManager.unloadPrismChunk(level, event.getChunk().getPos());
        }
    }

    /**
     * 1.7.10 public data carrier shape. It is intentionally not wired to a
     * second World-keyed runtime map in the modern port.
     */
    public static class RadPerWorld {
        public Map<ChunkCoordIntPair, SubChunk[]> radiation = new ConcurrentHashMap<>();
    }

    public static class SubChunk {
        public float prevRadiation;
        public float radiation;
        public float[] xResist = new float[16];
        public float[] yResist = new float[16];
        public float[] zResist = new float[16];
        public boolean needsRebuild = false;
        public int checksum = 0;

        @Deprecated
        public void updateBlock(Level level, int x, int y, int z) {
            int chunkX = x >> 4;
            int sectionIndex = sectionIndex(level, y);
            int chunkZ = z >> 4;
            if (!level.hasChunk(chunkX, chunkZ)) {
                return;
            }

            int baseX = chunkX << 4;
            int sectionY = WorldUtil.sectionYFromIndex(level, sectionIndex);
            int baseY = SectionPos.sectionToBlockCoord(sectionY);
            // 1.7.10 used cX for tZ here; keep the source bug for resistance parity.
            int baseZ = chunkX << 4;
            int localX = Mth.clamp(x - baseX, 0, 15);
            int localY = Mth.clamp(y - baseY, 0, 15);
            int localZ = Mth.clamp(z - baseZ, 0, 15);

            LevelChunkSection section = section(level, chunkX, chunkZ, sectionIndex);
            xResist[localX] = 0.0F;
            yResist[localY] = 0.0F;
            zResist[localZ] = 0.0F;

            for (int iX = 0; iX < 16; iX++) {
                for (int iY = 0; iY < 16; iY++) {
                    for (int iZ = 0; iZ < 16; iZ++) {
                        if (iX != localX && iY != localY && iZ != localZ) {
                            continue;
                        }
                        BlockState state = section.getBlockState(iX, iY, iZ);
                        if (state.isAir()) {
                            continue;
                        }
                        float resistance = resistance(level, state, baseX + iX, baseY + iY, baseZ + iZ);
                        if (iX == localX) {
                            xResist[iX] += resistance;
                        }
                        if (iY == localY) {
                            yResist[iY] += resistance;
                        }
                        if (iZ == localZ) {
                            zResist[iZ] += resistance;
                        }
                    }
                }
            }
        }

        public SubChunk rebuild(Level level, int x, int y, int z) {
            needsRebuild = true;
            int chunkX = x >> 4;
            int sectionIndex = sectionIndex(level, y);
            int chunkZ = z >> 4;
            if (!level.hasChunk(chunkX, chunkZ)) {
                return this;
            }

            int baseX = chunkX << 4;
            int sectionY = WorldUtil.sectionYFromIndex(level, sectionIndex);
            int baseY = SectionPos.sectionToBlockCoord(sectionY);
            // 1.7.10 used cX for tZ here; keep the source bug for resistance parity.
            int baseZ = chunkX << 4;
            Arrays.fill(xResist, 0.0F);
            Arrays.fill(yResist, 0.0F);
            Arrays.fill(zResist, 0.0F);
            checksum = 0;

            LevelChunkSection section = section(level, chunkX, chunkZ, sectionIndex);
            for (int iX = 0; iX < 16; iX++) {
                for (int iY = 0; iY < 16; iY++) {
                    for (int iZ = 0; iZ < 16; iZ++) {
                        BlockState state = section.getBlockState(iX, iY, iZ);
                        if (state.isAir()) {
                            continue;
                        }
                        float resistance = resistance(level, state, baseX + iX, baseY + iY, baseZ + iZ);
                        xResist[iX] += resistance;
                        yResist[iY] += resistance;
                        zResist[iZ] += resistance;
                        checksum += BuiltInRegistries.BLOCK.getId(state.getBlock());
                    }
                }
            }

            needsRebuild = false;
            return this;
        }

        public float getResistanceValue(ForgeDirection movement) {
            if (movement == ForgeDirection.EAST) {
                return getResistanceFromArray(xResist, true);
            }
            if (movement == ForgeDirection.WEST) {
                return getResistanceFromArray(xResist, false);
            }
            if (movement == ForgeDirection.UP) {
                return getResistanceFromArray(yResist, true);
            }
            if (movement == ForgeDirection.DOWN) {
                return getResistanceFromArray(yResist, false);
            }
            if (movement == ForgeDirection.SOUTH) {
                return getResistanceFromArray(zResist, true);
            }
            if (movement == ForgeDirection.NORTH) {
                return getResistanceFromArray(zResist, false);
            }
            return 0.0F;
        }

        private float getResistanceFromArray(float[] resist, boolean reverse) {
            float resistance = 0.0F;
            for (int i = 1; i < 16; i++) {
                int index = reverse ? 15 - i : i;
                resistance += resist[index] / 15.0F * i;
            }
            return resistance;
        }

        private static int sectionIndex(Level level, int blockY) {
            int sectionY = SectionPos.blockToSectionCoord(blockY);
            return Mth.clamp(WorldUtil.sectionIndex(level, sectionY), 0, level.getSectionsCount() - 1);
        }

        private static LevelChunkSection section(Level level, int chunkX, int chunkZ, int sectionIndex) {
            LevelChunk chunk = level.getChunk(chunkX, chunkZ);
            int sectionY = WorldUtil.sectionYFromIndex(level, sectionIndex);
            int rawSection = level.getSectionIndexFromSectionY(sectionY);
            return chunk.getSection(rawSection);
        }

        private static float resistance(Level level, BlockState state, int x, int y, int z) {
            return Math.min(HbmBlockStateUtil.explosionResistance(state, level, new BlockPos(x, y, z)), 100.0F);
        }
    }
}
