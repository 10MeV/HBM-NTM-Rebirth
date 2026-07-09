package com.hbm.entity.projectile;

import com.hbm.ntm.entity.projectile.ChemicalProjectileEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityChemical extends ChemicalProjectileEntity {
    public EntityChemical(EntityType<? extends ChemicalProjectileEntity> type, Level level) {
        super(type, level);
    }

    public EntityChemical(Level level) {
        super(ModEntityTypes.CHEMICAL_PROJECTILE.get(), level);
    }

    public EntityChemical(Level level, LivingEntity thrower, double sideOffset, double heightOffset, double frontOffset) {
        this(level);
        // 1.7.10 accepted these offset parameters but the constructor delegated without applying them.
        shootFromThrower(thrower);
    }

    @Override
    public EntityChemical setFluid(FluidType fluid) {
        super.setFluid(fluid);
        return this;
    }

    private void shootFromThrower(LivingEntity thrower) {
        if (thrower == null) {
            return;
        }
        setOwner(thrower);
        float yaw = thrower.getYRot();
        float pitch = thrower.getXRot();
        double yawRad = yaw * Math.PI / 180.0D;
        double pitchRad = pitch * Math.PI / 180.0D;
        setYRot(yaw);
        setXRot(pitch);
        setPos(
                thrower.getX() - Math.cos(yawRad) * 0.16D,
                thrower.getY() + thrower.getEyeHeight() - 0.1D,
                thrower.getZ() - Math.sin(yawRad) * 0.16D);

        double motionX = -Math.sin(yawRad) * Math.cos(pitchRad) * 0.4D;
        double motionZ = Math.cos(yawRad) * Math.cos(pitchRad) * 0.4D;
        double motionY = -Math.sin(pitchRad) * 0.4D;
        setLegacyThrowableHeading(motionX, motionY, motionZ, 1.5F, 1.0F);
    }

    private void setLegacyThrowableHeading(double motionX, double motionY, double motionZ, float velocity, float inaccuracy) {
        double length = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        if (length < 1.0E-7D) {
            return;
        }
        motionX /= length;
        motionY /= length;
        motionZ /= length;
        motionX += random.nextGaussian() * 0.0075D * inaccuracy;
        motionY += random.nextGaussian() * 0.0075D * inaccuracy;
        motionZ += random.nextGaussian() * 0.0075D * inaccuracy;
        motionX *= velocity;
        motionY *= velocity;
        motionZ *= velocity;
        setDeltaMovement(motionX, motionY, motionZ);

        float hyp = (float) Math.sqrt(motionX * motionX + motionZ * motionZ);
        float yaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
        float pitch = (float) (Math.atan2(motionY, hyp) * 180.0D / Math.PI);
        setYRot(yaw);
        setXRot(pitch);
        yRotO = yaw;
        xRotO = pitch;
        ticksInGround = 0;
    }
}
