package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HexafluorideTankBlock;
import com.hbm.ntm.blockentity.HexafluorideTankBlockEntity;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class HexafluorideTankRenderer implements BlockEntityRenderer<HexafluorideTankBlockEntity> {
    public HexafluorideTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(HexafluorideTankBlockEntity blockEntity, Vec3 cameraPos) {
        return false;
    }

    @Override
    public void render(HexafluorideTankBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // World geometry is baked into the chunk mesh; keep this class for the item BEWLR helper.
    }

    static void renderItemModel(HexafluorideTankBlock.Kind kind, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        ObjMachineModels.TANK.renderAll(texture(kind), poseStack, buffer, packedLight, packedOverlay);
    }

    private static ResourceLocation texture(HexafluorideTankBlock.Kind kind) {
        return kind == HexafluorideTankBlock.Kind.PUF6
                ? ObjMachineModels.PUF6_TANK_TEXTURE
                : ObjMachineModels.UF6_TANK_TEXTURE;
    }

}
