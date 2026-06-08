package com.hbm.ntm.bullet;

import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class BulletRicochetUtil {
    public static BlockHitResult applyBlockHit(BulletConfig config, Level level, Vec3 hitLocation, Vec3 motion,
            @Nullable Direction side, RandomSource random, @Nullable Entity source, @Nullable BlockPos hitBlock) {
        return applyBlockHit(config, level, hitLocation, motion, side, random, source, hitBlock, false);
    }

    public static BlockHitResult applyBlockHit(BulletConfig config, Level level, Vec3 hitLocation, Vec3 motion,
            @Nullable Direction side, RandomSource random, @Nullable Entity source, @Nullable BlockPos hitBlock,
            boolean inGround) {
        if (config == null || level == null || hitLocation == null || motion == null) {
            return BlockHitResult.NONE;
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
                BulletImpactUtil.applyBlockImpactEffects(config, level, hitLocation, source, hitBlock, inGround);
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

    public record BlockHitResult(boolean ricocheted, Vec3 motion, BulletRuntimeUtil.RicochetRoll ricochet,
            BulletImpactUtil.BlockImpactResult blockImpact, boolean playedSound) {
        public static final BlockHitResult NONE = new BlockHitResult(false, Vec3.ZERO,
                new BulletRuntimeUtil.RicochetRoll(false, Vec3.ZERO, false, false, 0.0D),
                BulletImpactUtil.BlockImpactResult.NONE, false);
    }

    private record SoundSpec(SoundEvent event, float volume) {
    }

    private BulletRicochetUtil() {
    }
}
