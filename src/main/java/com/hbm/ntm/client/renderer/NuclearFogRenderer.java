package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.entity.particle.EntityFogFX;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

/** Direct 1.7.10 FogRenderer conversion: 25 deterministic camera-facing translucent quads. */
public class NuclearFogRenderer extends EntityRenderer<EntityFogFX> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/fog.png");
    public NuclearFogRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public void render(EntityFogFX fog, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        int alpha = Math.max(0, Math.min(64, Math.round((float)Math.sin(fog.tickCount * Math.PI / 400.0D) * .25F * 255.0F)));
        Random random = new Random(50L); VertexConsumer vertices = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        poseStack.pushPose(); poseStack.scale(7.5F,7.5F,7.5F);
        for(int i=0;i<25;i++) { double x=(random.nextGaussian()-1)*.5D,y=(random.nextGaussian()-1)*.15D,z=(random.nextGaussian()-1)*.5D; float s=(float)(random.nextDouble()*.5D+.25D); poseStack.pushPose(); poseStack.translate(x,y,z); poseStack.mulPose(entityRenderDispatcher.cameraOrientation()); poseStack.mulPose(Axis.YP.rotationDegrees(180)); poseStack.scale(s,s,s); quad(vertices,poseStack,alpha); poseStack.popPose(); }
        poseStack.popPose(); super.render(fog,yaw,partialTick,poseStack,buffer,packedLight);
    }
    private static void quad(VertexConsumer c, PoseStack p, int a) { var m=p.last(); v(c,m,-1,-1,1,0,a);v(c,m,-1,1,0,0,a);v(c,m,1,1,0,1,a);v(c,m,1,-1,1,1,a); }
    private static void v(VertexConsumer c, PoseStack.Pose p, float x,float y,float u,float v,int a) { c.vertex(p.pose(),x,y,0).color(217,230,128,a).uv(u,v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(p.normal(),0,1,0).endVertex(); }
    @Override public ResourceLocation getTextureLocation(EntityFogFX fog) { return TEXTURE; }
}
