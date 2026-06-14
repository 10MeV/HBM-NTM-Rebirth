package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.CustomMissileLauncherBlock;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.CustomMissileLauncherBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjLaunchModels;
import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.client.obj.ObjMissilePartModels.LegacyMissilePart;
import com.hbm.ntm.item.missile.CustomMissilePartProfile;
import com.hbm.ntm.item.missile.CustomMissilePartProfile.PartSize;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class CustomMissileLauncherRenderer implements BlockEntityRenderer<CustomMissileLauncherBlockEntity> {
    public CustomMissileLauncherRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(CustomMissileLauncherBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(CustomMissileLauncherBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        if (state.getBlock() instanceof CustomMissileLauncherBlock launcher
                && launcher.kind() == CustomMissileLauncherBlock.Kind.LAUNCH_TABLE) {
            renderLaunchTable(blockEntity, poseStack, buffer, packedLight);
        } else {
            renderCompactLauncher(blockEntity, poseStack, buffer, packedLight);
        }
        poseStack.popPose();
    }

    private static void renderCompactLauncher(CustomMissileLauncherBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ObjLaunchModels.COMPACT_LAUNCHER.renderAll(ObjLaunchModels.COMPACT_LAUNCHER_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        CustomMissilePartProfile.Assembly assembly = blockEntity.assemblyForPreview();
        if (isValidForPad(assembly, PartSize.SIZE_10)) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 1.0625D, 0.0D);
            renderMissile(assembly, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    private static void renderLaunchTable(CustomMissileLauncherBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ObjLaunchModels.LAUNCH_TABLE_BASE_LEGACY.renderAll(ObjLaunchModels.LAUNCH_TABLE_BASE_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        PartSize padSize = blockEntity.getPadSize();
        if (padSize == PartSize.SIZE_20) {
            ObjLaunchModels.LAUNCH_TABLE_LARGE_PAD_LEGACY.renderAll(ObjLaunchModels.LAUNCH_TABLE_LARGE_PAD_TEXTURE,
                    poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        } else {
            ObjLaunchModels.LAUNCH_TABLE_SMALL_PAD_LEGACY.renderAll(ObjLaunchModels.LAUNCH_TABLE_SMALL_PAD_TEXTURE,
                    poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        }

        CustomMissilePartProfile.Assembly assembly = blockEntity.assemblyForPreview();
        int missileHeight = Math.max(10, (int) Math.ceil(missileHeight(assembly)));
        int connectorHeight = (int) (missileHeight * 0.75D);
        ScaffoldSet scaffold = padSize == PartSize.SIZE_10 ? ScaffoldSet.SMALL : ScaffoldSet.LARGE;

        poseStack.pushPose();
        if (padSize == PartSize.SIZE_10) {
            poseStack.translate(0.0D, 0.0D, -1.0D);
        }
        poseStack.translate(0.0D, 1.0D, 3.5D);
        for (int i = 0; i < missileHeight + 1; i++) {
            if (i == connectorHeight && isValidForPad(assembly, padSize)) {
                scaffold.connector().renderAll(scaffold.connectorTexture(), poseStack, buffer,
                        packedLight, OverlayTexture.NO_OVERLAY);
            } else if (i > connectorHeight) {
                scaffold.empty().renderAll(scaffold.baseTexture(), poseStack, buffer,
                        packedLight, OverlayTexture.NO_OVERLAY);
            } else {
                scaffold.base().renderAll(scaffold.baseTexture(), poseStack, buffer,
                        packedLight, OverlayTexture.NO_OVERLAY);
            }
            poseStack.translate(0.0D, 1.0D, 0.0D);
        }
        poseStack.popPose();

        if (isValidForPad(assembly, padSize)) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 2.0625D, 0.0D);
            renderMissile(assembly, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    private static void renderMissile(CustomMissilePartProfile.Assembly assembly, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ObjMissilePartModels.renderMissile(
                part(assembly.thruster()),
                part(assembly.fins()),
                part(assembly.fuselage()),
                part(assembly.warhead()),
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
    }

    private static boolean isValidForPad(CustomMissilePartProfile.Assembly assembly, PartSize padSize) {
        return assembly != null && assembly.isCompleteForLaunch()
                && assembly.fuselage() != null
                && assembly.fuselage().profile().top() == padSize;
    }

    private static double missileHeight(CustomMissilePartProfile.Assembly assembly) {
        if (assembly == null) {
            return 10.0D;
        }
        return ObjMissilePartModels.missileHeight(
                part(assembly.thruster()),
                part(assembly.fuselage()),
                part(assembly.warhead()));
    }

    private static LegacyMissilePart part(CustomMissilePartProfile.ResolvedPart part) {
        return part == null ? null : ObjMissilePartModels.part(part.legacyName());
    }

    private record ScaffoldSet(LegacyWavefrontModel base, LegacyWavefrontModel connector, LegacyWavefrontModel empty,
                               ResourceLocation baseTexture, ResourceLocation connectorTexture) {
        private static final ScaffoldSet SMALL = new ScaffoldSet(
                ObjLaunchModels.LAUNCH_TABLE_SMALL_SCAFFOLD_BASE_LEGACY,
                ObjLaunchModels.LAUNCH_TABLE_SMALL_SCAFFOLD_CONNECTOR_LEGACY,
                ObjLaunchModels.LAUNCH_TABLE_SMALL_SCAFFOLD_EMPTY_LEGACY,
                ObjLaunchModels.LAUNCH_TABLE_SMALL_SCAFFOLD_BASE_TEXTURE,
                ObjLaunchModels.LAUNCH_TABLE_SMALL_SCAFFOLD_CONNECTOR_TEXTURE);
        private static final ScaffoldSet LARGE = new ScaffoldSet(
                ObjLaunchModels.LAUNCH_TABLE_LARGE_SCAFFOLD_BASE_LEGACY,
                ObjLaunchModels.LAUNCH_TABLE_LARGE_SCAFFOLD_CONNECTOR_LEGACY,
                ObjLaunchModels.LAUNCH_TABLE_LARGE_SCAFFOLD_EMPTY_LEGACY,
                ObjLaunchModels.LAUNCH_TABLE_LARGE_SCAFFOLD_BASE_TEXTURE,
                ObjLaunchModels.LAUNCH_TABLE_LARGE_SCAFFOLD_CONNECTOR_TEXTURE);
    }
}
