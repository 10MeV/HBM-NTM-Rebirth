package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HexafluorideTankBlock;
import com.hbm.ntm.blockentity.HexafluorideTankBlockEntity;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class HexafluorideTankRenderer implements BlockEntityRenderer<HexafluorideTankBlockEntity> {
    public HexafluorideTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(HexafluorideTankBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // World geometry is baked into the chunk mesh; keep this class for the item BEWLR helper.
    }

    static void renderItemModel(HexafluorideTankBlock.Kind kind, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
        ObjMachineModels.TANK.renderAll(texture(kind), poseStack, buffer, packedLight, packedOverlay);
    }

    private static ResourceLocation texture(HexafluorideTankBlock.Kind kind) {
        return kind == HexafluorideTankBlock.Kind.PUF6
                ? ObjMachineModels.PUF6_TANK_TEXTURE
                : ObjMachineModels.UF6_TANK_TEXTURE;
    }

}
