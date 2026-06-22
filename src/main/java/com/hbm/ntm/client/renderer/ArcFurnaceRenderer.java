package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.ArcFurnaceBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ArcFurnaceRenderer implements BlockEntityRenderer<ArcFurnaceBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_ARC_FURNACE;

    public ArcFurnaceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ArcFurnaceBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(ArcFurnaceBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }
        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        LegacyArcFurnaceRenderHelper.renderPlan(MODEL,
                LegacyTileRenderPlans.arcFurnacePlan(
                        blockEntity.getPreviousLid(),
                        blockEntity.getLid(),
                        blockEntity.isProgressing(),
                        blockEntity.getLevel() == null ? 0L : blockEntity.getLevel().getGameTime(),
                        partialTick,
                        blockEntity.getLiquidAmount(),
                        blockEntity.getMaxLiquid(),
                        blockEntity.hasMaterial(),
                        electrodeStates(blockEntity)),
                new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay),
                poseStack);
        poseStack.popPose();
    }

    private static List<LegacyTileRenderPlans.ArcElectrodeState> electrodeStates(ArcFurnaceBlockEntity blockEntity) {
        return blockEntity.electrodeStates().stream()
                .map(ArcFurnaceRenderer::electrodeState)
                .toList();
    }

    private static LegacyTileRenderPlans.ArcElectrodeState electrodeState(byte state) {
        return switch (state) {
            case 1 -> LegacyTileRenderPlans.ArcElectrodeState.FRESH;
            case 2 -> LegacyTileRenderPlans.ArcElectrodeState.USED;
            case 3 -> LegacyTileRenderPlans.ArcElectrodeState.DEPLETED;
            default -> LegacyTileRenderPlans.ArcElectrodeState.NONE;
        };
    }
}
