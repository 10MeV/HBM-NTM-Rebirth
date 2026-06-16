package com.hbm.ntm.bullet;

import com.hbm.ntm.block.ShotDetonatableBlock;
import com.hbm.ntm.compat.CompatExternal;
import com.hbm.ntm.damage.DamageClass;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.entity.effect.EmpBlastEntity;
import com.hbm.ntm.entity.effect.FireLingeringEntity;
import com.hbm.ntm.entity.logic.NukeExplosionMk5Entity;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.explosion.NuclearExplosionUtil;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorBulkie;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorStandard;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorBalefire;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorDebris;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorFire;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorCrossSmooth;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectStandard;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectWeapon;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.fluid.HbmExtinguishType;
import com.hbm.ntm.fluid.HbmFluidRepairable;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BulletImpactUtil {
    public static BlockImpactResult applyBlockImpactEffects(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, @Nullable BlockPos hitBlock) {
        return applyBlockImpactEffects(config, level, position, source, hitBlock, false);
    }

    public static BlockImpactResult applyBlockImpactEffects(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, @Nullable BlockPos hitBlock, boolean inGround) {
        return applyBlockImpactEffects(config, level, position, source, hitBlock, null, inGround);
    }

    public static BlockImpactResult applyBlockImpactEffects(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, @Nullable BlockPos hitBlock, @Nullable Direction hitSide, boolean inGround) {
        return applyBlockImpactEffects(config, level, position, source, hitBlock, hitSide, inGround, 0.0F);
    }

    public static BlockImpactResult applyBlockImpactEffects(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, @Nullable BlockPos hitBlock, @Nullable Direction hitSide, boolean inGround,
            float impactDamage) {
        if (config == null || level == null || position == null) {
            return BlockImpactResult.NONE;
        }

        boolean discard = shouldDiscardAfterBlockImpact(config, hitBlock != null, inGround);
        if (level.isClientSide()) {
            return new BlockImpactResult(discard, false, false, false, false, false, false, false, false, false, false,
                    false, Collections.emptyList());
        }

        boolean fire = applyIncendiaryBlocks(config, level, position);
        boolean extinguish = applyFireExtinguishBlocks(config, level, hitBlock, position);
        boolean extinguisherRepair = applyFireExtinguisherRepair(config, level, hitBlock);
        boolean extinguisherPlacement = !extinguisherRepair
                && applyFireExtinguisherPlacement(config, level, hitBlock, hitSide, position);
        boolean emp = applyEmp(config, level, position);
        boolean jolt = applyJolt(config, level, position);
        boolean explosion = applyExplosion(config, level, position, source, hitSide,
                impactDamage(config, impactDamage));
        boolean shrapnel = applyShrapnel(config, level, position, source);
        boolean rainbow = applyRainbow(config, level, position);
        boolean nuke = applyNuke(config, level, position);
        boolean flameBlockIgnited = applyFlameBlockImpact(config, level, hitBlock, hitSide);
        fire |= flameBlockIgnited;
        boolean lingeringFire = applyLegacyExplosiveLingeringFire(config, level, position);
        lingeringFire |= applyLegacyFlamerLingeringFire(config, level, position, hitBlock, flameBlockIgnited);
        lingeringFire |= applyLegacyBeamLingeringFire(config, level, position, hitBlock, flameBlockIgnited);
        boolean specialBehavior = applyTaggedImpactEffects(config, level, position, source, hitSide,
                impactDamage(config, impactDamage));
        List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests =
                BulletSpecialSpawnUtil.collectImpactSpawnRequests(config, level, source, position, null, hitSide,
                        impactDamage(config, impactDamage));
        BlockBreakResult blockBreak = applyBlockBreak(config, level, hitBlock, source);
        return new BlockImpactResult(discard, fire || lingeringFire, emp, jolt, explosion, shrapnel, rainbow, nuke,
                specialBehavior || lingeringFire || extinguish || extinguisherRepair || extinguisherPlacement,
                blockBreak.destroyedBlock() || blockBreak.brokeGlass() || blockBreak.shotDetonated(),
                blockBreak.shotDetonated(), extinguish, spawnRequests);
    }

    public static EntityImpactResult applyEntityImpactEffects(BulletConfig config, Entity target,
            @Nullable Entity source, @Nullable Vec3 position) {
        return applyEntityImpactEffects(config, target, source, position, null);
    }

    public static EntityImpactResult applyEntityImpactEffects(BulletConfig config, Entity target,
            @Nullable Entity source, @Nullable Vec3 position, @Nullable RandomSource random) {
        return applyEntityImpactEffects(config, target, source, position, random, 0.0F);
    }

    public static EntityImpactResult applyEntityImpactEffects(BulletConfig config, Entity target,
            @Nullable Entity source, @Nullable Vec3 position, @Nullable RandomSource random, float impactDamage) {
        if (config == null || target == null) {
            return EntityImpactResult.NONE;
        }
        EntityHurtResult hurt = applyEntityHurtEffects(config, target, random);
        Vec3 impact = position == null ? target.position() : position;
        BlockImpactResult block = applyBlockImpactEffects(config, target.level(), impact, source, null, null,
                false, impactDamage);
        List<BulletSpecialSpawnUtil.SpawnRequest> splitSubBeams =
                BulletSpecialSpawnUtil.collectLightningSplitSubBeams(config, target.level(), source, target, impact,
                        random, impactDamage(config, impactDamage));
        block = withAdditionalSpawnRequests(block, splitSubBeams);
        return new EntityImpactResult(block.discardProjectile(), hurt, block);
    }

    private static BlockImpactResult withAdditionalSpawnRequests(BlockImpactResult result,
            List<BulletSpecialSpawnUtil.SpawnRequest> additional) {
        if (result == null) {
            return BlockImpactResult.NONE;
        }
        if (additional == null || additional.isEmpty()) {
            return result;
        }
        List<BulletSpecialSpawnUtil.SpawnRequest> merged =
                new ArrayList<>(result.spawnRequests().size() + additional.size());
        merged.addAll(result.spawnRequests());
        merged.addAll(additional);
        return new BlockImpactResult(result.discardProjectile(), result.placedFire(), result.emp(), result.jolt(),
                result.explosion(), result.shrapnel(), result.rainbow(), result.nuke(), result.specialBehavior(),
                result.brokeOrDestroyedBlock(), result.shotDetonated(), result.extinguishedFire(),
                Collections.unmodifiableList(merged));
    }

    public static EntityHurtResult applyEntityHurtEffects(BulletConfig config, Entity target) {
        return applyEntityHurtEffects(config, target, null);
    }

    public static EntityHurtResult applyEntityHurtEffects(BulletConfig config, Entity target, @Nullable RandomSource random) {
        if (config == null || target == null || target.level().isClientSide()) {
            return EntityHurtResult.NONE;
        }

        boolean fire = false;
        boolean lead = false;
        boolean instakill = false;
        boolean caustic = false;
        boolean extinguished = false;
        int customEffects = 0;
        if (config.hasBehavior(BulletBehaviorTag.FIRE_EXTINGUISH_ENTITY)) {
            extinguished = applyEntityExtinguish(target);
        }
        if (config.hasBehavior(BulletBehaviorTag.ENTITY_IGNITE) && target instanceof LivingEntity living
                && !skipsLegacyEntityIgnite(config)) {
            applyEntityIgnite(config, living);
            fire = true;
        }
        if (config.incendiaryTicks() > 0 && !hasAnyBehavior(config,
                BulletBehaviorTag.INCENDIARY_EXPLODE,
                BulletBehaviorTag.PHOSPHORUS_EXPLODE)) {
            target.setSecondsOnFire(config.incendiaryTicks());
            fire = true;
        }
        if (config.hasBehavior(BulletBehaviorTag.INCENDIARY_PHOSPHORUS) && target instanceof LivingEntity living) {
            HbmLivingProperties.ensurePhosphorus(living, 300);
            fire = true;
        }
        if (config.hasBehavior(BulletBehaviorTag.INFRARED_BEAM_HIT) && target instanceof LivingEntity living) {
            HbmLivingProperties.ensureFire(living, 100);
            fire = true;
        }
        if (config.hasBehavior(BulletBehaviorTag.BLACK_FIRE_BEAM_HIT) && target instanceof LivingEntity living) {
            HbmLivingProperties.addBlackFire(living, 200);
            fire = true;
        }
        if (hasAnyBehavior(config, BulletBehaviorTag.LIGHTNING_BEAM_HIT,
                BulletBehaviorTag.LIGHTNING_BEAM_SPLIT) && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9));
            living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 9));
            customEffects += 2;
        }

        RandomSource roll = random == null ? target.level().random : random;
        if (config.leadChance() > 0 && target instanceof LivingEntity living
                && roll.nextInt(100) < config.leadChance()) {
            living.addEffect(new MobEffectInstance(ModEffects.LEAD.get(), 10 * 20, 0));
            lead = true;
        }

        if (config.instakill() && target instanceof LivingEntity living
                && !(target instanceof Player player && player.isCreative())) {
            living.setHealth(0.0F);
            instakill = true;
        }

        if (config.caustic() > 0 && target instanceof Player player) {
            ArmorUtil.damageSuitAll(player, config.caustic());
            caustic = true;
        }

        if (!config.effects().isEmpty() && target instanceof LivingEntity living) {
            for (MobEffectInstance effect : config.effects()) {
                living.addEffect(new MobEffectInstance(effect));
                customEffects++;
            }
        }

        return new EntityHurtResult(fire, lead, instakill, caustic, extinguished, customEffects);
    }

    public static boolean shouldDiscardAfterBlockImpact(BulletConfig config, boolean hasBlockHit, boolean inGround) {
        if (config == null) {
            return false;
        }
        if (hasBlockHit && config.hasBehavior(BulletBehaviorTag.CHARGE_HOOK_STICK)) {
            return false;
        }
        if (hasBlockHit) {
            return !config.liveAfterImpact() && !config.spectral() && !inGround;
        }
        return !config.penetrates();
    }

    public static boolean shouldDiscardAfterEntityImpact(BulletConfig config) {
        return config != null && !config.penetrates();
    }

    private static boolean applyIncendiaryBlocks(BulletConfig config, Level level, Vec3 position) {
        if (config.incendiaryTicks() <= 0 || hasAnyBehavior(config,
                BulletBehaviorTag.INCENDIARY_EXPLODE,
                BulletBehaviorTag.PHOSPHORUS_EXPLODE)) {
            return false;
        }
        BlockPos base = legacyCastPos(position.x, position.y, position.z);
        boolean placed = false;
        placed |= maybeSetFire(level, base);
        placed |= maybeSetFire(level, base.east());
        placed |= maybeSetFire(level, base.west());
        placed |= maybeSetFire(level, base.above());
        placed |= maybeSetFire(level, base.below());
        placed |= maybeSetFire(level, base.south());
        placed |= maybeSetFire(level, base.north());
        return placed;
    }

    private static boolean applyLegacyExplosiveLingeringFire(BulletConfig config, Level level, Vec3 position) {
        if (!hasAnyBehavior(config, BulletBehaviorTag.INCENDIARY_EXPLODE,
                BulletBehaviorTag.PHOSPHORUS_EXPLODE)) {
            return false;
        }
        LegacyLingeringFire fire = legacyLingeringFire(config);
        if (fire == null) {
            return false;
        }
        FireLingeringEntity entity = FireLingeringEntity.create(level, position.x, position.y, position.z,
                fire.fireType(), fire.width(), fire.height(), fire.duration());
        boolean spawned = level.addFreshEntity(entity);
        boolean placed = placeLegacyLingeringFireBlocks(level, position, fire.blockRadius());
        return spawned || placed;
    }

    @Nullable
    private static LegacyLingeringFire legacyLingeringFire(BulletConfig config) {
        boolean phosphorus = config.hasBehavior(BulletBehaviorTag.PHOSPHORUS_EXPLODE);
        String legacyName = config.legacyName();
        if (legacyName.startsWith("g40_")) {
            return new LegacyLingeringFire(5.0F, 2.0F, phosphorus ? 400 : 200, 1,
                    phosphorus ? FireLingeringEntity.TYPE_PHOSPHORUS : FireLingeringEntity.TYPE_DIESEL);
        }
        if (legacyName.startsWith("rocket_")) {
            return new LegacyLingeringFire(6.0F, 2.0F, phosphorus ? 600 : 300, 2,
                    phosphorus ? FireLingeringEntity.TYPE_PHOSPHORUS : FireLingeringEntity.TYPE_DIESEL);
        }
        return null;
    }

    private static boolean applyLegacyFlamerLingeringFire(BulletConfig config, Level level, Vec3 position,
            @Nullable BlockPos hitBlock, boolean flameBlockIgnited) {
        if (hitBlock == null) {
            return false;
        }
        LegacyLingeringFire fire = legacyFlamerLingeringFire(config, flameBlockIgnited);
        if (fire == null) {
            return false;
        }
        FireLingeringEntity entity = FireLingeringEntity.create(level, position.x, position.y, position.z,
                fire.fireType(), fire.width(), fire.height(), fire.duration());
        AABB duplicateCheck = new AABB(position, position)
                .inflate(fire.width() / 2.0D + 0.5D, fire.height() / 2.0D + 0.5D,
                        fire.width() / 2.0D + 0.5D);
        if (!level.getEntitiesOfClass(FireLingeringEntity.class, duplicateCheck).isEmpty()) {
            return true;
        }
        return level.addFreshEntity(entity);
    }

    @Nullable
    private static LegacyLingeringFire legacyFlamerLingeringFire(BulletConfig config, boolean flameBlockIgnited) {
        String legacyName = config.legacyName();
        if (legacyName.contains("_gas")) {
            return null;
        }
        if (legacyName.startsWith("flame_daybreaker_diesel")) {
            return new LegacyLingeringFire(6.0F, 2.0F, 200, 0, FireLingeringEntity.TYPE_DIESEL);
        }
        if (legacyName.startsWith("flame_daybreaker_napalm")) {
            return new LegacyLingeringFire(6.0F, 2.0F, 300, 0, FireLingeringEntity.TYPE_DIESEL);
        }
        if (legacyName.startsWith("flame_daybreaker_balefire")) {
            return new LegacyLingeringFire(7.5F, 2.5F, 400, 0, FireLingeringEntity.TYPE_BALEFIRE);
        }
        if (config.hasBehavior(BulletBehaviorTag.FLAME_LINGER_BALEFIRE)) {
            return new LegacyLingeringFire(3.0F, 1.0F, 300, 0, FireLingeringEntity.TYPE_BALEFIRE);
        }
        if (flameBlockIgnited) {
            return null;
        }
        if (config.hasBehavior(BulletBehaviorTag.FLAME_LINGER_DIESEL)) {
            return new LegacyLingeringFire(2.0F, 1.0F, 100, 0, FireLingeringEntity.TYPE_DIESEL);
        }
        if (config.hasBehavior(BulletBehaviorTag.FLAME_LINGER_NAPALM)) {
            return new LegacyLingeringFire(2.5F, 1.0F, 200, 0, FireLingeringEntity.TYPE_DIESEL);
        }
        return null;
    }

    private static boolean applyLegacyBeamLingeringFire(BulletConfig config, Level level, Vec3 position,
            @Nullable BlockPos hitBlock, boolean flameBlockIgnited) {
        if (hitBlock == null) {
            return false;
        }
        LegacyLingeringFire fire = legacyBeamLingeringFire(config, flameBlockIgnited);
        if (fire == null) {
            return false;
        }
        FireLingeringEntity entity = FireLingeringEntity.create(level, position.x, position.y, position.z,
                fire.fireType(), fire.width(), fire.height(), fire.duration());
        return level.addFreshEntity(entity);
    }

    @Nullable
    private static LegacyLingeringFire legacyBeamLingeringFire(BulletConfig config, boolean flameBlockIgnited) {
        if (config.hasBehavior(BulletBehaviorTag.BLACK_FIRE_BEAM_HIT)) {
            return new LegacyLingeringFire(7.5F, 2.0F, 200, 0, FireLingeringEntity.TYPE_BLACK);
        }
        if (config.hasBehavior(BulletBehaviorTag.INFRARED_BEAM_HIT) && !flameBlockIgnited) {
            return new LegacyLingeringFire(2.0F, 1.0F, 100, 0, FireLingeringEntity.TYPE_DIESEL);
        }
        return null;
    }

    private static boolean placeLegacyLingeringFireBlocks(Level level, Vec3 position, int radius) {
        BlockPos center = legacyFloorPos(position.x, position.y, position.z);
        boolean placed = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            if (!level.getBlockState(pos).isAir()) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.relative(direction);
                if (level.getBlockState(neighbor).isFlammable(level, neighbor, direction.getOpposite())) {
                    BlockState fire = BaseFireBlock.getState(level, pos);
                    if (fire.canSurvive(level, pos) && level.setBlock(pos, fire, 3)) {
                        placed = true;
                    }
                    break;
                }
            }
        }
        return placed;
    }

    private static boolean maybeSetFire(Level level, BlockPos pos) {
        if (level.random.nextInt(3) != 0 || !level.getBlockState(pos).isAir()) {
            return false;
        }
        return level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
    }

    private static boolean applyFlameBlockImpact(BulletConfig config, Level level, @Nullable BlockPos hitBlock,
            @Nullable Direction hitSide) {
        if (hitBlock == null || hitSide == null || !hasAnyBehavior(config,
                BulletBehaviorTag.FLAME_LINGER_DIESEL,
                BulletBehaviorTag.FLAME_LINGER_GAS,
                BulletBehaviorTag.FLAME_LINGER_NAPALM,
                BulletBehaviorTag.INFRARED_BEAM_HIT)) {
            return false;
        }
        boolean legacyFlamer = hasAnyBehavior(config,
                BulletBehaviorTag.FLAME_LINGER_DIESEL,
                BulletBehaviorTag.FLAME_LINGER_GAS,
                BulletBehaviorTag.FLAME_LINGER_NAPALM,
                BulletBehaviorTag.FLAME_LINGER_BALEFIRE);
        if (legacyFlamer && config.legacyName().startsWith("flame_daybreaker_")) {
            return false;
        }
        BlockState hitState = level.getBlockState(hitBlock);
        Direction flammableSide = legacyFlamer ? hitSide.getOpposite() : hitSide;
        if (!hitState.isFlammable(level, hitBlock, flammableSide)) {
            return false;
        }
        BlockPos firePos = hitBlock.relative(hitSide);
        if (!level.getBlockState(firePos).isAir()) {
            return false;
        }
        BlockState fireState = BaseFireBlock.getState(level, firePos);
        if (!fireState.canSurvive(level, firePos)) {
            return false;
        }
        boolean placed = level.setBlock(firePos, fireState, 3);
        if (placed && !legacyFlamer) {
            LegacySoundPlayer.playLegacyFlamethrowerIgnite(level, firePos, 0.75F, 0.9F, 0.2F);
        }
        return placed;
    }

    private static boolean skipsLegacyEntityIgnite(BulletConfig config) {
        return config.legacyName().startsWith("flame_daybreaker_");
    }

    private static boolean applyEntityExtinguish(Entity target) {
        boolean changed = target.getRemainingFireTicks() > 0;
        target.clearFire();
        if (target instanceof LivingEntity living) {
            changed |= HbmLivingProperties.hasTemperatureEffects(living);
            HbmLivingProperties.clearTemperatureEffects(living);
        }
        return changed;
    }

    private static boolean applyFireExtinguisherRepair(BulletConfig config, Level level, @Nullable BlockPos hitBlock) {
        HbmExtinguishType type = extinguisherRepairType(config);
        if (type == null || hitBlock == null) {
            return false;
        }
        BlockEntity core = CompatExternal.getCoreFromPos(level, hitBlock);
        if (!(core instanceof HbmFluidRepairable repairable)) {
            return false;
        }
        repairable.tryExtinguish(type);
        return true;
    }

    @Nullable
    private static HbmExtinguishType extinguisherRepairType(BulletConfig config) {
        if (config.hasBehavior(BulletBehaviorTag.FIRE_EXTINGUISH_WATER)) {
            return HbmExtinguishType.WATER;
        }
        if (config.hasBehavior(BulletBehaviorTag.FIRE_EXTINGUISH_FOAM)) {
            return HbmExtinguishType.FOAM;
        }
        if (config.hasBehavior(BulletBehaviorTag.FIRE_EXTINGUISH_SAND)) {
            return HbmExtinguishType.SAND;
        }
        return null;
    }

    private static void applyEntityIgnite(BulletConfig config, LivingEntity target) {
        if (config.hasBehavior(BulletBehaviorTag.BALEFIRE_VISUAL)
                || config.hasBehavior(BulletBehaviorTag.FLAME_LINGER_BALEFIRE)) {
            HbmLivingProperties.ensureBalefire(target, 200);
            return;
        }
        if (isLegacyG26Flare(config)) {
            HbmLivingProperties.addFire(target, 200);
            return;
        }
        HbmLivingProperties.ensureFire(target, hasAnyBehavior(config,
                BulletBehaviorTag.FLAME_LINGER_DIESEL,
                BulletBehaviorTag.FLAME_LINGER_GAS,
                BulletBehaviorTag.FLAME_LINGER_NAPALM) ? 100 : 200);
    }

    private static boolean isLegacyG26Flare(BulletConfig config) {
        return config.legacyName().startsWith("g26_flare");
    }

    private static boolean applyFireExtinguishBlocks(BulletConfig config, Level level, @Nullable BlockPos hitBlock,
            Vec3 position) {
        if (!hasAnyBehavior(config, BulletBehaviorTag.FIRE_EXTINGUISH_WATER,
                BulletBehaviorTag.FIRE_EXTINGUISH_FOAM, BulletBehaviorTag.FIRE_EXTINGUISH_SAND)) {
            return false;
        }
        BlockPos center = hitBlock == null ? legacyFloorPos(position.x, position.y, position.z) : hitBlock;
        boolean changed = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            BlockState state = level.getBlockState(pos);
            if (isExtinguishableFire(state)
                    || (config.hasBehavior(BulletBehaviorTag.FIRE_EXTINGUISH_WATER) && isLegacyFoam(state))) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                changed = true;
            }
        }
        if (changed) {
            level.playSound(null, center, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                    1.0F, 1.5F + level.random.nextFloat() * 0.5F);
        }
        return changed;
    }

    private static boolean applyFireExtinguisherPlacement(BulletConfig config, Level level,
            @Nullable BlockPos hitBlock, @Nullable Direction hitSide, Vec3 position) {
        if (!hasAnyBehavior(config, BulletBehaviorTag.FIRE_EXTINGUISH_FOAM,
                BulletBehaviorTag.FIRE_EXTINGUISH_SAND)) {
            return false;
        }
        BlockPos target = hitBlock == null ? legacyFloorPos(position.x, position.y, position.z) : hitBlock;
        if (hitSide != null && level.random.nextBoolean()) {
            target = target.relative(hitSide);
        }
        BlockState placedState = config.hasBehavior(BulletBehaviorTag.FIRE_EXTINGUISH_FOAM)
                ? ModBlocks.legacyBlock("block_foam").get().defaultBlockState()
                : ModBlocks.SAND_BORON.get().defaultBlockState();
        BlockState currentState = level.getBlockState(target);
        if (!currentState.canBeReplaced() || !placedState.canSurvive(level, target)) {
            return false;
        }
        return level.setBlock(target, placedState, 3);
    }

    private static boolean isExtinguishableFire(BlockState state) {
        return state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(ModBlocks.FIRE_DIGAMMA.get())
                || state.is(ModBlocks.BALEFIRE.get());
    }

    private static boolean isLegacyFoam(BlockState state) {
        return "block_foam".equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
    }

    private static boolean applyEmp(BulletConfig config, Level level, Vec3 position) {
        if (config.emp() <= 0) {
            return false;
        }
        ExplosionNukeGeneric.empBlast(level, legacyRoundPos(position.x), legacyRoundPos(position.y),
                legacyRoundPos(position.z), config.emp());
        if (config.emp() > 3) {
            level.addFreshEntity(EmpBlastEntity.create(level, position.x, position.y + 0.5D, position.z,
                    config.emp()));
        }
        return true;
    }

    private static boolean applyJolt(BulletConfig config, Level level, Vec3 position) {
        if (config.jolt() <= 0.0D) {
            return false;
        }
        ExplosionLarge.jolt(level, position.x, position.y, position.z, config.jolt(), 150, 0.25D);
        return true;
    }

    private static boolean applyExplosion(BulletConfig config, Level level, Vec3 position, @Nullable Entity source,
            @Nullable Direction hitSide, float impactDamage) {
        if (applyMiniNukeImpact(config, level, position, impactDamage)) {
            return true;
        }
        if (applyLegacySpecialVntImpact(config, level, position, source, impactDamage)) {
            return true;
        }
        if (config.explosive() <= 0.0F) {
            return false;
        }
        if (hasAnyBehavior(config, BulletBehaviorTag.TINY_EXPLODE,
                BulletBehaviorTag.GRENADE_TINY_EXPLOSIVE_PELLET)) {
            Vec3 explosionPosition = tinyExplosionPosition(position, hitSide);
            WeaponExplosionUtil.explodeTinySmooth(level, explosionPosition.x, explosionPosition.y, explosionPosition.z,
                    config.explosive(), source, Math.max(1.0F, impactDamage), 0.5D,
                    config.armorThresholdNegation(), config.armorPiercingPercent(), 0.25D);
            return true;
        }
        if (hasAnyBehavior(config, BulletBehaviorTag.DEMO_EXPLODE,
                BulletBehaviorTag.TURRET_240_VNT_EXPLODE,
                BulletBehaviorTag.CHARGE_MORTAR_EXPLODE,
                BulletBehaviorTag.CHARGE_MORTAR_CHARGE_EXPLODE,
                BulletBehaviorTag.MINI_NUKE_DEMO,
                BulletBehaviorTag.MINI_NUKE_BALEFIRE)) {
            WeaponExplosionUtil.smooth(level, position.x, position.y, position.z,
                    config.explosive(), source, Math.max(1.0F, impactDamage), 1.0D, true,
                    config.armorThresholdNegation(), config.armorPiercingPercent()).explode();
            return true;
        }
        if (hasAnyBehavior(config, BulletBehaviorTag.STANDARD_EXPLODE,
                BulletBehaviorTag.HEAT_EXPLODE,
                BulletBehaviorTag.INCENDIARY_EXPLODE,
                BulletBehaviorTag.PHOSPHORUS_EXPLODE,
                BulletBehaviorTag.TURRET_240_STANDARD_EXPLODE,
                BulletBehaviorTag.MINI_NUKE_STANDARD,
                BulletBehaviorTag.MINI_NUKE_TINYTOT,
                BulletBehaviorTag.MINI_NUKE_HIVE)) {
            WeaponExplosionUtil.smooth(level, position.x, position.y, position.z,
                    config.explosive(), source, Math.max(1.0F, impactDamage), 1.0D, false,
                    config.armorThresholdNegation(), config.armorPiercingPercent()).explode();
            return true;
        }
        WeaponExplosionUtil.explodeStandard(level, position.x, position.y, position.z, config.explosive(), source,
                config.blockDamage(), config.incendiaryTicks() > 0);
        return true;
    }

    private static Vec3 tinyExplosionPosition(Vec3 position, @Nullable Direction hitSide) {
        if (hitSide == null) {
            return position;
        }
        return position.add(hitSide.getStepX() * 0.25D, hitSide.getStepY() * 0.25D,
                hitSide.getStepZ() * 0.25D);
    }

    private static boolean applyLegacySpecialVntImpact(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, float impactDamage) {
        String legacyName = config.legacyName();
        if ("cluster_submunition".equals(legacyName)) {
            explodeLegacyBlockVnt(level, position, source, 7.5F, 1.0D, impactDamage);
            return true;
        }
        if (config.hasBehavior(BulletBehaviorTag.DEMO_EXPLODE)) {
            explodeLegacyBlockVnt(level, position, source, config.explosive(), 1.0D, impactDamage);
            return true;
        }
        if (config.hasBehavior(BulletBehaviorTag.TURRET_240_VNT_EXPLODE)) {
            explodeLegacyBlockVnt(level, position, source, 10.0F, 1.0D, impactDamage);
            return true;
        }
        if (config.hasBehavior(BulletBehaviorTag.CHARGE_MORTAR_EXPLODE)) {
            ExplosionVnt explosion = new ExplosionVnt(level, position.x, position.y, position.z, 5.0F, source);
            explosion.setBlockAllocator(new BlockAllocatorBulkie(60.0D, 8))
                    .setBlockProcessor(new BlockProcessorStandard())
                    .setEntityProcessor(new EntityProcessorCrossSmooth(1.0D, Math.max(1.0F, impactDamage))
                            .setupPiercing(config.armorThresholdNegation(), config.armorPiercingPercent()))
                    .setPlayerProcessor(new PlayerProcessorStandard())
                    .setEffects(new ExplosionEffectWeapon(10, 2.5F, 1.0F))
                    .explode();
            return true;
        }
        if (config.hasBehavior(BulletBehaviorTag.CHARGE_MORTAR_CHARGE_EXPLODE)) {
            BlockProcessorStandard processor = new BlockProcessorStandard().setNoDrop();
            processor.withBlockEffect(new BlockMutatorDebris(ModBlocks.legacyBlock("block_slag").get()));
            ParticleUtil.spawnLegacyExplosionSmall(level, position.x, position.y + 0.5D, position.z);
            ExplosionVnt explosion = new ExplosionVnt(level, position.x, position.y, position.z, 15.0F, source);
            explosion.setBlockAllocator(new BlockAllocatorStandard())
                    .setBlockProcessor(processor)
                    .setEntityProcessor(new EntityProcessorCrossSmooth(1.0D, Math.max(1.0F, impactDamage))
                            .setupPiercing(config.armorThresholdNegation(), config.armorPiercingPercent()))
                    .setPlayerProcessor(new PlayerProcessorStandard())
                    .explode();
            return true;
        }
        return false;
    }

    private static void explodeLegacyBlockVnt(Level level, Vec3 position, @Nullable Entity source, float radius,
            double nodeDistance, float damage) {
        EntityProcessorCrossSmooth entityProcessor =
                new EntityProcessorCrossSmooth(nodeDistance, Math.max(1.0F, damage));
        new ExplosionVnt(level, position.x, position.y, position.z, radius, source)
                .setBlockAllocator(new BlockAllocatorStandard())
                .setBlockProcessor(new BlockProcessorStandard())
                .setEntityProcessor(entityProcessor)
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setEffects(new ExplosionEffectWeapon(10, 2.5F, 1.0F))
                .explode();
    }

    private static boolean applyMiniNukeImpact(BulletConfig config, Level level, Vec3 position, float impactDamage) {
        if (config.hasBehavior(BulletBehaviorTag.MINI_NUKE_STANDARD)) {
            explodeMiniNukeVnt(level, position, 10.0F, 2.0D, impactDamage, false, false);
            incrementMiniNukeRadiation(level, position, 1.0F);
            spawnMiniNukeMush(level, position, false);
            return true;
        }
        if (config.hasBehavior(BulletBehaviorTag.MINI_NUKE_DEMO)) {
            explodeMiniNukeVnt(level, position, 15.0F, 2.0D, impactDamage, true, false);
            incrementMiniNukeRadiation(level, position, 1.5F);
            spawnMiniNukeMush(level, position, false);
            return true;
        }
        if (config.hasBehavior(BulletBehaviorTag.MINI_NUKE_TINYTOT)) {
            explodeMiniNukeVnt(level, position, 5.0F, 2.0D, impactDamage, false, false);
            incrementMiniNukeRadiation(level, position, 0.25F);
            spawnMiniNukeTinyTot(level, position);
            return true;
        }
        if (config.hasBehavior(BulletBehaviorTag.MINI_NUKE_HIVE)) {
            explodeMiniNukeVnt(level, position, 5.0F, 1.0D, impactDamage, false, true);
            return true;
        }
        if (config.hasBehavior(BulletBehaviorTag.MINI_NUKE_BALEFIRE)) {
            explodeMiniNukeVnt(level, position, 10.0F, 2.0D, impactDamage, true, true);
            incrementMiniNukeRadiation(level, position, 1.5F);
            spawnMiniNukeMush(level, position, true);
            return true;
        }
        return false;
    }

    private static void explodeMiniNukeVnt(Level level, Vec3 position, float radius, double nodeDistance,
            float damage, boolean blockDamage, boolean specialEffect) {
        ExplosionVnt explosion = new ExplosionVnt(level, position.x, position.y, position.z, radius);
        if (blockDamage) {
            explosion.setBlockAllocator(new BlockAllocatorStandard(64));
            explosion.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(specialEffect
                    ? new BlockMutatorBalefire()
                    : new BlockMutatorFire()));
        }
        explosion.setEntityProcessor(new EntityProcessorCrossSmooth(nodeDistance, Math.max(1.0F, damage))
                .withRangeMod(1.5F))
                .setPlayerProcessor(new PlayerProcessorStandard());
        if (specialEffect && !blockDamage) {
            explosion.setEffects(new ExplosionEffectWeapon(10, 2.5F, 1.0F));
        }
        explosion.explode();
    }

    private static void incrementMiniNukeRadiation(Level level, Vec3 position, float multiplier) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                int distance = Math.abs(x) + Math.abs(z);
                if (distance < 4) {
                    float radiation = 50.0F / (distance + 1.0F) * multiplier;
                    ChunkRadiationManager.incrementRadiation(level,
                            BlockPos.containing(Math.floor(position.x + x * 16.0D),
                                    Math.floor(position.y), Math.floor(position.z + z * 16.0D)),
                            radiation);
                }
            }
        }
    }

    private static void spawnMiniNukeMush(Level level, Vec3 position, boolean balefire) {
        LegacySoundPlayer.playLegacyMukeExplosion(level, position.x, position.y + 0.5D, position.z);
        ParticleUtil.spawnMuke(level, position.x, position.y + 0.5D, position.z,
                balefire || level.random.nextInt(100) == 0);
    }

    private static void spawnMiniNukeTinyTot(Level level, Vec3 position) {
        LegacySoundPlayer.playLegacyMukeExplosion(level, position.x, position.y + 0.5D, position.z);
        ParticleUtil.spawnTinyTot(level, position.x, position.y + 0.5D, position.z);
    }

    private static float fixedExplosionDamage(BulletConfig config) {
        return Math.max(1.0F, config.damageMax());
    }

    private static boolean hasAnyBehavior(BulletConfig config, BulletBehaviorTag... behaviors) {
        for (BulletBehaviorTag behavior : behaviors) {
            if (config.hasBehavior(behavior)) {
                return true;
            }
        }
        return false;
    }

    private static boolean applyShrapnel(BulletConfig config, Level level, Vec3 position, @Nullable Entity source) {
        if (config.shrapnel() <= 0) {
            return false;
        }
        ExplosionLarge.spawnShrapnels(level, position.x, position.y, position.z, config.shrapnel(), 1.0F, source);
        return true;
    }

    private static boolean applyRainbow(BulletConfig config, Level level, Vec3 position) {
        if (config.rainbow() <= 0) {
            return false;
        }
        boolean spawned = NuclearExplosionUtil.spawnFleijaRainbow(level, position.x, position.y, position.z,
                config.rainbow(), config.rainbow());
        if (spawned) {
            LegacySoundPlayer.playSoundEffectRandomPitch(level, position, "random.explode",
                    SoundSource.BLOCKS, 100.0F, 0.9F, 0.1F);
        }
        return spawned;
    }

    private static boolean applyNuke(BulletConfig config, Level level, Vec3 position) {
        if (config.nuke() <= 0) {
            return false;
        }
        if (config.hasBehavior(BulletBehaviorTag.FOLLY_NUKE_IMPACT)) {
            return NuclearExplosionUtil.spawnNuclear(level, config.nuke(), position.x, position.y, position.z);
        }
        boolean spawned = level.addFreshEntity(NukeExplosionMk5Entity.statFac(level, config.nuke(),
                position.x, position.y, position.z));
        if (spawned) {
            ParticleUtil.spawnMuke(level, position.x, position.y + 0.5D, position.z,
                    level.random.nextInt(100) == 0);
            LegacySoundPlayer.playLegacyMukeExplosion(level, position.x, position.y + 0.5D, position.z);
        }
        return spawned;
    }

    private static boolean applyTaggedImpactEffects(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, @Nullable Direction hitSide, float impactDamage) {
        if (config.hasBehavior(BulletBehaviorTag.UFO_BLAST)) {
            applyUfoBlast(level, position);
            return true;
        }
        if (hasAnyBehavior(config, BulletBehaviorTag.LIGHTNING_BEAM_HIT,
                BulletBehaviorTag.LIGHTNING_BEAM_SPLIT)) {
            applyLightningBeamHit(level, offsetBeamBlockImpact(position, hitSide), source, impactDamage);
            return true;
        }
        if (config.hasBehavior(BulletBehaviorTag.BATTERY_SOCKET_DISCHARGE_BEAM)) {
            applyBatterySocketDischarge(level, position, source);
            return true;
        }
        if (config.hasBehavior(BulletBehaviorTag.SHREDDER_BEAM_SPLIT)) {
            applyShredderPulse(level, position, hitSide);
            if (hitSide != null) {
                applyShredderBeamBlockDamage(config, level, position, source, impactDamage);
            }
            return true;
        }
        return false;
    }

    private static Vec3 offsetBeamBlockImpact(Vec3 position, @Nullable Direction hitSide) {
        return hitSide == null ? position : position.add(hitSide.getStepX() * 0.5D,
                hitSide.getStepY() * 0.5D, hitSide.getStepZ() * 0.5D);
    }

    private static void applyLightningBeamHit(Level level, Vec3 position, @Nullable Entity source,
            float impactDamage) {
        if (level == null || level.isClientSide() || position == null) {
            return;
        }
        new ExplosionVnt(level, position.x, position.y, position.z, 2.0F, source, false,
                Explosion.BlockInteraction.KEEP)
                .setEntityProcessor(new EntityProcessorCrossSmooth(1.0D, impactDamage)
                        .setDamageClass(DamageClass.ELECTRIC))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .explode();
        LegacySoundPlayer.playLegacyUfoBlast(level, position, 5.0F, 0.9F, 0.2F);
        LegacySoundPlayer.playLegacyFireworksBlast(level, position, 5.0F, 0.5F);

        float yaw = level.random.nextFloat() * 180.0F;
        for (int i = 0; i < 3; i++) {
            ParticleUtil.spawnPlasmaBlast(level, position.x, position.y, position.z,
                    0.5F, 0.5F, 1.0F, -60.0F + 60.0F * i, yaw, 2.0F);
        }
    }

    public static void applyBatterySocketDischarge(Level level, Vec3 position, @Nullable Entity source) {
        if (level == null || level.isClientSide() || position == null) {
            return;
        }
        new ExplosionVnt(level, position.x, position.y, position.z, 5.0F, source, false,
                Explosion.BlockInteraction.KEEP)
                .setEntityProcessor(new EntityProcessorCrossSmooth(1.0D, 20.0F)
                        .setDamageClass(DamageClass.ELECTRIC))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setEffects(new ExplosionEffectStandard())
                .explode();
        LegacySoundPlayer.playLegacyUfoBlast(level, position, 5.0F, 0.9F, 0.2F);
    }

    public static void applyShredderPulse(Level level, Vec3 position, @Nullable Direction hitSide) {
        if (level == null || level.isClientSide() || position == null) {
            return;
        }
        Vec3 pulse = hitSide == null ? position : position.add(hitSide.getStepX() * 0.05D,
                hitSide.getStepY() * 0.05D, hitSide.getStepZ() * 0.05D);
        float yaw = 0.0F;
        float pitch = 0.0F;
        if (hitSide != null) {
            switch (hitSide) {
                case NORTH -> pitch = 90.0F;
                case SOUTH -> {
                    yaw = 180.0F;
                    pitch = 90.0F;
                }
                case EAST -> {
                    yaw = 90.0F;
                    pitch = 90.0F;
                }
                case WEST -> {
                    yaw = 270.0F;
                    pitch = 90.0F;
                }
                default -> {
                }
            }
        }
        ParticleUtil.spawnPlasmaBlast(level, pulse.x, pulse.y, pulse.z, 0.5F, 0.5F, 1.0F, pitch, yaw, 0.75F);
    }

    public static int applyShredderPulseDamage(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, float impactDamage, double radius) {
        if (config == null || level == null || level.isClientSide() || position == null || impactDamage <= 0.0F) {
            return 0;
        }
        AABB area = new AABB(position, position).inflate(radius);
        int damaged = 0;
        for (Entity entity : level.getEntities((Entity) null, area, Entity::isAlive)) {
            if (EntityDamageUtil.attackEntityFromNt(entity, config.damageSource(level, null, source), impactDamage,
                    true, false, 0.0D, 0.0F, 0.0F)) {
                damaged++;
            }
        }
        return damaged;
    }

    private static int applyShredderBeamBlockDamage(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, float impactDamage) {
        return applyShredderPulseDamage(config, level, position, source, impactDamage, 0.75D);
    }

    public static void applyUfoBlast(Level level, Vec3 position) {
        if (level == null || level.isClientSide() || position == null) {
            return;
        }
        LegacySoundPlayer.playLegacyUfoBlast(level, position, 5.0F, 0.9F, 0.2F);
        LegacySoundPlayer.playLegacyFireworksBlast(level, position, 5.0F, 0.5F);
        ExplosionNukeGeneric.dealDamage(level, position.x, position.y, position.z, 10.0D, 50.0F);

        for (int i = 0; i < 3; i++) {
            ParticleUtil.spawnPlasmaBlast(level, position.x, position.y, position.z,
                    0.0F, 0.75F, 1.0F, -30.0F + 30.0F * i, level.random.nextFloat() * 180.0F, 5.0F);
        }
    }

    private static BlockBreakResult applyBlockBreak(BulletConfig config, Level level,
            @Nullable BlockPos hitBlock, @Nullable Entity source) {
        if (hitBlock == null) {
            return BlockBreakResult.NONE;
        }
        BlockState state = level.getBlockState(hitBlock);
        if (state.isAir()) {
            return BlockBreakResult.NONE;
        }
        if (config.hasBehavior(BulletBehaviorTag.BATTERY_SOCKET_DISCHARGE_BEAM)) {
            return new BlockBreakResult(level.destroyBlock(hitBlock, false), false, false);
        }
        if (config.destroysBlocks() && state.getDestroySpeed(level, hitBlock) <= 120.0F) {
            return new BlockBreakResult(level.destroyBlock(hitBlock, false), false, false);
        }
        if (config.breaksGlass() && isLegacyGlass(state)) {
            return new BlockBreakResult(false, level.destroyBlock(hitBlock, false), false);
        }
        if (config.breaksGlass() && state.getBlock() instanceof ShotDetonatableBlock detonatable) {
            return new BlockBreakResult(false, false,
                    detonatable.detonateFromShot(level, hitBlock, state, source));
        }
        return BlockBreakResult.NONE;
    }

    private static boolean isLegacyGlass(BlockState state) {
        Block block = state.getBlock();
        return state.is(Blocks.GLASS)
                || state.is(Blocks.GLASS_PANE)
                || block instanceof StainedGlassBlock
                || block instanceof StainedGlassPaneBlock;
    }

    private static BlockPos legacyCastPos(double x, double y, double z) {
        return new BlockPos((int) x, (int) y, (int) z);
    }

    private static BlockPos legacyFloorPos(double x, double y, double z) {
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    private static int legacyRoundPos(double value) {
        return (int) (value + 0.5D);
    }

    private static float impactDamage(BulletConfig config, float impactDamage) {
        return impactDamage > 0.0F ? impactDamage : fixedExplosionDamage(config);
    }

    public record BlockImpactResult(boolean discardProjectile, boolean placedFire, boolean emp, boolean jolt,
            boolean explosion, boolean shrapnel, boolean rainbow, boolean nuke, boolean specialBehavior,
            boolean brokeOrDestroyedBlock, boolean shotDetonated,
            boolean extinguishedFire,
            List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests) {
        public static final BlockImpactResult NONE = new BlockImpactResult(false, false, false, false, false, false,
                false, false, false, false, false, false, Collections.emptyList());
    }

    public record EntityImpactResult(boolean discardProjectile, EntityHurtResult hurt, BlockImpactResult blockImpact) {
        public static final EntityImpactResult NONE = new EntityImpactResult(false, EntityHurtResult.NONE,
                BlockImpactResult.NONE);
    }

    public record EntityHurtResult(boolean setOnFire, boolean leadApplied, boolean instakilled,
            boolean damagedArmor, boolean extinguishedFire, int customEffectsApplied) {
        public static final EntityHurtResult NONE = new EntityHurtResult(false, false, false, false, false, 0);
    }

    private record BlockBreakResult(boolean destroyedBlock, boolean brokeGlass, boolean shotDetonated) {
        private static final BlockBreakResult NONE = new BlockBreakResult(false, false, false);
    }

    private record LegacyLingeringFire(float width, float height, int duration, int blockRadius, int fireType) {
    }

    private BulletImpactUtil() {
    }
}
