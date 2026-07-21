package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.entity.item.RequestDroneEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** The request drone is the same OBJ base but always uses the legacy request texture. */
public class RequestDroneRenderer extends EntityRenderer<RequestDroneEntity> {
    private static final ResourceLocation TEXTURE=new ResourceLocation(HbmNtm.MOD_ID,"textures/models/machines/drone_request.png");
    /** Request drones use the same legacy OBJ and therefore the same reload-aware VBO cache. */
    private static final LegacyWavefrontModel MODEL=ObjMachineModels.DELIVERY_DRONE;
    public RequestDroneRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public void render(RequestDroneEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffer,int light){pose.pushPose();RenderSystem.disableCull();MODEL.renderPart("Drone",TEXTURE,pose,buffer,light,0);if(entity.appearance()==1)MODEL.renderPart("Crate",TEXTURE,pose,buffer,light,0);if(entity.appearance()==2)MODEL.renderPart("Barrel",TEXTURE,pose,buffer,light,0);RenderSystem.enableCull();pose.popPose();super.render(entity,yaw,partial,pose,buffer,light);}
    @Override public ResourceLocation getTextureLocation(RequestDroneEntity entity){return TEXTURE;}
}
