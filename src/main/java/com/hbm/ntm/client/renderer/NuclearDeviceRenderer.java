package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.NuclearDeviceBlock;
import com.hbm.ntm.blockentity.NuclearDeviceBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjNukeModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class NuclearDeviceRenderer implements BlockEntityRenderer<NuclearDeviceBlockEntity> {
    private static final ResourceLocation GADGET_TEXTURE = ObjNukeModels.texture("gadget");
    private static final ResourceLocation BOY_TEXTURE = ObjNukeModels.texture("boy");
    private static final ResourceLocation MAN_TEXTURE = ObjNukeModels.texture("man");
    private static final ResourceLocation TSAR_TEXTURE = ObjNukeModels.texture("tsar");
    private static final ResourceLocation MIKE_TEXTURE = ObjNukeModels.texture("mike");
    private static final ResourceLocation PROTOTYPE_TEXTURE = ObjNukeModels.texture("prototype");
    private static final ResourceLocation FLEIJA_TEXTURE = ObjNukeModels.texture("fleija");
    private static final ResourceLocation SOLINIUM_TEXTURE = ObjNukeModels.texture("solinium");
    private static final ResourceLocation N2_TEXTURE = ObjNukeModels.texture("n2");

    public NuclearDeviceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NuclearDeviceBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(NuclearDeviceBlock.FACING)
                ? state.getValue(NuclearDeviceBlock.FACING)
                : Direction.SOUTH;
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(NuclearDeviceBlock.legacyRenderYaw(blockEntity.kind(), facing)));
        applyLegacyBlockTranslation(blockEntity.kind(), poseStack);
        renderKind(blockEntity.kind(), poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();
    }

    public static void renderKind(NuclearDeviceBlock.Kind kind, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        LegacyTexturedRenderMode renderMode = renderMode(kind);
        if (kind == NuclearDeviceBlock.Kind.GADGET) {
            ResourceLocation gadgetTexture = texture(kind);
            ObjNukeModels.renderGadgetPart(gadgetTexture, poseStack, buffer, packedLight, packedOverlay,
                    renderMode, "Body");
            if (fancyGraphics()) {
                ObjNukeModels.renderGadgetPart(gadgetTexture, poseStack, buffer, packedLight, packedOverlay,
                        renderMode, "Wires");
            }
            return;
        }
        model(kind).renderAll(texture(kind), poseStack, buffer, packedLight, packedOverlay, renderMode);
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
            case GADGET -> GADGET_TEXTURE;
            case BOY -> BOY_TEXTURE;
            case MAN -> MAN_TEXTURE;
            case TSAR -> TSAR_TEXTURE;
            case MIKE -> MIKE_TEXTURE;
            case PROTOTYPE -> PROTOTYPE_TEXTURE;
            case FLEIJA -> FLEIJA_TEXTURE;
            case SOLINIUM -> SOLINIUM_TEXTURE;
            case N2 -> N2_TEXTURE;
        };
    }

    public static void renderCustomNuke(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjNukeModels.BOY.renderAll(ObjNukeModels.CUSTOM_NUKE_TEXTURE, poseStack, buffer, packedLight,
                packedOverlay);
    }

    public static void applyCustomNukeLegacyItemCommon(PoseStack poseStack) {
        poseStack.translate(-1.0D, 0.0D, 0.0D);
    }

    public static void applyCustomNukeLegacyWorldTranslation(PoseStack poseStack) {
        poseStack.translate(-2.0D, 0.0D, 0.0D);
    }

    private static void applyLegacyBlockTranslation(NuclearDeviceBlock.Kind kind, PoseStack poseStack) {
        if (kind == NuclearDeviceBlock.Kind.BOY) {
            poseStack.translate(-2.0D, 0.0D, 0.0D);
        }
    }

    private static LegacyTexturedRenderMode renderMode(NuclearDeviceBlock.Kind kind) {
        return switch (kind) {
            case BOY, FLEIJA, N2 -> LegacyTexturedRenderMode.CUTOUT_CULL;
            case GADGET, MAN, TSAR, MIKE, PROTOTYPE, SOLINIUM -> LegacyTexturedRenderMode.CUTOUT_NO_CULL;
        };
    }

    private static boolean fancyGraphics() {
        GraphicsStatus graphics = Minecraft.getInstance().options.graphicsMode().get();
        return graphics == GraphicsStatus.FANCY || graphics == GraphicsStatus.FABULOUS;
    }

    @Override
    public boolean shouldRenderOffScreen(NuclearDeviceBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }
}
