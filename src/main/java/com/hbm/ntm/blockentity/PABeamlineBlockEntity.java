package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.ParticleAcceleratorBlock;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class PABeamlineBlockEntity extends PABlockEntity implements PAParticleUser {
    private static final String TAG_WINDOW = "window";
    private static final String TAG_DID_PASS = "didPass";

    private boolean window;
    private boolean didPass;
    private float light;
    private float prevLight;

    public PABeamlineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PA_BEAMLINE.get(), pos, state, ParticleAcceleratorBlock.Variant.BEAMLINE, 0, 0L);
    }

    @Override
    public void serverTick() {
        if (level != null && !level.isClientSide && level.getGameTime() % 150L == 0L) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void clientTick() {
        prevLight = light;
        if (light > 0.0F) {
            light -= 0.25F;
        }
        if (light > prevLight) {
            prevLight = light;
        }
    }

    public void toggleWindow() {
        window = !window;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean hasWindow() {
        return window;
    }

    public float getFlash(float partialTick) {
        return prevLight + (light - prevLight) * partialTick;
    }

    @Override
    public List<EnergyPort> energyPorts() {
        return List.of();
    }

    @Override
    public List<FluidPort> fluidPorts() {
        return List.of();
    }

    @Override
    public boolean canParticleEnter(PASourceBlockEntity.Particle particle, Direction dir, BlockPos entryPos) {
        Direction beam = beamSide(facing());
        return worldPosition.relative(beam.getOpposite()).equals(entryPos) && beam == dir;
    }

    @Override
    public void onParticleEnter(PASourceBlockEntity.Particle particle, Direction dir) {
        particle.addDistance(3);
        didPass = true;
    }

    @Override
    public BlockPos getParticleExitPos(PASourceBlockEntity.Particle particle) {
        return worldPosition.relative(beamSide(facing()), 2);
    }

    @Override
    protected void loadPa(CompoundTag tag) {
        window = tag.getBoolean(TAG_WINDOW);
        didPass = tag.getBoolean(TAG_DID_PASS);
        if (level != null && level.isClientSide && didPass) {
            light = 2.0F;
        }
    }

    @Override
    protected void savePa(CompoundTag tag) {
        tag.putBoolean(TAG_WINDOW, window);
        tag.putBoolean(TAG_DID_PASS, didPass);
        didPass = false;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(1.0D, 0.0D, 1.0D);
    }
}
