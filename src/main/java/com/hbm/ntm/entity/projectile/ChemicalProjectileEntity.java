package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidBlockImpactEffects;
import com.hbm.ntm.fluid.HbmFluidContactEffects;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.util.EnchantmentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import java.util.List;

public class ChemicalProjectileEntity extends LegacyThrowableEntity {
    private static final EntityDataAccessor<Integer> FLUID_ID =
            SynchedEntityData.defineId(ChemicalProjectileEntity.class, EntityDataSerializers.INT);

    public ChemicalProjectileEntity(EntityType<? extends ChemicalProjectileEntity> type, Level level) {
        super(type, level);
        noPhysics = false;
    }

    public ChemicalProjectileEntity(Level level) {
        this(ModEntityTypes.CHEMICAL_PROJECTILE.get(), level);
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && tickCount > maxAge()) {
            discard();
            return;
        }
        if (!level().isClientSide() && (style() == ChemicalStyle.GAS || style() == ChemicalStyle.GASFLAME)) {
            float intensity = 1.0F - (float) tickCount / (float) Math.max(1, maxAge());
            AABB area = getBoundingBox().inflate(intensity * 2.5F);
            List<Entity> affected = level().getEntities(this, area, entity -> entity.isAlive() && entity != getOwner());
            for (Entity entity : affected) {
                affect(entity, intensity);
            }
        }
        if (level().isClientSide()) {
            spawnClientParticles();
        }
        super.tick();
    }

    public void setFluid(FluidType fluid) {
        entityData.set(FLUID_ID, fluid == null ? HbmFluids.NONE.getId() : fluid.getId());
    }

    public FluidType fluid() {
        return HbmFluids.fromId(entityData.get(FLUID_ID));
    }

    public ChemicalStyle style() {
        return styleFromType(fluid());
    }

    public int maxAge() {
        return switch (style()) {
            case AMAT -> 100;
            case LIGHTNING -> 5;
            case BURNING -> 600;
            case GAS -> 60;
            case GASFLAME -> 20;
            case LIQUID -> 600;
            case NULL -> 100;
        };
    }

    private void spawnClientParticles() {
        FluidType type = fluid();
        ChemicalStyle currentStyle = style();
        if (type == HbmFluids.NONE) {
            return;
        }
        if (type == HbmFluids.BALEFIRE) {
            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() - 0.125D, getZ(),
                    0.0D, 0.0D, 0.0D);
        } else if (currentStyle == ChemicalStyle.LIQUID) {
            int color = type.getColor();
            float red = ((color >> 16) & 255) / 255.0F;
            float green = ((color >> 8) & 255) / 255.0F;
            float blue = (color & 255) / 255.0F;
            Vec3 motion = getDeltaMovement();
            level().addParticle(new DustParticleOptions(new Vector3f(red, green, blue), 0.75F),
                    getX(), getY(), getZ(),
                    motion.x + random.nextGaussian() * 0.05D,
                    motion.y - 0.2D + random.nextGaussian() * 0.05D,
                    motion.z + random.nextGaussian() * 0.05D);
        } else if (currentStyle == ChemicalStyle.BURNING) {
            level().addParticle(ParticleTypes.FLAME, getX(), getY() - 0.125D, getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    private void affect(Entity entity, float intensity) {
        ChemicalStyle currentStyle = style();
        FluidType type = fluid();
        float adjustedIntensity = currentStyle == ChemicalStyle.LIQUID || currentStyle == ChemicalStyle.BURNING
                ? 1.0F
                : intensity;
        LivingEntity living = entity instanceof LivingEntity livingEntity ? livingEntity : null;

        if (currentStyle == ChemicalStyle.AMAT) {
            EntityDamageUtil.attackEntityFromIgnoreIFrame(entity, ModDamageSources.radiation(level()), 1.0F);
            if (living != null) {
                RadiationUtil.contaminate(living, 50.0F * adjustedIntensity, true);
                return;
            }
        }
        if (currentStyle == ChemicalStyle.LIGHTNING) {
            EntityDamageUtil.attackEntityFromIgnoreIFrame(entity,
                    ModDamageSources.source(level(), ModDamageSources.ELECTRICITY), 0.5F);
            if (living != null) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9));
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 9));
                return;
            }
        }

        HbmFluidContactEffects.affectChemicalProjectile(type, entity, adjustedIntensity,
                mapContactStyle(currentStyle), this, getOwner());

        if (type == HbmFluids.XPJUICE && entity instanceof Player player) {
            EnchantmentUtil.addExperience(player, 1, false);
            discard();
        }
        if (type == HbmFluids.ENDERJUICE) {
            teleportRandomly(entity);
        }

        if (isExtinguishing()) {
            entity.clearFire();
        }
    }

    private static HbmFluidContactEffects.ChemicalContactStyle mapContactStyle(ChemicalStyle style) {
        return switch (style) {
            case AMAT -> HbmFluidContactEffects.ChemicalContactStyle.AMAT;
            case LIGHTNING -> HbmFluidContactEffects.ChemicalContactStyle.LIGHTNING;
            case LIQUID -> HbmFluidContactEffects.ChemicalContactStyle.LIQUID;
            case GAS -> HbmFluidContactEffects.ChemicalContactStyle.GAS;
            case GASFLAME -> HbmFluidContactEffects.ChemicalContactStyle.GASFLAME;
            case BURNING -> HbmFluidContactEffects.ChemicalContactStyle.BURNING;
            case NULL -> HbmFluidContactEffects.ChemicalContactStyle.NULL;
        };
    }

    private boolean isExtinguishing() {
        FluidType type = fluid();
        return style() == ChemicalStyle.LIQUID
                && type.getTemperature() < 50
                && !type.hasTrait(FlammableFluidTrait.class);
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (level().isClientSide()) {
            return;
        }
        float intensity = 1.0F - (float) tickCount / (float) Math.max(1, maxAge());
        if (hit instanceof EntityHitResult entityHit) {
            affect(entityHit.getEntity(), intensity);
        } else if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = new BlockPos(hitBlockX(hit), hitBlockY(hit), hitBlockZ(hit));
            HbmFluidBlockImpactEffects.applyChemicalProjectileImpact(level(), pos, fluid(), mapImpactStyle(style()));
            discard();
        }
    }

    private static HbmFluidBlockImpactEffects.ChemicalImpactStyle mapImpactStyle(ChemicalStyle style) {
        return switch (style) {
            case AMAT -> HbmFluidBlockImpactEffects.ChemicalImpactStyle.AMAT;
            case LIGHTNING -> HbmFluidBlockImpactEffects.ChemicalImpactStyle.LIGHTNING;
            case LIQUID -> HbmFluidBlockImpactEffects.ChemicalImpactStyle.LIQUID;
            case GAS -> HbmFluidBlockImpactEffects.ChemicalImpactStyle.GAS;
            case GASFLAME -> HbmFluidBlockImpactEffects.ChemicalImpactStyle.GASFLAME;
            case BURNING -> HbmFluidBlockImpactEffects.ChemicalImpactStyle.BURNING;
            case NULL -> HbmFluidBlockImpactEffects.ChemicalImpactStyle.NULL;
        };
    }

    private void teleportRandomly(Entity entity) {
        double targetX = getX() + (random.nextDouble() - 0.5D) * 64.0D;
        double targetY = Mth.clamp(getY() + random.nextInt(64) - 32.0D,
                level().getMinBuildHeight() + 1.0D, level().getMaxBuildHeight() - 1.0D);
        double targetZ = getZ() + (random.nextDouble() - 0.5D) * 64.0D;
        teleportTo(entity, targetX, targetY, targetZ);
    }

    private boolean teleportTo(Entity entity, double x, double y, double z) {
        double startX = entity.getX();
        double startY = entity.getY();
        double startZ = entity.getZ();
        double targetY = Mth.clamp(y, level().getMinBuildHeight() + 1.0D, level().getMaxBuildHeight() - 1.0D);
        entity.setPos(x, targetY, z);
        boolean valid = false;
        BlockPos cursor = BlockPos.containing(entity.getX(), entity.getY(), entity.getZ());

        if (level().isLoaded(cursor)) {
            while (cursor.getY() > level().getMinBuildHeight()) {
                BlockPos below = cursor.below();
                if (level().getBlockState(below).blocksMotion()) {
                    valid = true;
                    break;
                }
                entity.setPos(entity.getX(), entity.getY() - 1.0D, entity.getZ());
                cursor = cursor.below();
            }

            if (valid && (!level().noCollision(entity) || level().containsAnyLiquid(entity.getBoundingBox()))) {
                valid = false;
            }
        }

        if (!valid) {
            entity.setPos(startX, startY, startZ);
            return false;
        }

        if (level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 32; i++) {
                double progress = i / 31.0D;
                double px = Mth.lerp(progress, startX, entity.getX())
                        + (random.nextDouble() - 0.5D) * entity.getBbWidth() * 2.0D;
                double py = Mth.lerp(progress, startY, entity.getY()) + random.nextDouble() * entity.getBbHeight();
                double pz = Mth.lerp(progress, startZ, entity.getZ())
                        + (random.nextDouble() - 0.5D) * entity.getBbWidth() * 2.0D;
                serverLevel.sendParticles(ParticleTypes.PORTAL, px, py, pz, 1,
                        (random.nextFloat() - 0.5F) * 0.2F,
                        (random.nextFloat() - 0.5F) * 0.2F,
                        (random.nextFloat() - 0.5F) * 0.2F, 0.0D);
            }
        }
        level().playSound(null, startX, startY, startZ, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE, 1.0F, 1.0F);
        entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return true;
    }

    @Override
    protected float getAirDrag() {
        return switch (style()) {
            case AMAT, LIGHTNING -> 1.0F;
            case GAS -> 0.95F;
            default -> 0.99F;
        };
    }

    @Override
    protected float getWaterDrag() {
        return switch (style()) {
            case AMAT, LIGHTNING, GAS -> 1.0F;
            default -> 0.8F;
        };
    }

    @Override
    protected double getGravityVelocity() {
        return switch (style()) {
            case AMAT, LIGHTNING, GAS -> 0.0D;
            case GASFLAME -> -0.01D;
            default -> 0.03D;
        };
    }

    @Override
    protected int groundDespawn() {
        return 0;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(FLUID_ID, HbmFluids.NONE.getId());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setFluid(HbmFluids.fromId(tag.getInt("fluid")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("fluid", fluid().getId());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static ChemicalStyle styleFromType(FluidType type) {
        if (type == HbmFluids.IONGEL) {
            return ChemicalStyle.LIGHTNING;
        }
        if (type.isAntimatter()) {
            return ChemicalStyle.AMAT;
        }
        if (type.hasTrait(SimpleFluidTraits.Gaseous.class) || type.hasTrait(SimpleFluidTraits.GaseousAtRoomTemperature.class)) {
            return type.hasTrait(FlammableFluidTrait.class) || type.hasTrait(CombustibleFluidTrait.class)
                    ? ChemicalStyle.GASFLAME
                    : ChemicalStyle.GAS;
        }
        if (type.hasTrait(SimpleFluidTraits.Liquid.class)) {
            return type.hasTrait(CombustibleFluidTrait.class)
                    ? ChemicalStyle.BURNING
                    : ChemicalStyle.LIQUID;
        }
        return ChemicalStyle.NULL;
    }

    public enum ChemicalStyle {
        AMAT,
        LIGHTNING,
        LIQUID,
        GAS,
        GASFLAME,
        BURNING,
        NULL
    }
}
