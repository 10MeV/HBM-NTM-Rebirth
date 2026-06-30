package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.MachineBatterySocketBlock;
import com.hbm.ntm.blockentity.MachineBatterySocketBlockEntity;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyHorseRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmSelfChargingBatteryItem;
import com.hbm.ntm.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MachineBatterySocketRenderer implements BlockEntityRenderer<MachineBatterySocketBlockEntity> {
    static final ResourceLocation SOCKET_TEXTURE = ObjMachineModels.BATTERY_SOCKET_TEXTURE;
    private static final ResourceLocation SELF_CHARGING_TEXTURE = ObjMachineModels.BATTERY_SC_TEXTURE;
    static final LegacyWavefrontModel MODEL = ObjMachineModels.BATTERY_SOCKET_LEGACY;
    private static final LegacyWavefrontModel.SelectionHandle SOCKET =
            MODEL.prepareRenderOnlyInCallOrder("Socket");
    private static final LegacyWavefrontModel.SelectionHandle SUPPORTS =
            MODEL.prepareRenderOnlyInCallOrder("Supports");
    private static final LegacyWavefrontModel.SelectionHandle BATTERY =
            MODEL.prepareRenderOnlyInCallOrder("Battery");
    private static final LegacyWavefrontModel.SelectionHandle CAPACITOR =
            MODEL.prepareRenderOnlyInCallOrder("Capacitor");
    private static final LegacyHorseRenderer CREATIVE_HORSE = new LegacyHorseRenderer();

    public MachineBatterySocketRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(MachineBatterySocketBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(MachineBatterySocketBlockEntity socket, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(socket, getViewDistance())) {
            return;
        }
        LegacyBlockEntityRenderCulling.recordMachineSubmission(socket);
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(socket, packedLight);
        poseStack.pushPose();
        BlockState state = socket.getBlockState();
        applyLegacySocketTransform(state, poseStack);

        renderModelPart(SOCKET, SOCKET_TEXTURE, poseStack, buffer, modelLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_CULL);
        if (socket.hasFrame()) {
            renderModelPart(SUPPORTS, SOCKET_TEXTURE, poseStack, buffer, modelLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_CULL);
        }

        ItemStack stack = socket.getBatteryStack();
        if (stack.getItem() instanceof HbmBatteryPackItem pack) {
            ResourceLocation texture = BatteryPackItemRenderer.textureFor(pack);
            renderModelPart(pack.isCapacitor() ? CAPACITOR : BATTERY, texture, poseStack, buffer, modelLight,
                    packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
        } else if (stack.getItem() instanceof HbmSelfChargingBatteryItem) {
            renderModelPart(BATTERY, SELF_CHARGING_TEXTURE, poseStack, buffer, modelLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_CULL);
        } else if (stack.is(ModItems.BATTERY_CREATIVE.get())) {
            renderCreativeBatteryEffect(socket, partialTick, poseStack, buffer, modelLight, packedOverlay);
        }

        poseStack.popPose();
    }

    private static void renderCreativeBatteryEffect(MachineBatterySocketBlockEntity socket, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = socket.getLevel();
        if (level == null) {
            return;
        }

        LegacyTileRenderPlans.CreativeBatterySocketPlan plan = LegacyTileRenderPlans.creativeBatterySocketPlan(
                level.getGameTime(), System.currentTimeMillis(), partialTick);

        poseStack.pushPose();
        poseStack.scale((float) plan.horseScale(), (float) plan.horseScale(), (float) plan.horseScale());
        poseStack.mulPose(Axis.YN.rotationDegrees((float) plan.horseYawDegrees()));
        CREATIVE_HORSE.reset();
        CREATIVE_HORSE.enableHorn();
        CREATIVE_HORSE.render(poseStack, buffer, LegacyHorseRenderer.SUNBURST_TEXTURE, packedLight, packedOverlay);
        poseStack.popPose();

        if (!plan.beams().isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(plan.beamTranslateX(), plan.beamTranslateY(), plan.beamTranslateZ());
            LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, queuedPose -> {
                for (LegacyBeamRenderer.BeamPlan beam : plan.beams()) {
                    LegacyBeamRenderer.beam(queuedPose, buffer, beam);
                }
            });
            poseStack.popPose();
        }
    }

    private static void applyLegacySocketTransform(BlockState state, PoseStack poseStack) {
        Direction facing = state.hasProperty(MachineBatterySocketBlock.FACING)
                ? state.getValue(MachineBatterySocketBlock.FACING)
                : Direction.SOUTH;
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(270.0F - facing.toYRot()));
        poseStack.translate(-0.5D, 0.0D, 0.5D);
    }

    static void renderModelPart(String partName, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyWavefrontModel.SelectionHandle handle = handle(partName);
        if (handle != null) {
            MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle);
            return;
        }
        MODEL.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderModelPart(LegacyWavefrontModel.SelectionHandle handle, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle, renderMode);
    }

    private static LegacyWavefrontModel.SelectionHandle handle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Socket" -> SOCKET;
            case "Supports" -> SUPPORTS;
            case "Battery" -> BATTERY;
            case "Capacitor" -> CAPACITOR;
            default -> null;
        };
    }
}
