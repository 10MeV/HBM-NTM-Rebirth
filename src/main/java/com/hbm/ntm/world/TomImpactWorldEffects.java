package com.hbm.ntm.world;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.LegacyBurningEarthBlock;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.world.saveddata.TomImpactSavedData;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;

public final class TomImpactWorldEffects {
    private static final float ENTITY_BURN_DUST_LIMIT = 0.75F;
    private static final float LEGACY_LARGE_MOB_SIZE = 0.85F;
    private static final Method CHUNK_MAP_GET_CHUNKS = findChunkMapGetChunks();
    private static boolean warnedMissingLoadedChunkCarrier;

    public static TomImpactSavedData.ClimateTickResult tickLegacyWorldStart(ServerLevel level) {
        TomImpactSavedData data = TomImpactSavedData.forLevel(level);
        impactEffects(level, data);
        boolean changed = data.tickImpactClimate();
        burnExposedLivingEntities(level, data);
        return new TomImpactSavedData.ClimateTickResult(true, changed, data.snapshot());
    }

    public static void impactEffects(ServerLevel level) {
        impactEffects(level, TomImpactSavedData.forLevel(level));
    }

    public static void die(ServerLevel level, int x, int y, int z) {
        die(level, TomImpactSavedData.forLevel(level), new BlockPos(x, y, z));
    }

    public static void burn(ServerLevel level, int x, int y, int z) {
        burn(level, new BlockPos(x, y, z));
    }

    public static void handleExtinction(MobSpawnEvent.PositionCheck event) {
        ServerLevel level = event.getLevel().getLevel();
        TomImpactSavedData data = TomImpactSavedData.forLevel(level);
        if (!data.impact()) {
            return;
        }

        Mob mob = event.getEntity();
        boolean shouldDeny = false;
        if (level.dimension() == Level.OVERWORLD) {
            shouldDeny = mob.getBbHeight() >= LEGACY_LARGE_MOB_SIZE
                    || mob.getBbWidth() >= LEGACY_LARGE_MOB_SIZE && !(mob instanceof WaterAnimal) && !mob.isBaby();
        }
        if (mob instanceof WaterAnimal && new Random().nextInt(5) != 0) {
            shouldDeny = true;
        }

        if (shouldDeny) {
            event.setResult(Event.Result.DENY);
            mob.discard();
        }
    }

    private static void impactEffects(ServerLevel level, TomImpactSavedData data) {
        if (level.dimension() != Level.OVERWORLD || (data.dust() <= 0.0F && data.fire() <= 0.0F)) {
            return;
        }

        List<LevelChunk> loadedChunks = loadedFullChunks(level);
        int listSize = loadedChunks.size();
        if (listSize <= 0) {
            return;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = level.getMinBuildHeight();
        for (int i = 0; i < 3; i++) {
            LevelChunk chunk = loadedChunks.get(level.random.nextInt(listSize));
            int minX = chunk.getPos().getMinBlockX();
            int minZ = chunk.getPos().getMinBlockZ();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (level.random.nextBoolean()) {
                        continue;
                    }
                    int worldX = minX + x;
                    int worldZ = minZ + z;
                    int surfaceY = WorldUtil.legacyGetHeightValue(level, worldX, worldZ);
                    int span = Math.max(1, surfaceY - minY);
                    int worldY = surfaceY - level.random.nextInt(span);
                    pos.set(worldX, worldY, worldZ);

                    if (data.dust() > 0.0F) {
                        die(level, data, pos);
                    }
                    if (data.fire() > 0.0F) {
                        burn(level, pos);
                    }
                }
            }
        }
    }

    private static List<LevelChunk> loadedFullChunks(ServerLevel level) {
        List<LevelChunk> chunks = new ArrayList<>();
        if (CHUNK_MAP_GET_CHUNKS == null) {
            warnMissingLoadedChunkCarrier(null);
            return chunks;
        }
        try {
            Object result = CHUNK_MAP_GET_CHUNKS.invoke(level.getChunkSource().chunkMap);
            if (result instanceof Iterable<?> holders) {
                for (Object holderObject : holders) {
                    if (holderObject instanceof ChunkHolder holder) {
                        LevelChunk chunk = holder.getFullChunk();
                        if (chunk != null) {
                            chunks.add(chunk);
                        }
                    }
                }
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            warnMissingLoadedChunkCarrier(exception);
        }
        return chunks;
    }

    private static Method findChunkMapGetChunks() {
        try {
            Method method = net.minecraft.server.level.ChunkMap.class.getDeclaredMethod("getChunks");
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            warnMissingLoadedChunkCarrier(exception);
            return null;
        }
    }

    private static void warnMissingLoadedChunkCarrier(Exception exception) {
        if (warnedMissingLoadedChunkCarrier) {
            return;
        }
        warnedMissingLoadedChunkCarrier = true;
        HbmNtm.LOGGER.warn(
                "TOM impact world effects cannot enumerate loaded full chunks through ChunkMap#getChunks; "
                        + "skipping the legacy loadedChunks world-effect pass instead of using an approximate carrier.",
                exception);
    }

    private static void die(ServerLevel level, TomImpactSavedData data, BlockPos pos) {
        if (!level.isInWorldBounds(pos) || !level.isInWorldBounds(pos.above())) {
            return;
        }

        BlockPos above = pos.above();
        int light = Math.max(level.getBrightness(LightLayer.BLOCK, above),
                (int) (level.getMaxLocalRawBrightness(above) * (1.0F - data.dust())));
        if (light >= 4) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.GRASS_BLOCK)) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
        } else if (LegacyBurningEarthBlock.isLegacyPlantDeathTarget(state)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static void burn(ServerLevel level, BlockPos pos) {
        if (!level.isInWorldBounds(pos) || !level.isInWorldBounds(pos.above())) {
            return;
        }

        BlockPos above = pos.above();
        BlockState state = level.getBlockState(pos);
        BlockState aboveState = level.getBlockState(above);
        int skyLight = level.getBrightness(LightLayer.SKY, above);
        if (state.isFlammable(level, pos, Direction.UP) && aboveState.isAir() && skyLight >= 7) {
            if (LegacyBurningEarthBlock.isLegacyLeavesOrBush(state)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            level.setBlock(above, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
        } else if (LegacyBurningEarthBlock.isLegacyBurningEarthSpreadTarget(state)
                && !level.isRainingAt(pos) && skyLight >= 7) {
            level.setBlock(pos, ModBlocks.BURNING_EARTH.get().defaultBlockState(), Block.UPDATE_ALL);
        } else if (state.is(ModBlocks.FROZEN_DIRT.get()) && skyLight >= 7) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static void burnExposedLivingEntities(ServerLevel level, TomImpactSavedData data) {
        if (level.dimension() != Level.OVERWORLD || data.fire() <= 0.0F || data.dust() >= ENTITY_BURN_DUST_LIMIT) {
            return;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            pos.set((int) living.getX(), (int) living.getY(), (int) living.getZ());
            if (level.getBrightness(LightLayer.SKY, pos) > 7) {
                living.setSecondsOnFire(5);
                living.hurt(level.damageSources().onFire(), 2.0F);
            }
        }
    }

    private TomImpactWorldEffects() {
    }
}
