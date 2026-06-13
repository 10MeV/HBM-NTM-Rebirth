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
