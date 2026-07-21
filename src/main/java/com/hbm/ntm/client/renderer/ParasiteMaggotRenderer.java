package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.mob.EntityParasiteMaggot;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Source-backed 1.7.10 RenderMaggot Silverfish-model renderer. */
public class ParasiteMaggotRenderer extends MobRenderer<EntityParasiteMaggot, SilverfishModel<EntityParasiteMaggot>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/parasite_maggot.png");

    public ParasiteMaggotRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverfishModel<>(context.bakeLayer(ModelLayers.SILVERFISH)), 0.3F);
    }

    @Override
    protected float getFlipDegrees(EntityParasiteMaggot entity) {
        return 180.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityParasiteMaggot entity) {
        return TEXTURE;
    }
}
