package com.hbm.ntm.client.sound;

import com.hbm.ntm.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class LegacyMovingEntitySound extends AbstractSoundInstance implements TickableSoundInstance {
    private static final Map<Key, LegacyMovingEntitySound> ACTIVE_BY_ENTITY = new HashMap<>();

    private final Entity entity;
    private final LoopType type;
    private final String loopKey;
    private final Predicate<Entity> keepPlaying;
    private boolean stopped;

    public LegacyMovingEntitySound(ResourceLocation location, Entity entity, LoopType type, SoundSource source,
            Predicate<Entity> keepPlaying) {
        this(location, entity, type, typeKey(type), source, keepPlaying);
    }

    public LegacyMovingEntitySound(ResourceLocation location, Entity entity, String loopKey, SoundSource source,
            Predicate<Entity> keepPlaying) {
        this(location, entity, null, loopKey, source, keepPlaying);
    }

    private LegacyMovingEntitySound(ResourceLocation location, Entity entity, LoopType type, String loopKey,
            SoundSource source, Predicate<Entity> keepPlaying) {
        super(location, source, RandomSource.create());
        this.entity = entity;
        this.type = type;
        this.loopKey = normalizeLoopKey(loopKey, location);
        this.keepPlaying = keepPlaying == null ? Entity::isAlive : keepPlaying;
        this.looping = true;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        updatePosition();
    }

    @Override
    public void tick() {
        if (entity == null || entity.isRemoved() || !entity.isAlive() || !keepPlaying.test(entity)) {
            stopSound();
            return;
        }
        updatePosition();
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    public void start() {
        Minecraft.getInstance().getSoundManager().play(this);
    }

    public void stopSound() {
        if (stopped) {
            return;
        }
        this.stopped = true;
        this.looping = false;
        ACTIVE_BY_ENTITY.remove(Key.of(entity, loopKey), this);
        Minecraft.getInstance().getSoundManager().stop(this);
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public Entity getEntity() {
        return entity;
    }

    public LoopType getType() {
        return type;
    }

    public String getLoopKey() {
        return loopKey;
    }

    public static LegacyMovingEntitySound getSoundByEntity(Entity entity, LoopType type) {
        return getSoundByEntity(entity, typeKey(type));
    }

    public static LegacyMovingEntitySound getSoundByEntity(Entity entity, String loopKey) {
        pruneStopped();
        return ACTIVE_BY_ENTITY.get(Key.of(entity, loopKey));
    }

    public static LegacyMovingEntitySound startForEntity(ResourceLocation location, Entity entity, LoopType type,
            SoundSource source, float volume, float pitch, Predicate<Entity> keepPlaying) {
        if (type == null) {
            return null;
        }
        return startForEntity(location, entity, type, typeKey(type), source, volume, pitch, keepPlaying);
    }

    public static LegacyMovingEntitySound startForEntity(ResourceLocation location, Entity entity, String loopKey,
            SoundSource source, float volume, float pitch, Predicate<Entity> keepPlaying) {
        return startForEntity(location, entity, null, loopKey, source, volume, pitch, keepPlaying);
    }

    private static LegacyMovingEntitySound startForEntity(ResourceLocation location, Entity entity, LoopType type,
            String loopKey, SoundSource source, float volume, float pitch, Predicate<Entity> keepPlaying) {
        if (location == null || entity == null) {
            return null;
        }
        String normalizedLoopKey = normalizeLoopKey(loopKey, location);
        pruneStopped();
        Key key = Key.of(entity, normalizedLoopKey);
        LegacyMovingEntitySound sound = ACTIVE_BY_ENTITY.get(key);
        if (sound != null && !sound.isStopped()) {
            sound.setVolume(volume);
            sound.setPitch(pitch);
            return sound;
        }
        sound = new LegacyMovingEntitySound(location, entity, type, normalizedLoopKey,
                source == null ? SoundSource.HOSTILE : source, keepPlaying);
        sound.setVolume(volume);
        sound.setPitch(pitch);
        ACTIVE_BY_ENTITY.put(key, sound);
        sound.start();
        return sound;
    }

    public static void stop(Entity entity, LoopType type) {
        stop(entity, typeKey(type));
    }

    public static void stop(Entity entity, String loopKey) {
        LegacyMovingEntitySound sound = getSoundByEntity(entity, loopKey);
        if (sound != null) {
            sound.stopSound();
        }
    }

    public static LegacyMovingEntitySound startChopperFlying(Entity entity, Predicate<Entity> keepPlaying) {
        return startForEntity(ModSounds.ENTITY_CHOPPER_FLYING_LOOP.get().getLocation(), entity,
                LoopType.SOUND_CHOPPER_LOOP, SoundSource.HOSTILE, 10.0F, 1.0F, keepPlaying);
    }

    public static LegacyMovingEntitySound startChopperCrashing(Entity entity, Predicate<Entity> keepPlaying) {
        return startForEntity(ModSounds.ENTITY_CHOPPER_CRASHING_LOOP.get().getLocation(), entity,
                LoopType.SOUND_CRASHING_LOOP, SoundSource.HOSTILE, 10.0F, 1.0F, keepPlaying);
    }

    public static LegacyMovingEntitySound startChopperMine(Entity entity, Predicate<Entity> keepPlaying) {
        return startForEntity(ModSounds.ENTITY_CHOPPER_MINE_LOOP.get().getLocation(), entity,
                LoopType.SOUND_MINE_LOOP, SoundSource.HOSTILE, 10.0F, 1.0F, keepPlaying);
    }

    private static void pruneStopped() {
        Iterator<Map.Entry<Key, LegacyMovingEntitySound>> iterator = ACTIVE_BY_ENTITY.entrySet().iterator();
        while (iterator.hasNext()) {
            LegacyMovingEntitySound sound = iterator.next().getValue();
            if (sound.isStopped() || sound.entity == null || sound.entity.isRemoved() || !sound.entity.isAlive()) {
                iterator.remove();
            }
        }
    }

    public static void clearAll() {
        for (LegacyMovingEntitySound sound : List.copyOf(ACTIVE_BY_ENTITY.values())) {
            sound.stopSound();
        }
        ACTIVE_BY_ENTITY.clear();
    }

    public static int activeCount() {
        pruneStopped();
        return ACTIVE_BY_ENTITY.size();
    }

    private void updatePosition() {
        if (entity != null) {
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
        }
    }

    private static String typeKey(LoopType type) {
        return type == null ? "" : type.name();
    }

    private static String normalizeLoopKey(String loopKey, ResourceLocation fallbackLocation) {
        String key = loopKey == null ? "" : loopKey.trim();
        if (key.isEmpty() && fallbackLocation != null) {
            key = fallbackLocation.toString();
        }
        return key.toLowerCase(Locale.ROOT);
    }

    private record Key(int entityId, String loopKey) {
        static Key of(Entity entity, String loopKey) {
            return new Key(entity == null ? Integer.MIN_VALUE : entity.getId(),
                    normalizeLoopKey(loopKey, null));
        }
    }

    public enum LoopType {
        SOUND_TAU_LOOP,
        SOUND_CHOPPER_LOOP,
        SOUND_CRASHING_LOOP,
        SOUND_MINE_LOOP
    }
}
