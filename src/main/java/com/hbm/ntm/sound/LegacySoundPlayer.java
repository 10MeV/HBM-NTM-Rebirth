package com.hbm.ntm.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public final class LegacySoundPlayer {
    public static void playSoundEffect(Level level, double x, double y, double z, String sound, float volume, float pitch) {
        playSoundEffect(level, x, y, z, sound, SoundSource.BLOCKS, volume, pitch);
    }

    public static void playSoundEffect(Level level, Vec3 position, String sound, float volume, float pitch) {
        playSoundEffect(level, position, sound, SoundSource.BLOCKS, volume, pitch);
    }

    public static void playSoundEffect(Level level, Vec3 position, String sound, SoundSource source, float volume, float pitch) {
        if (position == null) {
            return;
        }
        playSoundEffect(level, position.x, position.y, position.z, sound, source, volume, pitch);
    }

    public static void playSoundEffect(Level level, BlockPos pos, String sound, float volume, float pitch) {
        playSoundEffect(level, pos, sound, SoundSource.BLOCKS, volume, pitch);
    }

    public static void playSoundEffect(Level level, BlockPos pos, String sound, SoundSource source, float volume, float pitch) {
        if (pos == null) {
            return;
        }
        playSoundEffect(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, sound, source, volume, pitch);
    }

    public static void playSoundEffect(Level level, double x, double y, double z, String sound, SoundSource source,
            float volume, float pitch) {
        SoundEvent event = resolveEvent(sound);
        if (level == null || event == null) {
            return;
        }
        level.playSound(null, x, y, z, event, source == null ? SoundSource.BLOCKS : source, volume, pitch);
    }

    public static void playSoundEffectRandomPitch(Level level, double x, double y, double z, String sound,
            SoundSource source, float volume, float basePitch, float randomPitch) {
        playSoundEffect(level, x, y, z, sound, source, volume, randomPitch(level, basePitch, randomPitch));
    }

    public static void playSoundEffectRandomPitch(Level level, BlockPos pos, String sound,
            SoundSource source, float volume, float basePitch, float randomPitch) {
        if (pos == null) {
            return;
        }
        playSoundEffectRandomPitch(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                sound, source, volume, basePitch, randomPitch);
    }

    public static void playSoundEffectRandomPitch(Level level, Vec3 position, String sound,
            SoundSource source, float volume, float basePitch, float randomPitch) {
        if (position == null) {
            return;
        }
        playSoundEffectRandomPitch(level, position.x, position.y, position.z, sound, source, volume, basePitch, randomPitch);
    }

    public static void playSoundAtEntity(Entity entity, String sound, float volume, float pitch) {
        playSoundAtEntity(entity, sound, SoundSource.NEUTRAL, volume, pitch);
    }

    public static void playSoundAtEntity(Entity entity, String sound, SoundSource source, float volume, float pitch) {
        if (entity == null) {
            return;
        }
        playSoundEffect(entity.level(), entity.getX(), entity.getY(), entity.getZ(), sound,
                source == null ? SoundSource.NEUTRAL : source, volume, pitch);
    }

    public static void playSoundAtPlayer(Player player, String sound, float volume, float pitch) {
        playSoundAtPlayer(player, sound, SoundSource.PLAYERS, volume, pitch);
    }

    public static void playSoundAtPlayer(Player player, String sound, SoundSource source, float volume, float pitch) {
        if (player == null) {
            return;
        }
        playSoundEffect(player.level(), player.getX(), player.getY(), player.getZ(), sound,
                source == null ? SoundSource.PLAYERS : source, volume, pitch);
    }

    public static void playLegacyTechBoop(Entity entity, float volume, float pitch) {
        playSoundAtEntity(entity, "hbm:item.techBoop", SoundSource.PLAYERS, volume, pitch);
    }

    public static void playLegacyTechBoop(Level level, BlockPos pos, SoundSource source, float volume, float pitch) {
        playSoundEffect(level, pos, "hbm:item.techBoop", source, volume, pitch);
    }

    public static void playLegacyTechBoop(Level level, double x, double y, double z,
            SoundSource source, float volume, float pitch) {
        playSoundEffect(level, x, y, z, "hbm:item.techBoop", source, volume, pitch);
    }

    public static void playLegacyTechBleep(Entity entity, float volume, float pitch) {
        playSoundAtEntity(entity, "hbm:item.techBleep", SoundSource.PLAYERS, volume, pitch);
    }

    public static void playLegacyTechBleep(Level level, BlockPos pos, SoundSource source, float volume, float pitch) {
        playSoundEffect(level, pos, "hbm:item.techBleep", source, volume, pitch);
    }

    public static void playLegacyGeiger(Level level, Entity entity, int sound) {
        if (entity == null) {
            return;
        }
        playLegacyGeiger(level, entity.getX(), entity.getY(), entity.getZ(), sound);
    }

    public static void playLegacyGeiger(Level level, double x, double y, double z, int sound) {
        playSoundEffect(level, x, y, z, "hbm:item.geiger" + clampGeigerLevel(sound),
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void playLegacyGasMaskScrew(Entity entity) {
        playSoundAtEntity(entity, "hbm:item.gasmaskScrew", SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void playLegacyRadaway(Entity entity) {
        playSoundAtEntity(entity, "hbm:item.radaway", SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void playLegacySyringe(Entity entity) {
        playSoundAtEntity(entity, "hbm:item.syringe", SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void playLegacyUpgradePlug(Level level, BlockPos pos, SoundSource source, float volume, float pitch) {
        playSoundEffect(level, pos, "hbm:item.upgradePlug", source, volume, pitch);
    }

    public static void playLegacyJetpackTank(Entity entity) {
        playSoundAtEntity(entity, "hbm:item.jetpackTank", SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void playLegacyPressOperate(Level level, BlockPos pos, float volume, float pitch) {
        playSoundEffect(level, pos, "hbm:block.pressOperate", SoundSource.BLOCKS, volume, pitch);
    }

    public static void playLegacyCrateOpen(Level level, BlockPos pos, float volume, float pitch) {
        playSoundEffect(level, pos, "hbm:block.crateOpen", SoundSource.BLOCKS, volume, pitch);
    }

    public static void playLegacyCrateClose(Level level, BlockPos pos, float volume, float pitch) {
        playSoundEffect(level, pos, "hbm:block.crateClose", SoundSource.BLOCKS, volume, pitch);
    }

    public static void playLegacySonarPing(Level level, BlockPos pos, float volume, float pitch) {
        playSoundEffect(level, pos, "hbm:block.sonarPing", SoundSource.BLOCKS, volume, pitch);
    }

    public static void playLegacyReactorStart(Level level, BlockPos pos, float volume, float pitch) {
        playSoundEffect(level, pos, "hbm:block.reactorStart", SoundSource.BLOCKS, volume, pitch);
    }

    public static void playLegacyFstbmbStart(Level level, BlockPos pos, float volume, float pitch) {
        playSoundEffect(level, pos, "hbm:weapon.fstbmbStart", SoundSource.BLOCKS, volume, pitch);
    }

    public static void playLegacyFstbmbPing(Level level, BlockPos pos, float volume, float pitch) {
        playSoundEffect(level, pos, "hbm:weapon.fstbmbPing", SoundSource.BLOCKS, volume, pitch);
    }

    public static void playLegacyTubeFwoomp(Level level, BlockPos pos, float volume,
            float basePitch, float randomPitch) {
        playSoundEffectRandomPitch(level, pos, "hbm:weapon.reload.tubeFwoomp",
                SoundSource.BLOCKS, volume, basePitch, randomPitch);
    }

    public static void playLegacyFlamethrowerShoot(Entity entity, float volume, float pitch) {
        playSoundAtEntity(entity, "hbm:weapon.flamethrowerShoot", SoundSource.PLAYERS, volume, pitch);
    }

    public static void playLegacyFlamethrowerShoot(Level level, double x, double y, double z,
            SoundSource source, float volume, float pitch) {
        playSoundEffect(level, x, y, z, "hbm:weapon.flamethrowerShoot", source, volume, pitch);
    }

    public static void playLegacyImmolatorShoot(Level level, double x, double y, double z,
            SoundSource source, float volume, float pitch) {
        playSoundEffect(level, x, y, z, "hbm:weapon.immolatorShoot", source, volume, pitch);
    }

    public static void playLegacyRocketFlame(Entity entity, float volume, float pitch) {
        playSoundAtEntity(entity, "hbm:weapon.rocketFlame", SoundSource.PLAYERS, volume, pitch);
    }

    public static void playLegacyGavelWhack(Level level, double x, double y, double z, float volume, float pitch) {
        playSoundEffect(level, x, y, z, "hbm:weapon.whack", SoundSource.PLAYERS, volume, pitch);
    }

    public static void playLegacyBoltOpen(Level level, BlockPos pos, float volume, float pitch) {
        playSoundEffect(level, pos, "hbm:weapon.reload.boltOpen", SoundSource.BLOCKS, volume, pitch);
    }

    public static void playLegacySlicer(Entity entity) {
        playSoundAtEntity(entity, "hbm:entity.slicer", SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void playLegacyPinBreak(Entity entity) {
        playSoundAtEntity(entity, "hbm:item.pinBreak", SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void playLegacyTesla(Entity entity) {
        playSoundAtEntity(entity, "hbm:weapon.tesla", SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void playLegacyShotgunShoot(Entity entity, float volume, float pitch) {
        playSoundAtEntity(entity, "hbm:weapon.shotgunShoot", SoundSource.PLAYERS, volume, pitch);
    }

    public static void playLegacyPlayerCough(Entity entity) {
        playSoundAtEntity(entity, "hbm:player.cough", SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    public static void playLegacyPlayerVomit(Entity entity) {
        playSoundAtEntity(entity, "hbm:player.vomit", SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    public static void playLegacySoyuzTakeoff(Entity entity) {
        playSoundAtEntity(entity, "hbm:entity.soyuzTakeoff", SoundSource.PLAYERS, 100.0F, 1.1F);
    }

    public static void playLegacySoyuzedAlarm(Entity entity) {
        playSoundAtEntity(entity, "hbm:alarm.soyuzed", SoundSource.RECORDS, 100.0F, 1.0F);
    }

    public static void playLegacyLaserBang(Entity entity) {
        playSoundAtEntity(entity, "hbm:weapon.laserBang", SoundSource.HOSTILE, 100.0F, 1.0F);
    }

    public static void playLegacyFlamethrowerIgnite(Level level, BlockPos pos,
            float volume, float basePitch, float randomPitch) {
        playSoundEffectRandomPitch(level, pos, "hbm:weapon.flamethrowerIgnite",
                SoundSource.BLOCKS, volume, basePitch, randomPitch);
    }

    public static void playLegacyNuclearExplosionClient(Level level, double x, double y, double z,
            float volume, float pitch) {
        playLocalSound(level, x, y, z, "hbm:weapon.nuclearExplosion", SoundSource.BLOCKS, volume, pitch, false);
    }

    public static void playLegacyChainsaw(Level level, double x, double y, double z) {
        playSoundEffect(level, x, y, z, "hbm:weapon.chainsaw", SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    public static void playLegacyWeaponSwitchMode(Entity entity, int previousMode) {
        playSoundAtEntity(entity, previousMode == 0 ? "hbm:weapon.switchmode1" : "hbm:weapon.switchmode2",
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void playLegacyFireStab(Level level, double x, double y, double z,
            float volume, float basePitch, float randomPitch) {
        playSoundEffectRandomPitch(level, x, y, z, "hbm:weapon.fire.stab",
                SoundSource.PLAYERS, volume, basePitch, randomPitch);
    }

    public static void playLegacyTauRelease(Entity entity) {
        playSoundAtEntity(entity, "hbm:weapon.fire.tauRelease", SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void playLocalSound(Level level, double x, double y, double z, String sound, SoundSource source,
            float volume, float pitch, boolean distanceDelay) {
        SoundEvent event = resolveEvent(sound);
        if (level == null || event == null || !level.isClientSide()) {
            return;
        }
        level.playLocalSound(x, y, z, event, source == null ? SoundSource.BLOCKS : source, volume, pitch, distanceDelay);
    }

    public static void playLegacyExplosion(Level level, Vec3 position) {
        if (position == null) {
            return;
        }
        playLegacyExplosion(level, position.x, position.y, position.z);
    }

    public static void playLegacyExplosion(Level level, double x, double y, double z) {
        playSoundEffect(level, x, y, z, "random.explode", SoundSource.BLOCKS, 4.0F,
                legacyExplosionPitch(level));
    }

    public static void playLegacyAmatExplosion(Level level, Vec3 position) {
        if (position == null) {
            return;
        }
        playLegacyAmatExplosion(level, position.x, position.y, position.z);
    }

    public static void playLegacyAmatExplosion(Level level, double x, double y, double z) {
        playSoundEffect(level, x, y, z, "random.explode", SoundSource.BLOCKS, 4.0F,
                legacyAmatExplosionPitch(level));
    }

    public static void playLegacyTinyExplosion(Level level, Vec3 position) {
        if (position == null) {
            return;
        }
        playSoundEffect(level, position, "hbm:weapon.explosionTiny", SoundSource.BLOCKS, 15.0F, 1.0F);
    }

    public static void playLegacyMukeExplosion(Level level, Vec3 position) {
        if (position == null) {
            return;
        }
        playLegacyMukeExplosion(level, position.x, position.y, position.z, 15.0F, 1.0F);
    }

    public static void playLegacyMukeExplosion(Level level, double x, double y, double z) {
        playLegacyMukeExplosion(level, x, y, z, 15.0F, 1.0F);
    }

    public static void playLegacyMukeExplosion(Level level, double x, double y, double z, float volume, float pitch) {
        playSoundEffect(level, x, y, z, "hbm:weapon.mukeExplosion", SoundSource.BLOCKS, volume, pitch);
    }

    public static void playLegacyUfoBlast(Level level, Vec3 position, float volume, float basePitch, float randomPitch) {
        if (position == null) {
            return;
        }
        playLegacyUfoBlast(level, position.x, position.y, position.z, volume, basePitch, randomPitch);
    }

    public static void playLegacyUfoBlast(Level level, double x, double y, double z,
            float volume, float basePitch, float randomPitch) {
        playSoundEffect(level, x, y, z, "hbm:entity.ufoBlast", SoundSource.BLOCKS, volume,
                randomPitch(level, basePitch, randomPitch));
    }

    public static void playLegacyFireworksBlast(Level level, Vec3 position, float volume, float pitch) {
        if (position == null) {
            return;
        }
        playSoundEffect(level, position, "fireworks.blast", SoundSource.BLOCKS, volume, pitch);
    }

    public static void playSoundClient(double x, double y, double z, String sound, float volume, float pitch) {
        playSoundClient(x, y, z, sound, SoundSource.BLOCKS, volume, pitch);
    }

    public static void playSoundClient(BlockPos pos, String sound, float volume, float pitch) {
        playSoundClient(pos, sound, SoundSource.BLOCKS, volume, pitch);
    }

    public static void playSoundClient(BlockPos pos, String sound, SoundSource source, float volume, float pitch) {
        if (pos == null) {
            return;
        }
        playSoundClient(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, sound, source, volume, pitch);
    }

    public static void playSoundClient(Vec3 position, String sound, float volume, float pitch) {
        playSoundClient(position, sound, SoundSource.BLOCKS, volume, pitch);
    }

    public static void playSoundClient(Vec3 position, String sound, SoundSource source, float volume, float pitch) {
        if (position == null) {
            return;
        }
        playSoundClient(position.x, position.y, position.z, sound, source, volume, pitch);
    }

    public static void playSoundClient(double x, double y, double z, String sound, SoundSource source,
            float volume, float pitch) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.hbm.ntm.client.sound.LegacyClientSoundPlayer.playSoundClient(x, y, z, sound,
                        source == null ? SoundSource.BLOCKS : source, volume, pitch));
    }

    public static void playSoundClientRandomPitch(Level level, double x, double y, double z, String sound,
            SoundSource source, float volume, float basePitch, float randomPitch) {
        playSoundClient(x, y, z, sound, source, volume, randomPitch(level, basePitch, randomPitch));
    }

    public static void playSoundClientRandomPitch(Level level, BlockPos pos, String sound,
            SoundSource source, float volume, float basePitch, float randomPitch) {
        if (pos == null) {
            return;
        }
        playSoundClientRandomPitch(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                sound, source, volume, basePitch, randomPitch);
    }

    public static void playSoundClientRandomPitch(Level level, Vec3 position, String sound,
            SoundSource source, float volume, float basePitch, float randomPitch) {
        if (position == null) {
            return;
        }
        playSoundClientRandomPitch(level, position.x, position.y, position.z, sound, source, volume, basePitch, randomPitch);
    }

    @Nullable
    public static SoundEvent resolveEvent(@Nullable String sound) {
        ResourceLocation location = LegacySoundIds.resolveLocation(sound);
        if (location == null) {
            return null;
        }
        SoundEvent registered = ForgeRegistries.SOUND_EVENTS.getValue(location);
        return registered == null ? SoundEvent.createVariableRangeEvent(location) : registered;
    }

    private static float randomPitch(@Nullable Level level, float basePitch, float randomPitch) {
        if (level == null || randomPitch == 0.0F) {
            return basePitch;
        }
        return basePitch + level.random.nextFloat() * randomPitch;
    }

    private static int clampGeigerLevel(int sound) {
        return Math.max(1, Math.min(6, sound));
    }

    private static float legacyExplosionPitch(@Nullable Level level) {
        if (level == null) {
            return 0.7F;
        }
        return (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F;
    }

    private static float legacyAmatExplosionPitch(@Nullable Level level) {
        if (level == null) {
            return 0.98F;
        }
        return (1.4F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F;
    }

    private LegacySoundPlayer() {
    }
}
