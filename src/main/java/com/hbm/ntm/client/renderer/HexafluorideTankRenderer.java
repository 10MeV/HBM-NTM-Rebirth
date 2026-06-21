package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HexafluorideTankBlock;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.HexafluorideTankBlockEntity;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class HexafluorideTankRenderer implements BlockEntityRenderer<HexafluorideTankBlockEntity> {
    public HexafluorideTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(HexafluorideTankBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof HexafluorideTankBlock block)) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyRotation(state)));
        ObjMachineModels.TANK.renderAll(texture(block.kind()), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
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

    private static float legacyRotation(BlockState state) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
        return switch (facing) {
            case WEST -> 90.0F;
            case SOUTH -> 180.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
    }
}
