package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.MiningLaserBlockEntity;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MiningLaserRenderer implements BlockEntityRenderer<MiningLaserBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_MINING_LASER;

    public MiningLaserRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(MiningLaserBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(MiningLaserBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }
        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);

        double tx = Mth.lerp(partialTick, blockEntity.getLastTargetX(), blockEntity.getTargetX());
        double ty = Mth.lerp(partialTick, blockEntity.getLastTargetY(), blockEntity.getTargetY());
        double tz = Mth.lerp(partialTick, blockEntity.getLastTargetZ(), blockEntity.getTargetZ());
        double vx = tx - blockEntity.getBlockPos().getX();
        double vy = ty - blockEntity.getBlockPos().getY() + 3.0D;
        double vz = tz - blockEntity.getBlockPos().getZ();
        Vec3 normal = new Vec3(vx, vy, vz).normalize().scale(1.5D);
        Vec3 beam = new Vec3(vx - normal.x, vy - normal.y, vz - normal.z);
        double yaw = Math.toDegrees(Math.atan2(beam.x, beam.z));
        double horizontal = Math.sqrt(beam.x * beam.x + beam.z * beam.z);
        double pitch = Math.toDegrees(Math.atan2(beam.y, horizontal));

        poseStack.pushPose();
        poseStack.translate(0.5D, -1.0D, 0.5D);
        MODEL.renderPart("Base", ObjMachineModels.MINING_LASER_BASE_TEXTURE, context);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) yaw));
        MODEL.renderPart("Pivot", ObjMachineModels.MINING_LASER_PIVOT_TEXTURE, context);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) yaw));
        poseStack.translate(0.0D, -1.0D, 0.0D);
        poseStack.mulPose(Axis.XN.rotationDegrees((float) pitch + 90.0F));
        poseStack.translate(0.0D, 1.0D, 0.0D);
        MODEL.renderPart("Laser", ObjMachineModels.MINING_LASER_LASER_TEXTURE, context);
        poseStack.popPose();

        if (blockEntity.hasBeam()) {
            poseStack.translate(normal.x, normal.y - 1.0D, normal.z);
            int range = (int) Math.ceil(beam.length() * 0.5D);
            int ticks = blockEntity.getLevel() == null ? 0 : (int) blockEntity.getLevel().getGameTime();
            for (int offset = 0; offset < 360; offset += 120) {
                LegacyBeamRenderer.beam(poseStack, buffer,
                        LegacyBeamRenderer.beamPlan(beam.x, beam.y, beam.z,
                                LegacyBeamRenderer.WaveType.SPIRAL,
                                LegacyBeamRenderer.BeamType.SOLID,
                                0xA00000, 0xA00000,
                                ticks * -25 + offset, Math.max(1, range * 2), 0.075F, 3, 0.025F));
            }
        }
        poseStack.popPose();
    }
}
