package com.hbm.ntm.pollution;

import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.pollution.PollutionSavedData.PollutionGridPos;
import com.hbm.ntm.pollution.PollutionSavedData.PollutionSample;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.LegacyRadiationWorldUtil;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmMobEquipmentUtil;
import com.hbm.ntm.world.saveddata.WorldSavedDataHelper;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
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
        if (pos == null) {
            return;
        }
        incrementPollution(level, PollutionGridPos.ofBlock(pos), type, amount);
    }

    public static void incrementPollution(Level level, int x, int y, int z, PollutionType type, float amount) {
        incrementPollution(level, new BlockPos(x, y, z), type, amount);
    }

    public static void incrementPollution(Level level, PollutionGridPos pos, PollutionType type, float amount) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel) || pos == null || type == null
                || amount == 0.0F) {
            return;
        }
        getData(serverLevel).add(pos, type, amount * RadiationConfig.pollutionMultiplier());
    }

    public static void incrementPollution(Level level, BlockPos pos, PollutionSample amounts) {
        if (pos == null) {
            return;
        }
        incrementPollution(level, PollutionGridPos.ofBlock(pos), amounts);
    }

    public static void incrementPollution(Level level, int x, int y, int z, PollutionSample amounts) {
        incrementPollution(level, new BlockPos(x, y, z), amounts);
    }

    public static void incrementPollution(Level level, PollutionGridPos pos, PollutionSample amounts) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel) || pos == null
                || !hasAnyFiniteNonZero(amounts)) {
            return;
        }
        getData(serverLevel).addClamped(pos, scaledFinite(amounts, RadiationConfig.pollutionMultiplier()));
    }

    public static void decrementPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        incrementPollution(level, pos, type, -amount);
    }

    public static void decrementPollution(Level level, int x, int y, int z, PollutionType type, float amount) {
        decrementPollution(level, new BlockPos(x, y, z), type, amount);
    }

    public static void decrementPollution(Level level, PollutionGridPos pos, PollutionType type, float amount) {
        incrementPollution(level, pos, type, -amount);
    }

    public static boolean applyPollutionDelta(Level level, BlockPos pos, PollutionType type, float amount) {
        if (pos == null) {
            return false;
        }
        return applyPollutionDelta(level, PollutionGridPos.ofBlock(pos), type, amount);
    }

    public static boolean applyPollutionDelta(Level level, int x, int y, int z, PollutionType type, float amount) {
        return applyPollutionDelta(level, new BlockPos(x, y, z), type, amount);
    }

    public static boolean applyPollutionDelta(Level level, PollutionGridPos pos, PollutionType type, float amount) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel) || pos == null || type == null
                || !Float.isFinite(amount) || amount == 0.0F) {
            return false;
        }
        if (amount > 0.0F) {
            PollutionSavedData data = getData(serverLevel);
            data.add(pos, type, amount * RadiationConfig.pollutionMultiplier());
            return true;
        }

        PollutionSavedData data = getExistingData(serverLevel);
        if (data == null) {
            return false;
        }
        float debt = -amount;
        if (!data.get(pos).hasAtLeast(type, debt)) {
            return false;
        }
        data.add(pos, type, -debt * RadiationConfig.pollutionMultiplier());
        return true;
    }

    public static boolean applyPollutionDelta(Level level, BlockPos pos, PollutionSample amounts) {
        if (pos == null) {
            return false;
        }
        return applyPollutionDelta(level, PollutionGridPos.ofBlock(pos), amounts);
    }

    public static boolean applyPollutionDelta(Level level, int x, int y, int z, PollutionSample amounts) {
        return applyPollutionDelta(level, new BlockPos(x, y, z), amounts);
    }

    public static boolean applyPollutionDelta(Level level, PollutionGridPos pos, PollutionSample amounts) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel) || pos == null
                || !hasAnyFiniteNonZero(amounts)) {
            return false;
        }

        PollutionSavedData data = hasAnyNegative(amounts) ? getExistingData(serverLevel) : getData(serverLevel);
        if (data == null || !hasPollutionDebtsAvailable(data, pos, amounts)) {
            return false;
        }
        data.addClamped(pos, scaledFinite(amounts, RadiationConfig.pollutionMultiplier()));
        return true;
    }

    public static boolean decrementPollutionIfAvailable(Level level, BlockPos pos, PollutionType type, float amount) {
        if (pos == null) {
            return false;
        }
        return decrementPollutionIfAvailable(level, PollutionGridPos.ofBlock(pos), type, amount);
    }

    public static boolean decrementPollutionIfAvailable(Level level, int x, int y, int z, PollutionType type, float amount) {
        return decrementPollutionIfAvailable(level, new BlockPos(x, y, z), type, amount);
    }

    public static boolean decrementPollutionIfAvailable(Level level, PollutionGridPos pos, PollutionType type, float amount) {
        return Float.isFinite(amount) && amount > 0.0F && applyPollutionDelta(level, pos, type, -amount);
    }

    public static boolean hasPollutionAtLeast(Level level, BlockPos pos, PollutionType type, float amount) {
        if (pos == null) {
            return false;
        }
        return hasPollutionAtLeast(level, PollutionGridPos.ofBlock(pos), type, amount);
    }

    public static boolean hasPollutionAtLeast(Level level, int x, int y, int z, PollutionType type, float amount) {
        return hasPollutionAtLeast(level, new BlockPos(x, y, z), type, amount);
    }

    public static boolean hasPollutionAtLeast(Level level, PollutionGridPos pos, PollutionType type, float amount) {
        return isEnabled() && pos != null && type != null && Float.isFinite(amount) && amount >= 0.0F
                && getPollution(level, pos, type) >= amount;
    }

    public static void setPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        if (pos == null) {
            return;
        }
        setPollution(level, PollutionGridPos.ofBlock(pos), type, amount);
    }

    public static void setPollution(Level level, int x, int y, int z, PollutionType type, float amount) {
        setPollution(level, new BlockPos(x, y, z), type, amount);
    }

    public static void setPollution(Level level, PollutionGridPos pos, PollutionType type, float amount) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel) || pos == null || type == null) {
            return;
        }
        getData(serverLevel).set(pos, type, amount);
    }

    public static float getPollution(Level level, BlockPos pos, PollutionType type) {
        if (pos == null) {
            return 0.0F;
        }
        return getPollution(level, PollutionGridPos.ofBlock(pos), type);
    }

    public static float getPollution(Level level, PollutionGridPos pos, PollutionType type) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel) || pos == null || type == null) {
            return 0.0F;
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? 0.0F : data.get(pos, type);
    }

    public static float getPollution(Level level, int x, int y, int z, PollutionType type) {
        return getPollution(level, new BlockPos(x, y, z), type);
    }

    public static PollutionSample getPollutionData(Level level, BlockPos pos) {
        PollutionSample sample = getPollutionDataOrNull(level, pos);
        return sample == null ? new PollutionSample() : sample;
    }

    public static PollutionSample getPollutionData(Level level, PollutionGridPos pos) {
        PollutionSample sample = getPollutionDataOrNull(level, pos);
        return sample == null ? new PollutionSample() : sample;
    }

    public static PollutionSample getPollutionData(Level level, int x, int y, int z) {
        return getPollutionData(level, new BlockPos(x, y, z));
    }

    public static PollutionSample getPollutionDataAtEntityEyes(LivingEntity entity) {
        PollutionSample sample = getPollutionDataOrNullAtEntityEyes(entity);
        return sample == null ? new PollutionSample() : sample;
    }

    public static float getPollutionAtEntityEyes(LivingEntity entity, PollutionType type) {
        PollutionSample sample = getPollutionDataOrNullAtEntityEyes(entity);
        return sample == null ? 0.0F : sample.get(type);
    }

    public static void setPollutionData(Level level, BlockPos pos, PollutionSample sample) {
        if (pos == null) {
            return;
        }
        setPollutionData(level, PollutionGridPos.ofBlock(pos), sample);
    }

    public static void setPollutionData(Level level, int x, int y, int z, PollutionSample sample) {
        setPollutionData(level, new BlockPos(x, y, z), sample);
    }

    public static void setPollutionData(Level level, PollutionGridPos pos, PollutionSample sample) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel) || pos == null) {
            return;
        }
        getData(serverLevel).set(pos, sample);
    }

    public static void updatePollutionData(Level level, BlockPos pos, Consumer<PollutionSample> updater) {
        if (pos == null) {
            return;
        }
        updatePollutionData(level, PollutionGridPos.ofBlock(pos), updater);
    }

    public static void updatePollutionData(Level level, PollutionGridPos pos, Consumer<PollutionSample> updater) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel) || pos == null || updater == null) {
            return;
        }
        PollutionSavedData data = getData(serverLevel);
        PollutionSample sample = data.get(pos);
        updater.accept(sample);
        data.set(pos, sample);
    }

    public static void updatePollutionData(Level level, int x, int y, int z, Consumer<PollutionSample> updater) {
        updatePollutionData(level, new BlockPos(x, y, z), updater);
    }

    public static Map<PollutionGridPos, PollutionSample> pollutionSnapshot(Level level) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return Map.of();
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? Map.of() : data.pollutionSnapshot();
    }

    public static List<PollutionSavedData.EntrySnapshot> pollutionEntriesSnapshot(Level level) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return List.of();
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? List.of() : data.gridEntriesSnapshot();
    }

    public static List<PollutionSavedData.EntrySnapshot> positivePollutionEntriesSnapshot(Level level, int limit) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return List.of();
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? List.of() : data.positiveEntriesSnapshot(limit);
    }

    public static List<PollutionSavedData.EntrySnapshot> positivePollutionEntriesSnapshot(Level level,
                                                                                         PollutionType type,
                                                                                         int limit) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel) || type == null) {
            return List.of();
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? List.of() : data.positiveEntriesSnapshot(type, limit);
    }

    public static PollutionSavedData.DiffusionPreview previewDiffusion(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return PollutionSavedData.DiffusionPreview.empty();
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null
                ? PollutionSavedData.DiffusionPreview.empty()
                : data.previewDiffusion(pos -> isPollutionGridLoaded(serverLevel, pos));
    }

    public static List<PollutionSavedData.EntrySnapshot> positiveDiffusionPreviewEntries(Level level, int limit) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return List.of();
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? List.of() : data.positiveDiffusionPreviewEntries(limit);
    }

    public static List<PollutionSavedData.EntrySnapshot> positiveDiffusionPreviewEntries(Level level,
                                                                                        PollutionType type,
                                                                                        int limit) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel) || type == null) {
            return List.of();
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? List.of() : data.positiveDiffusionPreviewEntries(type, limit);
    }

    public static List<PollutionSavedData.EntryDelta> diffusionDeltaEntries(Level level, int limit) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return List.of();
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? List.of() : data.diffusionDeltaEntries(limit);
    }

    public static List<PollutionSavedData.EntryDelta> diffusionDeltaEntries(Level level, PollutionType type,
                                                                            int limit) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel) || type == null) {
            return List.of();
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? List.of() : data.diffusionDeltaEntries(type, limit);
    }

    public static PollutionSavedData.EntryDelta diffusionDeltaAt(Level level, PollutionGridPos pos) {
        PollutionGridPos safePos = pos == null ? new PollutionGridPos(0, 0) : pos;
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return PollutionSavedData.EntryDelta.of(safePos, new PollutionSavedData.PollutionSample(),
                    new PollutionSavedData.PollutionSample());
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null
                ? PollutionSavedData.EntryDelta.of(safePos, new PollutionSavedData.PollutionSample(),
                        new PollutionSavedData.PollutionSample())
                : data.diffusionDeltaAt(safePos);
    }

    public static List<PollutionSavedData.EntryDelta> diffusionNeighborDeltas(Level level, PollutionGridPos center) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel) || center == null) {
            return List.of();
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? List.of() : data.diffusionNeighborDeltas(center);
    }

    public static void setPollutionData(Level level, Map<PollutionGridPos, PollutionSample> values) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        getData(serverLevel).replaceAll(values);
    }

    public static void addPollutionData(Level level, Map<PollutionGridPos, PollutionSample> amounts) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        getData(serverLevel).addClamped(amounts);
    }

    @Nullable
    public static PollutionSample getPollutionDataOrNull(Level level, BlockPos pos) {
        if (pos == null) {
            return null;
        }
        return getPollutionDataOrNull(level, PollutionGridPos.ofBlock(pos));
    }

    @Nullable
    public static PollutionSample getPollutionDataOrNull(Level level, PollutionGridPos pos) {
        if (!isEnabled() || !(level instanceof ServerLevel serverLevel) || pos == null) {
            return null;
        }
        PollutionSavedData data = getExistingData(serverLevel);
        return data == null ? null : data.getOrNull(pos);
    }

    @Nullable
    public static PollutionSample getPollutionDataOrNull(Level level, int x, int y, int z) {
        return getPollutionDataOrNull(level, new BlockPos(x, y, z));
    }

    @Nullable
    public static PollutionSample getPollutionDataOrNullAtEntityEyes(LivingEntity entity) {
        if (entity == null) {
            return null;
        }
        BlockPos pos = BlockPos.containing(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
        return getPollutionDataOrNull(entity.level(), pos);
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
            return PollutionSavedData.Stats.empty();
        }
        PollutionSavedData data = getExistingData(serverLevel);
        if (data == null) {
            return PollutionSavedData.Stats.empty();
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

    public static boolean isPollutionGridLoaded(Level level, PollutionGridPos pos) {
        return level instanceof ServerLevel serverLevel && pos != null && isPollutionGridLoaded(serverLevel, pos);
    }

    public static void tick(ServerLevel level) {
        tick(List.of(level));
    }

    public static void tick(Iterable<ServerLevel> levels) {
        if (!isEnabled()) {
            return;
        }

        diffusionTimer++;
        boolean updateDiffusion = diffusionTimer >= DIFFUSION_INTERVAL_TICKS;
        if (updateDiffusion) {
            diffusionTimer = 0;
        }

        for (ServerLevel level : levels) {
            handleWorldDestruction(level);
            if (updateDiffusion) {
                PollutionSavedData data = getExistingData(level);
                if (data != null) {
                    data.updateDiffusion();
                }
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
        if (RadiationConfig.mobGearEnabled()
                && mob instanceof Zombie zombie
                && !zombie.isBaby()
                && soot > 2.0F
                && mob.getRandom().nextFloat() < 0.005F) {
            HbmMobEquipmentUtil.equipFullSet(zombie,
                    ModItems.HAZMAT_HELMET.get(),
                    ModItems.HAZMAT_PLATE.get(),
                    ModItems.HAZMAT_LEGS.get(),
                    ModItems.HAZMAT_BOOTS.get());
        }
        if (soot <= RadiationConfig.pollutionBuffMobThreshold()) {
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

    public static boolean applyLeadFromBlockBreak(Player player, Level level, BlockPos pos) {
        if (!isEnabled()
                || !RadiationConfig.pollutionLeadFromBlocksEnabled()
                || player == null
                || pos == null
                || ArmorUtil.hasPollutionLeadProtection(player)) {
            return false;
        }

        float metal = getPollution(level, pos, PollutionType.HEAVYMETAL);
        if (metal < 5.0F) {
            return false;
        }

        int amplifier = metal < 10.0F ? 0 : metal < 25.0F ? 1 : 2;
        player.addEffect(new MobEffectInstance(ModEffects.LEAD.get(), 100, amplifier));
        return true;
    }

    public static void applyEntityPollutionEffects(LivingEntity entity) {
        if (!isEnabled() || entity == null || entity.level().isClientSide || entity.tickCount % 60 != 0) {
            return;
        }

        PollutionSample sample = getPollutionDataAtEntityEyes(entity);
        applyPoisonEffect(entity, sample);
        applyHeavyMetalEffect(entity, sample);
    }

    public static double getSootLungLoad(LivingEntity entity) {
        if (!(entity instanceof Player) || !isEnabled() || ArmorUtil.hasSootLungProtection(entity)) {
            return 0.0D;
        }
        return getPollutionAtEntityEyes(entity, PollutionType.SOOT);
    }

    public static boolean shouldGlyphidUseExtendedTargeting(Level level, BlockPos pos) {
        if (RadiationConfig.rampantExtendedTargetingEnabled()) {
            return true;
        }
        return getPollution(level, pos, PollutionType.SOOT) >= RadiationConfig.glyphidTargetingThreshold();
    }

    public static boolean canTryRampantScoutSpawn(Level level, BlockPos pos) {
        return isEnabled()
                && level instanceof ServerLevel serverLevel
                && pos != null
                && serverLevel.dimension().equals(Level.OVERWORLD)
                && RadiationConfig.rampantNaturalScoutSpawnEnabled()
                && serverLevel.canSeeSky(pos)
                && getPollution(serverLevel, pos, PollutionType.SOOT) >= RadiationConfig.rampantScoutSpawnThreshold()
                && serverLevel.random.nextInt(RadiationConfig.rampantScoutSpawnChance()) == 0;
    }

    private static void applyPoisonEffect(LivingEntity entity, PollutionSample sample) {
        if (!RadiationConfig.pollutionPoisonEnabled() || ArmorUtil.hasPollutionPoisonProtection(entity)) {
            return;
        }
        float poison = sample.get(PollutionType.POISON);
        if (poison <= 10.0F) {
            return;
        }
        if (poison < 25.0F) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        } else if (poison < 50.0F) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
        } else {
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2));
        }
    }

    private static void applyHeavyMetalEffect(LivingEntity entity, PollutionSample sample) {
        if (!RadiationConfig.pollutionLeadPoisoningEnabled() || ArmorUtil.hasPollutionLeadProtection(entity)) {
            return;
        }
        float metal = sample.get(PollutionType.HEAVYMETAL);
        if (metal > 25.0F) {
            int amplifier = metal < 50.0F ? 0 : 2;
            entity.addEffect(new MobEffectInstance(ModEffects.LEAD.get(), 100, amplifier));
        }
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
                int x = pos.randomBlockX(level.random);
                int z = pos.randomBlockZ(level.random);
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
        for (int chunkX = pos.minChunkX(); chunkX <= pos.maxChunkX(); chunkX++) {
            for (int chunkZ = pos.minChunkZ(); chunkZ <= pos.maxChunkZ(); chunkZ++) {
                if (level.hasChunk(chunkX, chunkZ)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isEnabled() {
        return RadiationConfig.pollutionEnabled();
    }

    private static boolean hasAnyFiniteNonZero(@Nullable PollutionSample sample) {
        if (sample == null) {
            return false;
        }
        for (PollutionType type : PollutionType.orderedValues()) {
            float value = sample.get(type);
            if (Float.isFinite(value) && value != 0.0F) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAnyNegative(PollutionSample sample) {
        for (PollutionType type : PollutionType.orderedValues()) {
            float value = sample.get(type);
            if (Float.isFinite(value) && value < 0.0F) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasPollutionDebtsAvailable(PollutionSavedData data, PollutionGridPos pos,
                                                      PollutionSample amounts) {
        PollutionSample current = data.get(pos);
        for (PollutionType type : PollutionType.orderedValues()) {
            float value = amounts.get(type);
            if (Float.isFinite(value) && value < 0.0F && !current.hasAtLeast(type, -value)) {
                return false;
            }
        }
        return true;
    }

    private static PollutionSample scaledFinite(PollutionSample sample, float multiplier) {
        PollutionSample scaled = new PollutionSample();
        for (PollutionType type : PollutionType.orderedValues()) {
            float value = sample.get(type);
            if (Float.isFinite(value) && value != 0.0F) {
                scaled.set(type, value * multiplier);
            }
        }
        return scaled;
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
        PollutionSavedData primary = WorldSavedDataHelper.getExisting(level, PollutionSavedData.DATA_NAME,
                PollutionSavedData::load).orElse(null);
        PollutionSavedData fallback = WorldSavedDataHelper.getExisting(level, PollutionSavedData.MODERN_COMPAT_DATA_NAME,
                PollutionSavedData::load).orElse(null);

        if (primary != null && !primary.isEmpty()) {
            return primary;
        }
        if (fallback != null && !fallback.isEmpty()) {
            fallback.setDirty();
            level.getDataStorage().set(PollutionSavedData.DATA_NAME, fallback);
            return fallback;
        }

        PollutionSavedData data = primary == null ? fallback : primary;
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
