package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.MissileAssemblyBlockEntity;
import com.hbm.ntm.client.obj.ObjLaunchModels;
import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.client.obj.ObjMissilePartModels.LegacyMissilePart;
import com.hbm.ntm.item.missile.CustomMissilePartProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class MissileAssemblyRenderer implements BlockEntityRenderer<MissileAssemblyBlockEntity> {
    public MissileAssemblyRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(MissileAssemblyBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(MissileAssemblyBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyRotation(facing)));
        ObjLaunchModels.MISSILE_ASSEMBLY.renderAll(ObjLaunchModels.MISSILE_ASSEMBLY_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

        CustomMissilePartProfile.Assembly assembly = blockEntity.assemblyForPreview();
        if (assembly != null) {
            renderPreviewMissile(assembly, poseStack, buffer, packedLight);
        }
        poseStack.popPose();
    }

    private static void renderPreviewMissile(CustomMissilePartProfile.Assembly assembly, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        LegacyMissilePart thruster = part(assembly.thruster());
        LegacyMissilePart fins = part(assembly.fins());
        LegacyMissilePart fuselage = part(assembly.fuselage());
        LegacyMissilePart warhead = part(assembly.warhead());
        ObjMissilePartModels.MissileRenderPlan plan =
                ObjMissilePartModels.missileRenderPlan(thruster, fins, fuselage, warhead);
        if (plan.steps().isEmpty()) {
            return;
        }

        int range = (int) (plan.multipartHeight() / 2.0D - 1.0D);
        int step = range >= 2 ? 2 : 1;
        for (int i = -range; i <= range; i += step) {
            if (i == 0) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(i, 0.0D, 0.0D);
            ObjLaunchModels.STRUT.renderAll(ObjLaunchModels.STRUT_TEXTURE, poseStack, buffer,
                    packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.translate(-plan.multipartHeight() / 2.0D, 0.0D, 0.0D);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F));
        ObjMissilePartModels.renderMissile(thruster, fins, fuselage, warhead,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static LegacyMissilePart part(CustomMissilePartProfile.ResolvedPart part) {
        return part == null ? null : ObjMissilePartModels.part(part.legacyName());
    }

    private static float legacyRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            case SOUTH -> 0.0F;
            default -> 0.0F;
        };
    }
}
