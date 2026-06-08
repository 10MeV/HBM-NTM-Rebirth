package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.SoyuzLauncherBlockEntity;
import com.hbm.ntm.client.obj.ObjLaunchModels;
import com.hbm.ntm.client.obj.ObjSoyuzModels;
import com.hbm.ntm.client.obj.ObjSoyuzModels.SoyuzTextureSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class SoyuzLauncherRenderer implements BlockEntityRenderer<SoyuzLauncherBlockEntity> {
    public SoyuzLauncherRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(SoyuzLauncherBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(SoyuzLauncherBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, -4.0D, 0.5D);
        renderLauncher(blockEntity.getTowerRotation(partialTick), poseStack, buffer, packedLight);
        if (blockEntity.getRocketType() >= 0) {
            poseStack.translate(0.0D, 5.0D, 0.0D);
            ObjSoyuzModels.renderSoyuz(textureSet(blockEntity.getRocketType()), poseStack, buffer,
                    packedLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }

    private static void renderLauncher(float rotation, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ObjLaunchModels.SOYUZ_LAUNCHER_LEGS_LEGACY.renderAll(ObjLaunchModels.SOYUZ_LAUNCHER_LEG_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        ObjLaunchModels.SOYUZ_LAUNCHER_TABLE_LEGACY.renderAll(ObjLaunchModels.SOYUZ_LAUNCHER_TABLE_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        ObjLaunchModels.SOYUZ_LAUNCHER_TOWER_BASE_LEGACY.renderAll(ObjLaunchModels.SOYUZ_LAUNCHER_TOWER_BASE_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.pushPose();
        poseStack.translate(0.0D, 5.5D, 5.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.translate(0.0D, -5.5D, -5.5D);
        ObjLaunchModels.SOYUZ_LAUNCHER_TOWER_LEGACY.renderAll(ObjLaunchModels.SOYUZ_LAUNCHER_TOWER_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        ObjLaunchModels.SOYUZ_LAUNCHER_SUPPORT_BASE_LEGACY.renderAll(ObjLaunchModels.SOYUZ_LAUNCHER_SUPPORT_BASE_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.pushPose();
        poseStack.translate(0.0D, 5.5D, -6.5D);
        poseStack.mulPose(Axis.XN.rotationDegrees(rotation));
        poseStack.translate(0.0D, -5.5D, 6.5D);
        ObjLaunchModels.SOYUZ_LAUNCHER_SUPPORT_LEGACY.renderAll(ObjLaunchModels.SOYUZ_LAUNCHER_SUPPORT_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static SoyuzTextureSet textureSet(int skin) {
        return switch (skin) {
            case 1 -> ObjSoyuzModels.LUNA_TEXTURES;
            case 2 -> ObjSoyuzModels.AUTHENTIC_TEXTURES;
            default -> ObjSoyuzModels.SOYUZ_TEXTURES;
        };
    }
}
