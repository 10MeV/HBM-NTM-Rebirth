package com.hbm.ntm.explosion.vnt;

import com.hbm.ntm.entity.item.LegacyPrimedExplosiveEntity;
import com.hbm.ntm.explosion.vnt.interfaces.BlockAllocator;
import com.hbm.ntm.explosion.vnt.interfaces.BlockProcessor;
import com.hbm.ntm.explosion.vnt.interfaces.EntityProcessor;
import com.hbm.ntm.explosion.vnt.interfaces.ExplosionEffect;
import com.hbm.ntm.explosion.vnt.interfaces.PlayerProcessor;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorStandard;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorWater;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.CustomDamageHandlerAmat;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectAmat;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectStandard;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.radiation.ModDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Map;

public class ExplosionVnt {
    private BlockAllocator blockAllocator;
    private EntityProcessor entityProcessor;
    private BlockProcessor blockProcessor;
    private PlayerProcessor playerProcessor;
    private ExplosionEffect[] effects;

    private final Level level;
    private final Vec3 position;
    private final float size;
    @Nullable
    private final Entity exploder;
    private final boolean fire;
    private final Explosion.BlockInteraction blockInteraction;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final Explosion compat;

    public ExplosionVnt(Level level, double x, double y, double z, float size) {
        this(level, x, y, z, size, null);
    }

    public ExplosionVnt(Level level, double x, double y, double z, float size, @Nullable Entity exploder) {
        this(level, x, y, z, size, exploder, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY);
    }

    public ExplosionVnt(Level level, double x, double y, double z, float size, @Nullable Entity exploder, boolean fire,
            Explosion.BlockInteraction blockInteraction) {
        this.level = level;
        this.position = new Vec3(x, y, z);
        this.size = size;
        this.exploder = exploder;
        this.fire = fire;
        this.blockInteraction = blockInteraction;
        this.damageSource = ModDamageSources.explosion(level, exploder);
        this.damageCalculator = exploder == null ? new ExplosionDamageCalculator() : new EntityBasedExplosionDamageCalculator(exploder);
        this.compat = new Explosion(level, exploder, damageSource, damageCalculator, x, y, z, size, fire, blockInteraction);
    }

    public void explode() {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        level.gameEvent(exploder, GameEvent.EXPLODE, position);

        boolean processBlocks = blockAllocator != null && blockProcessor != null && blockInteraction != Explosion.BlockInteraction.KEEP;
        boolean processEntities = entityProcessor != null && playerProcessor != null;
        LinkedHashSet<BlockPos> affectedBlocks = new LinkedHashSet<>();
        Map<net.minecraft.world.entity.player.Player, Vec3> affectedPlayers = Map.of();

        if (processBlocks) {
            affectedBlocks.addAll(blockAllocator.allocate(this, serverLevel, position, size));
            compat.getToBlow().addAll(affectedBlocks);
        }
        if (processEntities) {
            affectedPlayers = entityProcessor.process(this, serverLevel, position, size);
            compat.getHitPlayers().putAll(affectedPlayers);
        }
        if (processBlocks) {
            blockProcessor.process(this, serverLevel, position, affectedBlocks);
        }
        if (fire) {
            BlockProcessorStandard.placeFire(level, affectedBlocks);
        }
        if (processEntities) {
            playerProcessor.process(this, serverLevel, position, affectedPlayers);
        }
        if (effects != null) {
            for (ExplosionEffect effect : effects) {
                effect.doEffect(this, serverLevel, position, size, affectedBlocks);
            }
        }
    }

    public ExplosionVnt setBlockAllocator(BlockAllocator blockAllocator) {
        this.blockAllocator = blockAllocator;
        return this;
    }

    public ExplosionVnt setEntityProcessor(EntityProcessor entityProcessor) {
        this.entityProcessor = entityProcessor;
        return this;
    }

    public ExplosionVnt setBlockProcessor(BlockProcessor blockProcessor) {
        this.blockProcessor = blockProcessor;
        return this;
    }

    public ExplosionVnt setPlayerProcessor(PlayerProcessor playerProcessor) {
        this.playerProcessor = playerProcessor;
        return this;
    }

    public ExplosionVnt setEffects(ExplosionEffect... effects) {
        this.effects = effects;
        return this;
    }

    public ExplosionVnt setSFX(ExplosionEffect... effects) {
        return setEffects(effects);
    }

    public ExplosionVnt makeStandard() {
        return setBlockAllocator(new BlockAllocatorStandard())
                .setBlockProcessor(new BlockProcessorStandard())
                .setEntityProcessor(new EntityProcessorStandard())
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setEffects(new ExplosionEffectStandard());
    }

    public ExplosionVnt makeUnderwater(int resolution) {
        return makeStandard().setBlockAllocator(new BlockAllocatorWater(resolution));
    }

    public ExplosionVnt makeAmat() {
        return setBlockAllocator(new BlockAllocatorStandard(size < 15.0F ? 16 : 32))
                .setBlockProcessor(new BlockProcessorStandard().setNoDrop())
                .setEntityProcessor(new EntityProcessorStandard()
                        .withRangeMod(2.0F)
                        .withDamageMod(new CustomDamageHandlerAmat(50.0F)))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setEffects(new ExplosionEffectAmat());
    }

    public Map<net.minecraft.world.entity.player.Player, Vec3> func_77277_b() {
        return compat.getHitPlayers();
    }

    public Level level() {
        return level;
    }

    public Vec3 position() {
        return position;
    }

    public float size() {
        return size;
    }

    @Nullable
    public Entity exploder() {
        return exploder;
    }

    public Explosion.BlockInteraction blockInteraction() {
        return blockInteraction;
    }

    public DamageSource damageSource() {
        return damageSource;
    }

    public ExplosionDamageCalculator damageCalculator() {
        return damageCalculator;
    }

    public Explosion compat() {
        return compat;
    }

    @Nullable
    public LivingEntity indirectSourceEntity() {
        if (exploder instanceof LegacyPrimedExplosiveEntity legacyPrimed) {
            return legacyPrimed.getTntPlacedBy();
        }
        if (exploder instanceof PrimedTnt primedTnt) {
            return primedTnt.getOwner();
        }
        if (exploder instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        if (exploder instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }
}
