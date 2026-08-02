package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
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
import net.minecraft.world.entity.monster.Creeper;

/** Shared modern rendering equivalent of legacy RenderCreeperUniversal. */
public final class LegacyCreeperRenderer<T extends Creeper> extends MobRenderer<T, CreeperModel<T>> {
    private static final ResourceLocation ARMOR_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/entity/creeper_armor.png");
    private final ResourceLocation texture;

    public LegacyCreeperRenderer(EntityRendererProvider.Context context, String textureName) {
        super(context, new CreeperModel<>(context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
        this.texture = new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/" + textureName + ".png");
        addLayer(new PowerLayer<>(this, context.getModelSet()));
    }

    @Override
    protected void scale(T creeper, PoseStack poseStack, float partialTick) {
        float swell = creeper.getSwelling(partialTick);
        float flash = 1.0F + Mth.sin(swell * 100.0F) * swell * 0.01F;
        swell = Mth.clamp(swell, 0.0F, 1.0F);
        swell *= swell;
        swell *= swell;
        swell *= 5.0F;
        poseStack.scale((1.0F + swell * 0.4F) * flash, (1.0F + swell * 0.1F) / flash,
                (1.0F + swell * 0.4F) * flash);
    }

    @Override
    protected float getWhiteOverlayProgress(T creeper, float partialTick) {
        float swell = creeper.getSwelling(partialTick);
        return (int) (swell * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(swell, 0.5F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }

    private static final class PowerLayer<T extends Creeper> extends EnergySwirlLayer<T, CreeperModel<T>> {
        private final CreeperModel<T> model;

        private PowerLayer(RenderLayerParent<T, CreeperModel<T>> parent, EntityModelSet modelSet) {
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
        protected EntityModel<T> model() {
            return model;
        }
    }
}
