package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.mob.EntityCreeperTainted;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TaintedCreeperRenderer extends MobRenderer<EntityCreeperTainted, CreeperModel<EntityCreeperTainted>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/entity/creeper_tainted.png");
    private static final ResourceLocation ARMOR_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/entity/creeper_armor_taint.png");

    public TaintedCreeperRenderer(EntityRendererProvider.Context context) {
        super(context, new CreeperModel<>(context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
        addLayer(new TaintedCreeperPowerLayer(this, context.getModelSet()));
    }

    @Override
    protected void scale(EntityCreeperTainted creeper, com.mojang.blaze3d.vertex.PoseStack poseStack, float partialTick) {
        float swell = creeper.getSwelling(partialTick);
        float flash = 1.0F + Mth.sin(swell * 100.0F) * swell * 0.01F;
        swell = Mth.clamp(swell, 0.0F, 1.0F);
        swell *= swell;
        swell *= swell;
        float horizontal = (1.0F + swell * 0.4F) * flash;
        float vertical = (1.0F + swell * 0.1F) / flash;
        poseStack.scale(horizontal, vertical, horizontal);
    }

    @Override
    protected float getWhiteOverlayProgress(EntityCreeperTainted creeper, float partialTick) {
        float swell = creeper.getSwelling(partialTick);
        return (int) (swell * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(swell, 0.5F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCreeperTainted entity) {
        return TEXTURE;
    }

    private static class TaintedCreeperPowerLayer
            extends EnergySwirlLayer<EntityCreeperTainted, CreeperModel<EntityCreeperTainted>> {
        private final CreeperModel<EntityCreeperTainted> model;

        private TaintedCreeperPowerLayer(
                RenderLayerParent<EntityCreeperTainted, CreeperModel<EntityCreeperTainted>> parent,
                EntityModelSet modelSet) {
            super(parent);
            this.model = new CreeperModel<>(modelSet.bakeLayer(ModelLayers.CREEPER_ARMOR));
        }

        @Override
        protected float xOffset(float tickCount) {
            return tickCount * 0.01F;
        }

        @Override
        protected ResourceLocation getTextureLocation() {
            return ARMOR_TEXTURE;
        }

        @Override
        protected EntityModel<EntityCreeperTainted> model() {
            return model;
        }
    }
}
