package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.NuclearDeviceBlock;
import com.hbm.ntm.blockentity.NuclearDeviceBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjNukeModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class NuclearDeviceRenderer implements BlockEntityRenderer<NuclearDeviceBlockEntity> {
    public NuclearDeviceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NuclearDeviceBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(NuclearDeviceBlock.FACING)
                ? state.getValue(NuclearDeviceBlock.FACING)
                : Direction.SOUTH;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(NuclearDeviceBlock.legacyRenderYaw(blockEntity.kind(), facing)));
        applyLegacyBlockTranslation(blockEntity.kind(), poseStack);
        renderKind(blockEntity.kind(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    public static void renderKind(NuclearDeviceBlock.Kind kind, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        model(kind).renderAll(texture(kind), poseStack, buffer, packedLight, packedOverlay);
    }

    public static LegacyWavefrontModel model(NuclearDeviceBlock.Kind kind) {
        return switch (kind) {
            case GADGET -> ObjNukeModels.GADGET;
            case BOY -> ObjNukeModels.BOY;
            case MAN -> ObjNukeModels.MAN;
            case TSAR -> ObjNukeModels.TSAR;
            case MIKE -> ObjNukeModels.MIKE;
            case PROTOTYPE -> ObjNukeModels.PROTOTYPE;
            case FLEIJA -> ObjNukeModels.FLEIJA;
            case SOLINIUM -> ObjNukeModels.SOLINIUM;
            case N2 -> ObjNukeModels.N2;
        };
    }

    public static ResourceLocation texture(NuclearDeviceBlock.Kind kind) {
        return switch (kind) {
            case GADGET -> ObjNukeModels.texture("gadget");
            case BOY -> ObjNukeModels.texture("boy");
            case MAN -> ObjNukeModels.texture("man");
            case TSAR -> ObjNukeModels.texture("tsar");
            case MIKE -> ObjNukeModels.texture("mike");
            case PROTOTYPE -> ObjNukeModels.texture("prototype");
            case FLEIJA -> ObjNukeModels.texture("fleija");
            case SOLINIUM -> ObjNukeModels.texture("solinium");
            case N2 -> ObjNukeModels.texture("n2");
        };
    }

    private static void applyLegacyBlockTranslation(NuclearDeviceBlock.Kind kind, PoseStack poseStack) {
        if (kind == NuclearDeviceBlock.Kind.BOY) {
            poseStack.translate(-2.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(NuclearDeviceBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }
}
