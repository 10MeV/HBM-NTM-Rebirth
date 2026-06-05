package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.Bat9000BlockEntity;
import com.hbm.ntm.blockentity.BigAssTankBlockEntity;
import com.hbm.ntm.blockentity.FluidTankBlockEntity;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class FluidTankRenderer<T extends FluidTankBlockEntity> implements BlockEntityRenderer<T> {
    public FluidTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        float rotation = state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block
                ? block.definition().yRotation(state)
                : state.hasProperty(HorizontalMachineBlock.FACING)
                        ? (360.0F - state.getValue(HorizontalMachineBlock.FACING).toYRot()) % 360.0F
                        : 180.0F;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        if (blockEntity instanceof Bat9000BlockEntity) {
            ObjModelLibrary.MACHINE_BAT9000.renderAll(poseStack, buffer, packedLight, packedOverlay);
            LegacyFluidTankRenderHelper.renderBat9000Diamonds(blockEntity.getTank().getTankType(), poseStack, buffer,
                    packedLight, packedOverlay);
            LegacyFluidTankRenderHelper.renderBat9000Fluid(blockEntity.getTank(), state, poseStack, buffer,
                    packedLight, packedOverlay);
        } else if (blockEntity instanceof BigAssTankBlockEntity) {
            ObjModelLibrary.MACHINE_BIGASSTANK.renderAll(poseStack, buffer, packedLight, packedOverlay);
            LegacyFluidTankRenderHelper.renderBigAssTankDiamonds(blockEntity.getTank().getTankType(), poseStack, buffer,
                    packedLight, packedOverlay);
            LegacyFluidTankRenderHelper.renderBigAssTankFluid(blockEntity.getTank(), state, poseStack, buffer,
                    packedLight, packedOverlay, blockEntity.getLevel() == null ? partialTick
                            : blockEntity.getLevel().getGameTime() + partialTick);
        } else if (blockEntity.isExploded()) {
            LegacyFluidTankRenderHelper.renderSmallTank(ObjModelLibrary.MACHINE_FLUIDTANK,
                    ObjModelLibrary.MACHINE_FLUIDTANK_EXPLODED, blockEntity.getTank(), true, poseStack, buffer,
                    packedLight, packedOverlay);
        } else {
            LegacyFluidTankRenderHelper.renderSmallTank(ObjModelLibrary.MACHINE_FLUIDTANK,
                    ObjModelLibrary.MACHINE_FLUIDTANK_EXPLODED, blockEntity.getTank(), false, poseStack, buffer,
                    packedLight, packedOverlay);
        }
        poseStack.popPose();
    }
}
