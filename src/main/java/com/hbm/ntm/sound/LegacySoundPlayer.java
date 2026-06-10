package com.hbm.ntm.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public final class LegacySoundPlayer {
    public static void playSoundEffect(Level level, double x, double y, double z, String sound, float volume, float pitch) {
        playSoundEffect(level, x, y, z, sound, SoundSource.BLOCKS, volume, pitch);
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

    public static void playSoundClient(double x, double y, double z, String sound, float volume, float pitch) {
        playSoundClient(x, y, z, sound, SoundSource.BLOCKS, volume, pitch);
    }

    public static void playSoundClient(double x, double y, double z, String sound, SoundSource source,
            float volume, float pitch) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.hbm.ntm.client.sound.LegacyClientSoundPlayer.playSoundClient(x, y, z, sound,
                        source == null ? SoundSource.BLOCKS : source, volume, pitch));
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

    private LegacySoundPlayer() {
    }
}
