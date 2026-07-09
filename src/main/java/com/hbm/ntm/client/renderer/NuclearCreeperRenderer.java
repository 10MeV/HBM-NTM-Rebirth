package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.mob.EntityCreeperNuclear;
import com.mojang.blaze3d.vertex.PoseStack;
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

public class NuclearCreeperRenderer extends MobRenderer<EntityCreeperNuclear, CreeperModel<EntityCreeperNuclear>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/entity/creeper.png");
    private static final ResourceLocation ARMOR_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/entity/creeper_armor.png");

    public NuclearCreeperRenderer(EntityRendererProvider.Context context) {
        super(context, new CreeperModel<>(context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
        addLayer(new NuclearCreeperPowerLayer(this, context.getModelSet()));
    }

    @Override
    protected void scale(EntityCreeperNuclear creeper, PoseStack poseStack, float partialTick) {
        float swell = creeper.getSwelling(partialTick);
        float flash = 1.0F + Mth.sin(swell * 100.0F) * swell * 0.01F;
        swell = Mth.clamp(swell, 0.0F, 1.0F);
        swell *= swell;
        swell *= swell;
        swell *= 5.0F;
        float horizontal = (1.0F + swell * 0.4F) * flash;
        float vertical = (1.0F + swell * 0.1F) / flash;
        poseStack.scale(horizontal, vertical, horizontal);
    }

    @Override
    protected float getWhiteOverlayProgress(EntityCreeperNuclear creeper, float partialTick) {
        float swell = creeper.getSwelling(partialTick);
        return (int) (swell * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(swell, 0.5F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCreeperNuclear entity) {
        return TEXTURE;
    }

    private static class NuclearCreeperPowerLayer
            extends EnergySwirlLayer<EntityCreeperNuclear, CreeperModel<EntityCreeperNuclear>> {
        private final CreeperModel<EntityCreeperNuclear> model;

        private NuclearCreeperPowerLayer(
                RenderLayerParent<EntityCreeperNuclear, CreeperModel<EntityCreeperNuclear>> parent,
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
        protected EntityModel<EntityCreeperNuclear> model() {
            return model;
        }
    }
}
