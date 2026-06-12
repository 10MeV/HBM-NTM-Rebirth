package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import com.hbm.ntm.fluid.trait.VentRadiationFluidTrait;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
    private static final int RELEASE_AMOUNT_MB = 5;

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

        if (currentStyle == ChemicalStyle.AMAT) {
            EntityDamageUtil.attackEntityFromNt(entity, ModDamageSources.radiation(level()), adjustedIntensity, true);
        } else if (currentStyle == ChemicalStyle.LIGHTNING) {
            EntityDamageUtil.attackEntityFromNt(entity, ModDamageSources.source(level(), ModDamageSources.ELECTRICITY),
                    0.5F, true);
        }
        type.affectEntity(entity, adjustedIntensity);

        if (isExtinguishing()) {
            entity.clearFire();
        }
        if (currentStyle == ChemicalStyle.BURNING || currentStyle == ChemicalStyle.GASFLAME) {
            entity.setSecondsOnFire(currentStyle == ChemicalStyle.GASFLAME ? Math.max(1, (int) Math.ceil(5.0F * adjustedIntensity)) : 5);
        }
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
            FluidType type = fluid();
            releaseBlockImpactRadiation(pos, type);
            if (style() == ChemicalStyle.BURNING || style() == ChemicalStyle.GASFLAME) {
                placeAdjacentFire(pos, type == HbmFluids.BALEFIRE);
            }
            if (isExtinguishing()) {
                extinguishAdjacentFire(pos);
            }
            discard();
        }
    }

    private void releaseBlockImpactRadiation(BlockPos pos, FluidType type) {
        VentRadiationFluidTrait trait = type.getTrait(VentRadiationFluidTrait.class);
        if (trait != null) {
            ChunkRadiationManager.incrementRadiation(level(), pos, trait.getRadiationPerMb() * RELEASE_AMOUNT_MB);
        }
    }

    private void placeAdjacentFire(BlockPos pos, boolean balefire) {
        for (Direction direction : Direction.values()) {
            BlockPos firePos = pos.relative(direction);
            if (!level().getBlockState(firePos).isAir()) {
                continue;
            }
            BlockState fire = balefire
                    ? ModBlocks.BALEFIRE.get().defaultBlockState()
                    : BaseFireBlock.getState(level(), firePos);
            if (fire.canSurvive(level(), firePos)) {
                level().setBlock(firePos, fire, 3);
            }
        }
    }

    private void extinguishAdjacentFire(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos target = pos.relative(direction);
            if (level().getBlockState(target).is(Blocks.FIRE)) {
                level().removeBlock(target, false);
            }
        }
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
            return type.hasTrait(FlammableFluidTrait.class) || type.hasTrait(CombustibleFluidTrait.class)
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
