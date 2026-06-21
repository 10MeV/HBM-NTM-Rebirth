package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.entity.projectile.CogEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CogRenderer extends MachinePartProjectileRenderer<CogEntity> {
    public CogRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected LegacyWavefrontModel model() {
        return ObjMachineModels.STIRLING;
    }

    @Override
    protected String partName() {
        return "Cog";
    }

    @Override
    protected ResourceLocation texture(CogEntity entity) {
        return entity.getMeta() == 0 ? ObjMachineModels.STIRLING_TEXTURE : ObjMachineModels.STIRLING_STEEL_TEXTURE;
    }

    @Override
    protected float spinDegrees() {
        return (System.currentTimeMillis() % (360L * 3L)) / 3.0F;
    }
}
