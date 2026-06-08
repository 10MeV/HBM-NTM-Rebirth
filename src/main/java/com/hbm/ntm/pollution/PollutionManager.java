package com.hbm.ntm.pollution;

import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.pollution.PollutionSavedData.PollutionGridPos;
import com.hbm.ntm.pollution.PollutionSavedData.PollutionSample;
import com.hbm.ntm.radiation.LegacyRadiationWorldUtil;
import com.hbm.ntm.world.saveddata.WorldSavedDataHelper;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

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
    private static int diffusionTimer;
    private static final Map<ResourceKey<Level>, BlockPos> RAMPANT_TARGETS = new HashMap<>();
    private static final Set<ResourceKey<Level>> LEGACY_ROOT_CHECKED = new HashSet<>();

    public static void incrementPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        getData(serverLevel).add(PollutionGridPos.ofBlock(pos.getX(), pos.getZ()), type,
                amount * RadiationConfig.POLLUTION_MULT.get().floatValue());
    }

    public static void incrementPollution(Level level, int x, int y, int z, PollutionType type, float amount) {
        incrementPollution(level, new BlockPos(x, y, z), type, amount);
    }

    public static void decrementPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        incrementPollution(level, pos, type, -amount);
    }

    public static void decrementPollution(Level level, int x, int y, int z, PollutionType type, float amount) {
        decrementPollution(level, new BlockPos(x, y, z), type, amount);
    }

    public static void setPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        getData(serverLevel).set(PollutionGridPos.ofBlock(pos.getX(), pos.getZ()), type, amount);
    }

    public static void setPollution(Level level, int x, int y, int z, PollutionType type, float amount) {
        setPollution(level, new BlockPos(x, y, z), type, amount);
    }

    public static float getPollution(Level level, BlockPos pos, PollutionType type) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return 0.0F;
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? 0.0F : data.get(PollutionGridPos.ofBlock(pos.getX(), pos.getZ()), type);
    }

    public static float getPollution(Level level, int x, int y, int z, PollutionType type) {
        return getPollution(level, new BlockPos(x, y, z), type);
    }

    public static PollutionSample getPollutionData(Level level, BlockPos pos) {
        PollutionSample sample = getPollutionDataOrNull(level, pos);
        return sample == null ? new PollutionSample() : sample;
    }

    public static PollutionSample getPollutionData(Level level, int x, int y, int z) {
        return getPollutionData(level, new BlockPos(x, y, z));
    }

    public static void setPollutionData(Level level, BlockPos pos, PollutionSample sample) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        getData(serverLevel).set(PollutionGridPos.ofBlock(pos.getX(), pos.getZ()), sample);
    }

    public static void setPollutionData(Level level, int x, int y, int z, PollutionSample sample) {
        setPollutionData(level, new BlockPos(x, y, z), sample);
    }

    public static void updatePollutionData(Level level, BlockPos pos, Consumer<PollutionSample> updater) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        PollutionGridPos gridPos = PollutionGridPos.ofBlock(pos.getX(), pos.getZ());
        PollutionSavedData data = getData(serverLevel);
        PollutionSample sample = data.get(gridPos);
        updater.accept(sample);
        data.set(gridPos, sample);
    }

    public static void updatePollutionData(Level level, int x, int y, int z, Consumer<PollutionSample> updater) {
        updatePollutionData(level, new BlockPos(x, y, z), updater);
    }

    @Nullable
    public static PollutionSample getPollutionDataOrNull(Level level, BlockPos pos) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? null : data.getOrNull(PollutionGridPos.ofBlock(pos.getX(), pos.getZ()));
    }

    @Nullable
    public static PollutionSample getPollutionDataOrNull(Level level, int x, int y, int z) {
        return getPollutionDataOrNull(level, new BlockPos(x, y, z));
    }

    public static void clear(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            PollutionSavedData data = getExistingData(serverLevel);
            if (data != null) {
                data.clear();
            }
        }
    }

    public static PollutionSavedData.Stats getStats(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return new PollutionSavedData.Stats(0, 0, 0.0F, 0.0F, 0.0F, 0.0F,
                    new float[PollutionType.values().length], new float[PollutionType.values().length]);
        }
        PollutionSavedData data = getExistingData(serverLevel);
        if (data == null) {
            return new PollutionSavedData.Stats(0, 0, 0.0F, 0.0F, 0.0F, 0.0F,
                    new float[PollutionType.values().length], new float[PollutionType.values().length]);
        }
        return data.stats(pos -> isPollutionGridLoaded(serverLevel, pos));
    }

    public static int pruneUnloaded(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? 0 : data.pruneLoaded(pos -> isPollutionGridLoaded(serverLevel, pos));
    }

    public static void tick(ServerLevel level) {
        tick(List.of(level));
    }

    public static void tick(Iterable<ServerLevel> levels) {
        if (!isEnabled()) {
            return;
        }

        List<ServerLevel> snapshot = new ArrayList<>();
        for (ServerLevel level : levels) {
            snapshot.add(level);
        }

        for (ServerLevel level : snapshot) {
            handleWorldDestruction(level);
        }

        diffusionTimer++;
        if (diffusionTimer < DIFFUSION_INTERVAL_TICKS) {
            return;
        }
        diffusionTimer = 0;

        for (ServerLevel level : snapshot) {
            PollutionSavedData data = getExistingData(level);
            if (data != null) {
                data.updateDiffusion();
            }
        }
    }

    public static void decorateMob(Mob mob) {
        if (!isEnabled() || mob.level().isClientSide) {
            return;
        }
        if (!(mob instanceof Enemy)) {
            return;
        }

        float soot = getPollution(mob.level(), mob.blockPosition(), PollutionType.SOOT);
        if (soot <= RadiationConfig.POLLUTION_BUFF_MOB_THRESHOLD.get().floatValue()) {
            return;
        }
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null
                && mob.getAttribute(Attributes.MAX_HEALTH).getModifier(MAX_HEALTH_MODIFIER) == null) {
            mob.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(
                    MAX_HEALTH_MODIFIER, "Soot Anger Health Increase", 1.0D, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null
                && mob.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(ATTACK_DAMAGE_MODIFIER) == null) {
            mob.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(
                    ATTACK_DAMAGE_MODIFIER, "Soot Anger Damage Increase", 1.5D, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        mob.heal(mob.getMaxHealth());
    }

    public static void unloadLevel(Level level) {
        RAMPANT_TARGETS.remove(level.dimension());
        LEGACY_ROOT_CHECKED.remove(level.dimension());
    }

    public static void setRampantTarget(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        RAMPANT_TARGETS.put(level.dimension(), pos.immutable());
    }

    public static Optional<BlockPos> getRampantTarget(Level level) {
        if (level == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(RAMPANT_TARGETS.get(level.dimension()));
    }

    private static void handleWorldDestruction(ServerLevel level) {
        PollutionSavedData data = getExistingData(level);
        if (data == null) {
            return;
        }
        List<Map.Entry<Long, PollutionSample>> entries = data.entriesSnapshot();
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

                int y = LegacyRadiationWorldUtil.legacyHeightValue(level, x, z) - level.random.nextInt(3) + 1;
                y = Math.max(level.getMinBuildHeight(), Math.min(y, level.getMaxBuildHeight() - 1));
                BlockPos blockPos = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(blockPos);
                if (state.is(Blocks.GRASS_BLOCK) || isPlainDirt(state)) {
                    level.setBlock(blockPos, Blocks.COARSE_DIRT.defaultBlockState(), Block.UPDATE_ALL);
                } else if (isLegacyLeafOrPlant(state)) {
                    level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static boolean isPlainDirt(BlockState state) {
        return state.is(Blocks.DIRT);
    }

    private static boolean isLegacyLeafOrPlant(BlockState state) {
        return state.is(BlockTags.LEAVES)
                || isLegacyPlantMaterial(state)
                || isLegacyVineMaterial(state);
    }

    private static boolean isLegacyPlantMaterial(BlockState state) {
        return state.is(Blocks.GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.DEAD_BUSH)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.CROPS);
    }

    private static boolean isLegacyVineMaterial(BlockState state) {
        return state.is(Blocks.VINE)
                || state.is(Blocks.TWISTING_VINES)
                || state.is(Blocks.TWISTING_VINES_PLANT)
                || state.is(Blocks.WEEPING_VINES)
                || state.is(Blocks.WEEPING_VINES_PLANT);
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
        PollutionSavedData existing = getExistingData(level);
        if (existing != null) {
            return existing;
        }
        return WorldSavedDataHelper.getWithFallback(level, PollutionSavedData.DATA_NAME, PollutionSavedData::load,
                PollutionSavedData::new, PollutionSavedData.MODERN_COMPAT_DATA_NAME);
    }

    @Nullable
    private static PollutionSavedData getExistingData(ServerLevel level) {
        PollutionSavedData data = WorldSavedDataHelper.getExistingWithFallback(level, PollutionSavedData.DATA_NAME,
                PollutionSavedData::load, PollutionSavedData.MODERN_COMPAT_DATA_NAME).orElse(null);
        PollutionSavedData legacyRootData = readLegacyRootData(level, data);
        return legacyRootData == null ? data : legacyRootData;
    }

    @Nullable
    private static PollutionSavedData readLegacyRootData(ServerLevel level, @Nullable PollutionSavedData data) {
        if (data != null && !data.isEmpty()) {
            return null;
        }
        if (!LEGACY_ROOT_CHECKED.add(level.dimension())) {
            return null;
        }

        for (String name : List.of(PollutionSavedData.DATA_NAME, PollutionSavedData.MODERN_COMPAT_DATA_NAME)) {
            CompoundTag root = readSavedDataRoot(level, name);
            if (root == null || !PollutionSavedData.hasLegacyRootEntries(root)) {
                continue;
            }
            PollutionSavedData legacyData = PollutionSavedData.load(root);
            if (legacyData.isEmpty()) {
                continue;
            }
            legacyData.setDirty();
            level.getDataStorage().set(PollutionSavedData.DATA_NAME, legacyData);
            return legacyData;
        }
        return null;
    }

    @Nullable
    private static CompoundTag readSavedDataRoot(ServerLevel level, String name) {
        try {
            return level.getDataStorage().readTagFromDisk(name,
                    SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private PollutionManager() {
    }
}
