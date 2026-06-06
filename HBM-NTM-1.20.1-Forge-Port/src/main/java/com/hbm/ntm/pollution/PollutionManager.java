package com.hbm.ntm.pollution;

import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.pollution.PollutionSavedData.PollutionGridPos;
import com.hbm.ntm.pollution.PollutionSavedData.PollutionSample;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PollutionManager {
    public static final float MAX_POLLUTION = 10_000.0F;
    public static final float SOOT_PER_SECOND = 1.0F / 25.0F;
    public static final float HEAVY_METAL_PER_SECOND = 1.0F / 50.0F;
    public static final float POISON_PER_SECOND = 1.0F / 50.0F;

    private static final int DIFFUSION_INTERVAL_TICKS = 60;
    private static final float DESTRUCTION_THRESHOLD = 15.0F;
    private static final int DESTRUCTION_COUNT = 5;
    private static final UUID MAX_HEALTH_MODIFIER = UUID.fromString("25462f6c-2cb2-4ca8-9b47-3a011cc61207");
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("8f442d7c-d03f-49f6-a040-249ae742eed9");
    private static final Map<ResourceKey<Level>, Integer> DIFFUSION_TIMERS = new HashMap<>();

    public static void incrementPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        getData(serverLevel).add(PollutionGridPos.ofBlock(pos.getX(), pos.getZ()), type,
                amount * RadiationConfig.POLLUTION_MULT.get().floatValue());
    }

    public static void decrementPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        incrementPollution(level, pos, type, -amount);
    }

    public static void setPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        getData(serverLevel).set(PollutionGridPos.ofBlock(pos.getX(), pos.getZ()), type, amount);
    }

    public static float getPollution(Level level, BlockPos pos, PollutionType type) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return 0.0F;
        }
        return getData(serverLevel).get(PollutionGridPos.ofBlock(pos.getX(), pos.getZ()), type);
    }

    public static PollutionSample getPollutionData(Level level, BlockPos pos) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return new PollutionSample();
        }
        return getData(serverLevel).get(PollutionGridPos.ofBlock(pos.getX(), pos.getZ()));
    }

    public static void clear(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            getData(serverLevel).clear();
        }
    }

    public static PollutionSavedData.Stats getStats(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return new PollutionSavedData.Stats(0, 0, 0.0F, 0.0F, 0.0F, 0.0F,
                    new float[PollutionType.values().length], new float[PollutionType.values().length]);
        }
        return getData(serverLevel).stats(pos -> isPollutionGridLoaded(serverLevel, pos));
    }

    public static int pruneUnloaded(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }
        return getData(serverLevel).pruneLoaded(pos -> isPollutionGridLoaded(serverLevel, pos));
    }

    public static void tick(ServerLevel level) {
        if (!isEnabled()) {
            return;
        }

        handleWorldDestruction(level);

        ResourceKey<Level> dimension = level.dimension();
        int timer = DIFFUSION_TIMERS.getOrDefault(dimension, 0) + 1;
        if (timer >= DIFFUSION_INTERVAL_TICKS) {
            getData(level).updateDiffusion();
            timer = 0;
        }
        DIFFUSION_TIMERS.put(dimension, timer);
    }

    public static void decorateMob(Mob mob) {
        if (!isEnabled() || mob.level().isClientSide) {
            return;
        }
        if (mob.getType().getCategory().isFriendly()) {
            return;
        }

        float soot = getPollution(mob.level(), mob.blockPosition(), PollutionType.SOOT);
        if (soot <= RadiationConfig.POLLUTION_BUFF_MOB_THRESHOLD.get().floatValue()) {
            return;
        }
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null
                && mob.getAttribute(Attributes.MAX_HEALTH).getModifier(MAX_HEALTH_MODIFIER) == null) {
            mob.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(
                    MAX_HEALTH_MODIFIER, "Soot Anger Health Increase", 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null
                && mob.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(ATTACK_DAMAGE_MODIFIER) == null) {
            mob.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(
                    ATTACK_DAMAGE_MODIFIER, "Soot Anger Damage Increase", 1.5D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        mob.heal(mob.getMaxHealth());
    }

    public static void unloadLevel(Level level) {
        DIFFUSION_TIMERS.remove(level.dimension());
    }

    private static void handleWorldDestruction(ServerLevel level) {
        List<Map.Entry<Long, PollutionSample>> entries = getData(level).entriesSnapshot();
        if (entries.isEmpty()) {
            return;
        }

        for (Map.Entry<Long, PollutionSample> pollution : entries) {
            float poison = pollution.getValue().get(PollutionType.POISON);
            if (poison < DESTRUCTION_THRESHOLD) {
                continue;
            }

            PollutionGridPos pos = PollutionGridPos.of(pollution.getKey());
            for (int i = 0; i < DESTRUCTION_COUNT; i++) {
                int x = pos.minBlockX() + level.random.nextInt(64);
                int z = pos.minBlockZ() + level.random.nextInt(64);
                if (!level.hasChunk(x >> 4, z >> 4)) {
                    continue;
                }

                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - level.random.nextInt(3) + 1;
                y = Math.max(level.getMinBuildHeight(), Math.min(y, level.getMaxBuildHeight() - 1));
                BlockPos blockPos = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(blockPos);
                if (state.is(Blocks.GRASS_BLOCK) || isPlainDirt(state)) {
                    level.setBlock(blockPos, Blocks.COARSE_DIRT.defaultBlockState(), Block.UPDATE_ALL);
                } else if (state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS) || state.is(BlockTags.LEAVES)
                        || state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS)) {
                    level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static boolean isPlainDirt(BlockState state) {
        return state.is(Blocks.DIRT);
    }

    private static boolean isPollutionGridLoaded(ServerLevel level, PollutionGridPos pos) {
        for (int dx = 0; dx < 4; dx++) {
            for (int dz = 0; dz < 4; dz++) {
                if (level.hasChunk((pos.x() << 2) + dx, (pos.z() << 2) + dz)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isEnabled() {
        return RadiationConfig.ENABLE_POLLUTION.get();
    }

    private static PollutionSavedData getData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                PollutionSavedData::load,
                PollutionSavedData::new,
                PollutionSavedData.DATA_NAME);
    }

    private PollutionManager() {
    }
}
