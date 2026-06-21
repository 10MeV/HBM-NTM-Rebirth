package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.entity.projectile.SawbladeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SawbladeRenderer extends MachinePartProjectileRenderer<SawbladeEntity> {
    public SawbladeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected LegacyWavefrontModel model() {
        return ObjMachineModels.SAWMILL;
    }

    @Override
    protected String partName() {
        return "Blade";
    }

    @Override
    protected ResourceLocation texture(SawbladeEntity entity) {
        return ObjMachineModels.SAWMILL_TEXTURE;
    }

    @Override
    protected float spinDegrees() {
        return (System.currentTimeMillis() % (360L * 5L)) / 3.0F;
    }
}
