package com.hbm.ntm.particle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class ClientParticleBridge {
    public static void handleAux(CompoundTag data) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.hbm.ntm.client.particle.HbmParticleEffects.handleAux(data));
    }

    public static void burst(BlockPos pos, BlockState state) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.hbm.ntm.client.particle.HbmParticleEffects.burst(pos, state));
    }

    private ClientParticleBridge() {
    }
}
