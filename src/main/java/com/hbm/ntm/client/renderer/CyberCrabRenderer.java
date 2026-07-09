package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.model.CyberCrabModel;
import com.hbm.ntm.entity.mob.EntityCyberCrab;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CyberCrabRenderer extends MobRenderer<EntityCyberCrab, CyberCrabModel<EntityCyberCrab>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/entity/crab.png");

    public CyberCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new CyberCrabModel<>(CyberCrabModel.createLayer().bakeRoot()), 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCyberCrab entity) {
        return TEXTURE;
    }
}
