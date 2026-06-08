package com.hbm.ntm.bullet;

import com.hbm.ntm.entity.logic.NukeExplosionMk5Entity;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.explosion.NuclearExplosionUtil;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class BulletImpactUtil {
    public static BlockImpactResult applyBlockImpactEffects(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, @Nullable BlockPos hitBlock) {
        return applyBlockImpactEffects(config, level, position, source, hitBlock, false);
    }

    public static BlockImpactResult applyBlockImpactEffects(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, @Nullable BlockPos hitBlock, boolean inGround) {
        if (config == null || level == null || position == null) {
            return BlockImpactResult.NONE;
        }

        boolean discard = shouldDiscardAfterBlockImpact(config, hitBlock != null, inGround);
        if (level.isClientSide()) {
            return new BlockImpactResult(discard, false, false, false, false, false, false, false, false, false,
                    Collections.emptyList());
        }

        boolean fire = applyIncendiaryBlocks(config, level, position);
        boolean emp = applyEmp(config, level, position);
        boolean jolt = applyJolt(config, level, position);
        boolean explosion = applyExplosion(config, level, position, source);
        boolean shrapnel = applyShrapnel(config, level, position, source);
        boolean rainbow = applyRainbow(config, level, position);
        boolean nuke = applyNuke(config, level, position);
        boolean specialBehavior = applyTaggedImpactEffects(config, level, position);
        List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests =
                BulletSpecialSpawnUtil.collectImpactSpawnRequests(config, level, source, position, null);
        BlockBreakResult blockBreak = applyBlockBreak(config, level, hitBlock);
        return new BlockImpactResult(discard, fire, emp, jolt, explosion, shrapnel, rainbow, nuke,
                specialBehavior, blockBreak.destroyedBlock() || blockBreak.brokeGlass(), spawnRequests);
    }

    public static EntityImpactResult applyEntityImpactEffects(BulletConfig config, Entity target,
            @Nullable Entity source, @Nullable Vec3 position) {
        return applyEntityImpactEffects(config, target, source, position, null);
    }

    public static EntityImpactResult applyEntityImpactEffects(BulletConfig config, Entity target,
            @Nullable Entity source, @Nullable Vec3 position, @Nullable RandomSource random) {
        if (config == null || target == null) {
            return EntityImpactResult.NONE;
        }
        EntityHurtResult hurt = applyEntityHurtEffects(config, target, random);
        Vec3 impact = position == null ? target.position() : position;
        BlockImpactResult block = applyBlockImpactEffects(config, target.level(), impact, source, null, false);
        return new EntityImpactResult(block.discardProjectile(), hurt, block);
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
        if (config.incendiaryTicks() > 0) {
            target.setSecondsOnFire(config.incendiaryTicks());
            fire = true;
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

        return new EntityHurtResult(fire, lead, instakill);
    }

    public static boolean shouldDiscardAfterBlockImpact(BulletConfig config, boolean hasBlockHit, boolean inGround) {
        if (config == null) {
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
        if (config.incendiaryTicks() <= 0) {
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

    private static boolean maybeSetFire(Level level, BlockPos pos) {
        if (level.random.nextInt(3) != 0 || !level.getBlockState(pos).isAir()) {
            return false;
        }
        return level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
    }

    private static boolean applyEmp(BulletConfig config, Level level, Vec3 position) {
        if (config.emp() <= 0) {
            return false;
        }
        ExplosionNukeGeneric.empBlast(level, legacyRoundPos(position.x), legacyRoundPos(position.y),
                legacyRoundPos(position.z), config.emp());
        return true;
    }

    private static boolean applyJolt(BulletConfig config, Level level, Vec3 position) {
        if (config.jolt() <= 0.0D) {
            return false;
        }
        ExplosionLarge.jolt(level, position.x, position.y, position.z, config.jolt(), 150, 0.25D);
        return true;
    }

    private static boolean applyExplosion(BulletConfig config, Level level, Vec3 position, @Nullable Entity source) {
        if (config.explosive() <= 0.0F) {
            return false;
        }
        WeaponExplosionUtil.explodeStandard(level, position.x, position.y, position.z, config.explosive(), source,
                config.blockDamage(), config.incendiaryTicks() > 0);
        return true;
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
            level.playSound(null, position.x, position.y, position.z, SoundEvents.GENERIC_EXPLODE,
                    SoundSource.BLOCKS, 100.0F, level.random.nextFloat() * 0.1F + 0.9F);
        }
        return spawned;
    }

    private static boolean applyNuke(BulletConfig config, Level level, Vec3 position) {
        if (config.nuke() <= 0) {
            return false;
        }
        boolean spawned = level.addFreshEntity(NukeExplosionMk5Entity.statFac(level, config.nuke(),
                position.x, position.y, position.z));
        if (spawned) {
            ParticleUtil.spawnMuke(level, position.x, position.y + 0.5D, position.z,
                    level.random.nextInt(100) == 0);
            level.playSound(null, position.x, position.y, position.z, ModSounds.WEAPON_MUKE_EXPLOSION.get(),
                    SoundSource.BLOCKS, 15.0F, 1.0F);
        }
        return spawned;
    }

    private static boolean applyTaggedImpactEffects(BulletConfig config, Level level, Vec3 position) {
        if (config.hasBehavior(BulletBehaviorTag.UFO_BLAST)) {
            applyUfoBlast(level, position);
            return true;
        }
        return false;
    }

    public static void applyUfoBlast(Level level, Vec3 position) {
        if (level == null || level.isClientSide() || position == null) {
            return;
        }
        level.playSound(null, position.x, position.y, position.z, ModSounds.ENTITY_UFO_BLAST.get(),
                SoundSource.BLOCKS, 5.0F, 0.9F + level.random.nextFloat() * 0.2F);
        level.playSound(null, position.x, position.y, position.z, SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.BLOCKS, 5.0F, 0.5F);
        ExplosionNukeGeneric.dealDamage(level, position.x, position.y, position.z, 10.0D, 50.0F);

        for (int i = 0; i < 3; i++) {
            ParticleUtil.spawnPlasmaBlast(level, position.x, position.y, position.z,
                    0.0F, 0.75F, 1.0F, -30.0F + 30.0F * i, level.random.nextFloat() * 180.0F, 5.0F);
        }
    }

    private static BlockBreakResult applyBlockBreak(BulletConfig config, Level level, @Nullable BlockPos hitBlock) {
        if (hitBlock == null) {
            return BlockBreakResult.NONE;
        }
        BlockState state = level.getBlockState(hitBlock);
        if (state.isAir()) {
            return BlockBreakResult.NONE;
        }
        if (config.destroysBlocks() && state.getDestroySpeed(level, hitBlock) <= 120.0F) {
            return new BlockBreakResult(level.destroyBlock(hitBlock, false), false);
        }
        if (config.breaksGlass() && isLegacyGlass(state)) {
            return new BlockBreakResult(false, level.destroyBlock(hitBlock, false));
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

    private static int legacyRoundPos(double value) {
        return (int) (value + 0.5D);
    }

    public record BlockImpactResult(boolean discardProjectile, boolean placedFire, boolean emp, boolean jolt,
            boolean explosion, boolean shrapnel, boolean rainbow, boolean nuke, boolean specialBehavior,
            boolean brokeOrDestroyedBlock, List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests) {
        public static final BlockImpactResult NONE = new BlockImpactResult(false, false, false, false, false, false,
                false, false, false, false, Collections.emptyList());
    }

    public record EntityImpactResult(boolean discardProjectile, EntityHurtResult hurt, BlockImpactResult blockImpact) {
        public static final EntityImpactResult NONE = new EntityImpactResult(false, EntityHurtResult.NONE,
                BlockImpactResult.NONE);
    }

    public record EntityHurtResult(boolean setOnFire, boolean leadApplied, boolean instakilled) {
        public static final EntityHurtResult NONE = new EntityHurtResult(false, false, false);
    }

    private record BlockBreakResult(boolean destroyedBlock, boolean brokeGlass) {
        private static final BlockBreakResult NONE = new BlockBreakResult(false, false);
    }

    private BulletImpactUtil() {
    }
}
