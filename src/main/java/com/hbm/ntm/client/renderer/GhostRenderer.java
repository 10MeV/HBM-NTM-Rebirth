package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.mob.EntityGhost;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Modern alpha-blended counterpart of 1.7.10 {@code RenderGhost}. */
public final class GhostRenderer extends MobRenderer<EntityGhost, HumanoidModel<EntityGhost>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/ghost.png");

    public GhostRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    protected RenderType getRenderType(EntityGhost ghost, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(ghost));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityGhost ghost) {
        return TEXTURE;
    }
}
