package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.PyroOvenBlockEntity;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class PyroOvenRenderer implements BlockEntityRenderer<PyroOvenBlockEntity> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();

    public PyroOvenRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(PyroOvenBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(PyroOvenBlockEntity pyroOven, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = pyroOven.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(pyroOven, state, definition, packedLight);
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()));
        float anim = Mth.lerp(partialTick, pyroOven.getPrevAnim(), pyroOven.getAnim());

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));

        model.renderPart("Oven", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(LegacyObjTransforms.softPeakSine(anim * 0.125D) / 2.0D - 0.5D, 0.0D, 0.0D);
        model.renderPart("Slider", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        LegacyObjTransforms.rotateAroundY(poseStack, 1.5D, 0.0D, 1.5D, (anim * 45.0F) % 360.0F);
        model.renderPart("Fan", definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.popPose();
    }
}
