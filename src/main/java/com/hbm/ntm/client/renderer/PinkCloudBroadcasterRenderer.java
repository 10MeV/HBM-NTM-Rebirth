package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.PinkCloudBroadcasterBlock;
import com.hbm.ntm.blockentity.PinkCloudBroadcasterBlockEntity;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Exact four-cuboid conversion of the legacy Techne ModelBroadcaster. */
public class PinkCloudBroadcasterRenderer implements BlockEntityRenderer<PinkCloudBroadcasterBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/models/model_broadcaster.png");
    private final ModelPart root;
    public PinkCloudBroadcasterRenderer(BlockEntityRendererProvider.Context context) { root = createLayer().bakeRoot(); }
    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0, 0, 0, 14, 10, 8), PartPose.offset(-7, 14, -4));
        root.addOrReplaceChild("knob", CubeListBuilder.create().texOffs(4, 21).mirror().addBox(0, 0, 0, 2, 3, 2), PartPose.offset(-5, 11, -1));
        root.addOrReplaceChild("antenna", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(0, 0, 0, 1, 11, 1), PartPose.offset(-4.5F, 0, -0.5F));
        root.addOrReplaceChild("dial", CubeListBuilder.create().texOffs(4, 18).mirror().addBox(0, 0, 0, 3, 2, 1), PartPose.offset(2, 12, -0.5F));
        return LayerDefinition.create(mesh, 64, 32);
    }
    @Override public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }
    @Override public boolean shouldRender(PinkCloudBroadcasterBlockEntity broadcaster, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(broadcaster, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(broadcaster, getViewDistance());
    }
    @Override public void render(PinkCloudBroadcasterBlockEntity broadcaster, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(broadcaster, getViewDistance())) {
            return;
        }
        Direction facing = broadcaster.getBlockState().getValue(PinkCloudBroadcasterBlock.FACING);
        float rotation = switch (facing) { case WEST -> 90.0F; case NORTH -> 180.0F; case EAST -> 270.0F; default -> 0.0F; };
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.5D, 0.5D);
        LegacyPoseRotations.rotateZDegrees(poseStack, 180.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, rotation);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(broadcaster)) {
            root.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight,
                    OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }
}
