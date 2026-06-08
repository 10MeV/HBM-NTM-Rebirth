package com.hbm.ntm.entity.logic;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.config.BombConfig;
import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.entity.effect.FalloutRainEntity;
import com.hbm.ntm.explosion.ExplosionFleija;
import com.hbm.ntm.explosion.ExplosionHurtUtil;
import com.hbm.ntm.explosion.ExplosionNukeAdvanced;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.explosion.ExplosionSolinium;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class NukeExplosionMk3Entity extends ExplosionChunkLoadingEntity {
    public static final int EXT_FLEIJA = 0;
    public static final int EXT_SOLINIUM = 1;
    private static final Map<ATEntry, Long> ANTI_TELEPORT_ENTRIES = new HashMap<>();

    private int age;
    private int destructionRange;
    private ExplosionNukeAdvanced exp;
    private ExplosionNukeAdvanced wst;
    private ExplosionNukeAdvanced vap;
    private ExplosionFleija fleija;
    private ExplosionSolinium solinium;
    private int speed = 1;
    private float coefficient = 1.0F;
    private float coefficient2 = 1.0F;
    private boolean initialized;
    private boolean spawnedFallout;
    private boolean waste = true;
    private int extType = EXT_FLEIJA;
    private boolean expiredFromSave;

    public NukeExplosionMk3Entity(EntityType<? extends NukeExplosionMk3Entity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public NukeExplosionMk3Entity(Level level) {
        this(ModEntityTypes.NUKE_EXPLOSION_MK3.get(), level);
    }

    public static NukeExplosionMk3Entity createFleija(Level level, double x, double y, double z, int range) {
        NukeExplosionMk3Entity entity = new NukeExplosionMk3Entity(level);
        entity.setPos(x, y, z);
        entity.destructionRange = range;
        entity.speed = BombConfig.BLAST_SPEED.get();
        entity.coefficient = 1.0F;
        entity.waste = false;
        entity.extType = EXT_FLEIJA;
        return entity;
    }

    public static NukeExplosionMk3Entity createWaste(Level level, double x, double y, double z, int range) {
        NukeExplosionMk3Entity entity = new NukeExplosionMk3Entity(level);
        entity.setPos(x, y, z);
        entity.destructionRange = range;
        entity.speed = BombConfig.BLAST_SPEED.get();
        entity.coefficient = 1.0F;
        entity.waste = true;
        return entity;
    }

    public static NukeExplosionMk3Entity createSolinium(Level level, double x, double y, double z, int range) {
        return statFacFleija(level, x, y, z, range).makeSol();
    }

    public static NukeExplosionMk3Entity statFacFleija(Level level, double x, double y, double z, int range) {
        NukeExplosionMk3Entity entity = createFleija(level, x, y, z, range);
        if (isFleijaInterrupted(level, x, y, z)) {
            entity.discard();
        }
        return entity;
    }

    public static void registerAntiTeleportEntry(Level level, int x, int y, int z, int ticks) {
        if (level == null || ticks <= 0) {
            return;
        }
        ANTI_TELEPORT_ENTRIES.put(new ATEntry(level.dimension(), x, y, z), level.getGameTime() + ticks);
    }

    public NukeExplosionMk3Entity makeSol() {
        extType = EXT_SOLINIUM;
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }

        if (expiredFromSave) {
            discard();
            return;
        }

        if (destructionRange <= 0) {
            discard();
            return;
        }

        forceCenterChunk();

        if (!initialized) {
            if (extendedLoggingEnabled()) {
                HbmNtm.LOGGER.info("[NUKE] Initialized mk3 explosion at {} / {} / {} with strength {}!",
                        getX(), getY(), getZ(), destructionRange);
            }
            initProcessors();
            initialized = true;
        }

        speed += 1;
        boolean destructionComplete = false;
        boolean finalComplete = false;

        for (int i = 0; i < speed; i++) {
            if (waste) {
                destructionComplete = exp.update();
                wst.update();
                finalComplete = vap.update();
                if (finalComplete) {
                    discard();
                    break;
                }
            } else if (extType == EXT_SOLINIUM) {
                if (solinium.update()) {
                    discard();
                    break;
                }
            } else if (fleija.update()) {
                discard();
                break;
            }
        }

        if (!destructionComplete) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER,
                    10000.0F, 0.8F + random.nextFloat() * 0.2F);
            if (waste || extType != EXT_SOLINIUM) {
                ExplosionNukeGeneric.dealDamage(level(), getX(), getY(), getZ(), destructionRange * 2.0D);
            } else {
                ExplosionHurtUtil.doRadiation(level(), getX(), getY(), getZ(), 15000.0F, 250000.0F, destructionRange);
            }
        } else if (!spawnedFallout && waste) {
            level().addFreshEntity(FalloutRainEntity.create(level(), getX(), getY(), getZ(), Math.max(1, (int) (destructionRange * 1.8D))));
            spawnedFallout = true;
        }

        age++;
    }

    private void initProcessors() {
        int x = (int) getX();
        int y = (int) getY();
        int z = (int) getZ();
        if (waste) {
            exp = new ExplosionNukeAdvanced(x, y, z, level(), destructionRange, coefficient, ExplosionNukeAdvanced.TYPE_DESTRUCTION);
            wst = new ExplosionNukeAdvanced(x, y, z, level(), (int) (destructionRange * 1.8D), coefficient, ExplosionNukeAdvanced.TYPE_WASTE);
            vap = new ExplosionNukeAdvanced(x, y, z, level(), (int) (destructionRange * 2.5D), coefficient, ExplosionNukeAdvanced.TYPE_VAPOR);
        } else if (extType == EXT_SOLINIUM) {
            solinium = new ExplosionSolinium(x, y, z, level(), destructionRange, coefficient, coefficient2);
        } else {
            fleija = new ExplosionFleija(x, y, z, level(), destructionRange, coefficient, coefficient2);
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getInt("age");
        destructionRange = tag.getInt("destructionRange");
        speed = Math.max(1, tag.getInt("speed"));
        coefficient = tag.getFloat("coefficient");
        if (coefficient == 0.0F) {
            coefficient = 1.0F;
        }
        coefficient2 = tag.getFloat("coefficient2");
        if (coefficient2 == 0.0F) {
            coefficient2 = 1.0F;
        }
        initialized = tag.getBoolean("did");
        spawnedFallout = tag.getBoolean("did2");
        waste = !tag.contains("waste") || tag.getBoolean("waste");
        extType = tag.getInt("extType");
        readChunkLoader(tag);
        expiredFromSave = shouldExpireFromSave(tag);

        if (initialized && !expiredFromSave) {
            initProcessors();
            if (exp != null) {
                exp.readFromNbt(tag, "exp_");
            }
            if (wst != null) {
                wst.readFromNbt(tag, "wst_");
            }
            if (vap != null) {
                vap.readFromNbt(tag, "vap_");
            }
            if (fleija != null) {
                fleija.readFromNbt(tag, "expl_");
            }
            if (solinium != null) {
                solinium.readFromNbt(tag, "sol_");
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("age", age);
        tag.putInt("destructionRange", destructionRange);
        tag.putInt("speed", speed);
        tag.putFloat("coefficient", coefficient);
        tag.putFloat("coefficient2", coefficient2);
        tag.putBoolean("did", initialized);
        tag.putBoolean("did2", spawnedFallout);
        tag.putBoolean("waste", waste);
        tag.putInt("extType", extType);
        tag.putLong("milliTime", System.currentTimeMillis());
        saveChunkLoader(tag);

        if (exp != null) {
            exp.saveToNbt(tag, "exp_");
        }
        if (wst != null) {
            wst.saveToNbt(tag, "wst_");
        }
        if (vap != null) {
            vap.saveToNbt(tag, "vap_");
        }
        if (fleija != null) {
            fleija.saveToNbt(tag, "expl_");
        }
        if (solinium != null) {
            solinium.saveToNbt(tag, "sol_");
        }
    }

    private static boolean extendedLoggingEnabled() {
        return HbmCommonConfig.ENABLE_EXTENDED_LOGGING != null && HbmCommonConfig.ENABLE_EXTENDED_LOGGING.get();
    }

    private static boolean isFleijaInterrupted(Level level, double x, double y, double z) {
        if (level == null || level.isClientSide()) {
            return false;
        }

        long gameTime = level.getGameTime();
        Iterator<Map.Entry<ATEntry, Long>> iterator = ANTI_TELEPORT_ENTRIES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ATEntry, Long> next = iterator.next();
            if (next.getValue() < gameTime) {
                iterator.remove();
                continue;
            }

            ATEntry entry = next.getKey();
            if (!entry.dimension.equals(level.dimension())) {
                continue;
            }

            double dx = x - entry.x;
            double dy = y - entry.y;
            double dz = z - entry.z;
            if (Math.sqrt(dx * dx + dy * dy + dz * dz) < 300.0D) {
                playInterruptionEffect(level, x, y, z);
                playInterruptionEffect(level, entry.x + 0.5D, entry.y + 0.5D, entry.z + 0.5D);
                return true;
            }
        }
        return false;
    }

    private static void playInterruptionEffect(Level level, double x, double y, double z) {
        level.playSound(null, x, y, z, ModSounds.ENTITY_UFO_BLAST.get(), SoundSource.BLOCKS,
                15.0F, 0.7F + level.random.nextFloat() * 0.2F);
        ParticleUtil.spawnPlasmaBlast(level, x, y, z, 0.0F, 0.75F, 1.0F, 0.0F, 0.0F, 7.5F);
    }

    public static final class ATEntry {
        private final ResourceKey<Level> dimension;
        private final int x;
        private final int y;
        private final int z;

        public ATEntry(ResourceKey<Level> dimension, int x, int y, int z) {
            this.dimension = Objects.requireNonNull(dimension);
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ATEntry other)) {
                return false;
            }
            return x == other.x && y == other.y && z == other.z && dimension.equals(other.dimension);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dimension, x, y, z);
        }
    }
}
