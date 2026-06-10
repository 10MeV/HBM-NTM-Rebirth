package com.hbm.ntm.bullet;

import com.hbm.ntm.block.ShotDetonatableBlock;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public final class BulletRicochetUtil {
    public static BlockHitResult applyBlockHit(BulletConfig config, Level level, Vec3 hitLocation, Vec3 motion,
            @Nullable Direction side, RandomSource random, @Nullable Entity source, @Nullable BlockPos hitBlock) {
        return applyBlockHit(config, level, hitLocation, motion, side, random, source, hitBlock, false);
    }

    public static BlockHitResult applyBlockHit(BulletConfig config, Level level, Vec3 hitLocation, Vec3 motion,
            @Nullable Direction side, RandomSource random, @Nullable Entity source, @Nullable BlockPos hitBlock,
            boolean inGround) {
        return applyBlockHit(config, level, hitLocation, motion, side, random, source, hitBlock, inGround, 0.0F);
    }

    public static BlockHitResult applyBlockHit(BulletConfig config, Level level, Vec3 hitLocation, Vec3 motion,
            @Nullable Direction side, RandomSource random, @Nullable Entity source, @Nullable BlockPos hitBlock,
            boolean inGround, float impactDamage) {
        if (config == null || level == null || hitLocation == null || motion == null) {
            return BlockHitResult.NONE;
        }

        if (config.hasBehavior(BulletBehaviorTag.SHREDDER_RICOCHET)) {
            return applyShredderRicochet(config, level, hitLocation, motion, side, source, hitBlock, impactDamage);
        }

        RandomSource rollRandom = random == null ? level.random : random;
        BulletRuntimeUtil.RicochetRoll ricochet = BulletRuntimeUtil.rollRicochet(config, motion, side, rollRandom);
        if (ricochet.ricochet()) {
            boolean playedSound = playRicochetSound(config, level, hitLocation);
            return new BlockHitResult(true, ricochet.motion(), ricochet, BulletImpactUtil.BlockImpactResult.NONE,
                    playedSound);
        }

        if (config.spectral()) {
            return new BlockHitResult(false, motion, ricochet, BulletImpactUtil.BlockImpactResult.NONE, false);
        }

        BulletImpactUtil.BlockImpactResult impact =
                BulletImpactUtil.applyBlockImpactEffects(config, level, hitLocation, source, hitBlock, side,
                        inGround, impactDamage);
        return new BlockHitResult(false, motion, ricochet, impact, false);
    }

    public static boolean playRicochetSound(BulletConfig config, Level level, Vec3 position) {
        SoundSpec sound = soundFor(config.plink());
        if (sound == null || level == null || position == null || level.isClientSide()) {
            return false;
        }
        level.playSound(null, position.x, position.y, position.z, sound.event(), SoundSource.PLAYERS,
                sound.volume(), 1.0F);
        return true;
    }

    @Nullable
    private static SoundSpec soundFor(BulletPlink plink) {
        return switch (plink) {
            case BULLET -> new SoundSpec(ModSounds.WEAPON_RICOCHET.get(), 0.25F);
            case GRENADE -> new SoundSpec(ModSounds.WEAPON_GRENADE_BOUNCE.get(), 1.0F);
            default -> null;
        };
    }

    private static BlockHitResult applyShredderRicochet(BulletConfig config, Level level, Vec3 hitLocation,
            Vec3 motion, @Nullable Direction side, @Nullable Entity source, @Nullable BlockPos hitBlock,
            float impactDamage) {
        if (hitBlock == null || side == null) {
            return new BlockHitResult(false, motion, noRicochet(motion, 0.0D), BulletImpactUtil.BlockImpactResult.NONE,
                    false);
        }

        ShredderBlockTouch touch = applyShredderBlockTouch(level, hitBlock, source);
        if (touch.brokeGlass()) {
            return new BlockHitResult(false, motion, noRicochet(motion, 0.0D),
                    new BulletImpactUtil.BlockImpactResult(false, false, false, false, false, false, false, false,
                            true, true, touch.shotDetonated(), false, Collections.emptyList()),
                    false);
        }

        double angle = surfaceAngle(motion, side);
        if (angle <= config.ricochetAngle()) {
            BulletImpactUtil.applyShredderPulse(level, hitLocation, side);
            BulletImpactUtil.applyShredderPulseDamage(config, level, hitLocation, source,
                    impactDamage > 0.0F ? impactDamage : config.damageMax(), 0.5D);
            Vec3 reflected = reflect(motion, side);
            return new BlockHitResult(true, reflected, new BulletRuntimeUtil.RicochetRoll(true, reflected,
                    true, false, angle),
                    new BulletImpactUtil.BlockImpactResult(false, false, false, false, false, false, false, false,
                            true, touch.shotDetonated(), touch.shotDetonated(), false, Collections.emptyList()),
                    false);
        }

        return new BlockHitResult(false, motion, noRicochet(motion, angle),
                new BulletImpactUtil.BlockImpactResult(true, false, false, false, false, false, false, false,
                        touch.shotDetonated(), touch.shotDetonated(), touch.shotDetonated(), false,
                        Collections.emptyList()),
                false);
    }

    private static ShredderBlockTouch applyShredderBlockTouch(Level level, BlockPos hitBlock,
            @Nullable Entity source) {
        if (level == null || hitBlock == null || level.isClientSide()) {
            return ShredderBlockTouch.NONE;
        }
        BlockState state = level.getBlockState(hitBlock);
        if (state.isAir()) {
            return ShredderBlockTouch.NONE;
        }
        if (isLegacyGlass(state)) {
            return new ShredderBlockTouch(level.destroyBlock(hitBlock, false), false);
        }
        boolean detonated = state.getBlock() instanceof ShotDetonatableBlock detonatable
                && detonatable.detonateFromShot(level, hitBlock, state, source);
        if ("deco_crt".equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath())) {
            // The modern bulk legacy CRT block is not yet registered as a metadata-preserving deco_crt.
            return new ShredderBlockTouch(false, detonated);
        }
        return new ShredderBlockTouch(false, detonated);
    }

    private static boolean isLegacyGlass(BlockState state) {
        Block block = state.getBlock();
        return state.is(Blocks.GLASS)
                || state.is(Blocks.GLASS_PANE)
                || block instanceof StainedGlassBlock
                || block instanceof StainedGlassPaneBlock;
    }

    private static Vec3 reflect(Vec3 motion, Direction side) {
        return switch (side.getAxis()) {
            case X -> new Vec3(-motion.x, motion.y, motion.z);
            case Y -> new Vec3(motion.x, -motion.y, motion.z);
            case Z -> new Vec3(motion.x, motion.y, -motion.z);
        };
    }

    private static BulletRuntimeUtil.RicochetRoll noRicochet(Vec3 motion, double angle) {
        return new BulletRuntimeUtil.RicochetRoll(false, motion, false, false, angle);
    }

    private static double surfaceAngle(Vec3 motion, Direction side) {
        if (motion == null || motion.lengthSqr() <= 1.0E-7D || side == null) {
            return 0.0D;
        }
        Vec3 face = new Vec3(side.getStepX(), side.getStepY(), side.getStepZ());
        double firstLength = motion.length();
        double secondLength = face.length();
        double cosine = motion.dot(face) / (firstLength * secondLength);
        double angle = Math.acos(Math.max(-1.0D, Math.min(1.0D, cosine))) * 180.0D / Math.PI;
        if (angle >= 180.0D) {
            angle -= 180.0D;
        }
        return Math.abs(angle - 90.0D);
    }

    public record BlockHitResult(boolean ricocheted, Vec3 motion, BulletRuntimeUtil.RicochetRoll ricochet,
            BulletImpactUtil.BlockImpactResult blockImpact, boolean playedSound) {
        public static final BlockHitResult NONE = new BlockHitResult(false, Vec3.ZERO,
                new BulletRuntimeUtil.RicochetRoll(false, Vec3.ZERO, false, false, 0.0D),
                BulletImpactUtil.BlockImpactResult.NONE, false);
    }

    private record SoundSpec(SoundEvent event, float volume) {
    }

    private record ShredderBlockTouch(boolean brokeGlass, boolean shotDetonated) {
        private static final ShredderBlockTouch NONE = new ShredderBlockTouch(false, false);
    }

    private BulletRicochetUtil() {
    }
}
