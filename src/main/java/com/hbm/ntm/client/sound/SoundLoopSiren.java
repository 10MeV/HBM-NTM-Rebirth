package com.hbm.ntm.client.sound;

import com.hbm.ntm.sound.LegacySirenTrack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class SoundLoopSiren extends SoundLoopMachine {
    private static final Map<BlockPos, SoundLoopSiren> ACTIVE_BY_POS = new HashMap<>();

    private final BlockEntity blockEntity;
    private LegacySirenTrack track;
    private float intendedVolume;
    private int age;

    public SoundLoopSiren(LegacySirenTrack track, BlockEntity blockEntity) {
        super(track.eventLocation(), blockEntity, SoundSource.RECORDS);
        this.blockEntity = blockEntity;
        setTrack(track);
        BlockPos pos = blockEntity.getBlockPos();
        setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (isStopped()) {
            ACTIVE_BY_POS.remove(blockEntity.getBlockPos(), this);
            return;
        }

        setLooping(track.type() == LegacySirenTrack.SoundType.LOOP);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            double dx = x - player.getX();
            double dy = y - player.getY();
            double dz = z - player.getZ();
            this.volume = volumeForDistance((float) Math.sqrt(dx * dx + dy * dy + dz * dz));
        } else {
            this.volume = intendedVolume;
        }

        if (track.type() != LegacySirenTrack.SoundType.LOOP && age > 2 && !isPlaying()) {
            requestStop();
            ACTIVE_BY_POS.remove(blockEntity.getBlockPos(), this);
        }
    }

    public static void handleClientTileEvent(BlockEntity blockEntity, CompoundTag data) {
        handleSirenEvent(blockEntity, data.getInt("trackId"), data.getBoolean("active"));
    }

    public static void handleSirenEvent(BlockEntity blockEntity, int trackId, boolean active) {
        if (blockEntity == null) {
            return;
        }
        pruneStopped();

        BlockPos pos = blockEntity.getBlockPos();
        SoundLoopSiren sound = ACTIVE_BY_POS.get(pos);
        if (sound != null && (sound.blockEntity != blockEntity || sound.isStopped() || (sound.age > 2 && !sound.isPlaying()))) {
            sound.requestStop();
            ACTIVE_BY_POS.remove(pos);
            sound = null;
        }

        if (!active) {
            if (sound != null) {
                sound.endSound();
                ACTIVE_BY_POS.remove(pos);
            }
            return;
        }

        LegacySirenTrack track = LegacySirenTrack.byId(trackId);
        if (!track.hasSound()) {
            if (sound != null) {
                sound.endSound();
                ACTIVE_BY_POS.remove(pos);
            }
            return;
        }

        if (sound == null) {
            start(track, blockEntity);
        } else if (sound.track != track) {
            sound.endSound();
            start(track, blockEntity);
        } else {
            sound.setTrack(track);
        }
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    public LegacySirenTrack getTrack() {
        return track;
    }

    public void endSound() {
        requestStop();
    }

    private static void start(LegacySirenTrack track, BlockEntity blockEntity) {
        SoundLoopSiren sound = new SoundLoopSiren(track, blockEntity);
        ACTIVE_BY_POS.put(blockEntity.getBlockPos(), sound);
        sound.start();
    }

    private static void pruneStopped() {
        Iterator<Map.Entry<BlockPos, SoundLoopSiren>> iterator = ACTIVE_BY_POS.entrySet().iterator();
        while (iterator.hasNext()) {
            SoundLoopSiren sound = iterator.next().getValue();
            if (sound.isStopped() || (sound.age > 2 && !sound.isPlaying())) {
                iterator.remove();
            }
        }
    }

    public static void clearAll() {
        for (SoundLoopSiren sound : ACTIVE_BY_POS.values()) {
            sound.requestStop();
        }
        ACTIVE_BY_POS.clear();
    }

    private void setTrack(LegacySirenTrack track) {
        this.track = track;
        this.intendedVolume = Math.max(0.0F, track.volume());
        setLooping(track.type() == LegacySirenTrack.SoundType.LOOP);
    }

    private float volumeForDistance(float distance) {
        if (intendedVolume <= 0.0F) {
            return 0.0F;
        }
        return Math.max(0.0F, (distance / intendedVolume) * -2.0F + 2.0F);
    }
}
