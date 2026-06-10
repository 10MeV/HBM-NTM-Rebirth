package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public final class LegacySmallPylonModel {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/network/model_pylon.png");
    public static final AABB LEGACY_RENDER_BOUNDS = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 5.5625D, 1.0D);

    private static final ModelPart ROOT = createLayer().bakeRoot();

    private LegacySmallPylonModel() {
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.5D - 14.0D / 16.0D, 0.5D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        ROOT.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("base", CubeListBuilder.create()
                        .mirror()
                        .texOffs(0, 96)
                        .addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(-8.0F, -6.0F, -8.0F));
        root.addOrReplaceChild("pole", CubeListBuilder.create()
                        .mirror()
                        .texOffs(1, 1)
                        .addBox(0.0F, 0.0F, 0.0F, 4.0F, 73.0F, 4.0F),
                PartPose.offset(-2.0F, -79.0F, -2.0F));
        root.addOrReplaceChild("lower_cap", CubeListBuilder.create()
                        .mirror()
                        .texOffs(24, 1)
                        .addBox(0.0F, 0.0F, 0.0F, 6.0F, 4.0F, 6.0F),
                PartPose.offset(-3.0F, -74.0F, -3.0F));
        root.addOrReplaceChild("upper_cap", CubeListBuilder.create()
                        .mirror()
                        .texOffs(25, 17)
                        .addBox(0.0F, 0.0F, 0.0F, 6.0F, 2.0F, 6.0F),
                PartPose.offset(-3.0F, -78.0F, -3.0F));
        return LayerDefinition.create(mesh, 64, 128);
    }
}
