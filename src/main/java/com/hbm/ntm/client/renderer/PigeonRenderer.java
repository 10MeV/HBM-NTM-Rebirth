package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.model.PigeonModel;
import com.hbm.ntm.entity.mob.EntityPigeon;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Uses the 1.7.10 RenderPigeon wing interpolation rather than vanilla chicken animation. */
public final class PigeonRenderer extends MobRenderer<EntityPigeon, PigeonModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/pigeon.png");

    public PigeonRenderer(EntityRendererProvider.Context context) {
        super(context, new PigeonModel(PigeonModel.createLayer().bakeRoot()), 0.3F);
    }

    @Override
    protected float getBob(EntityPigeon pigeon, float partialTick) {
        float fallTime = pigeon.prevFallTime + (pigeon.fallTime - pigeon.prevFallTime) * partialTick;
        float destination = pigeon.prevDest + (pigeon.dest - pigeon.prevDest) * partialTick;
        return (Mth.sin(fallTime) + 1.0F) * destination;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityPigeon pigeon) {
        return TEXTURE;
    }
}
