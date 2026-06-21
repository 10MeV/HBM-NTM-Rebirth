package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.blockentity.MultiblockDummyBlockEntity;
import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.neutron.RBMKStructureDimensions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MultiblockDummyRenderer implements BlockEntityRenderer<MultiblockDummyBlockEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public MultiblockDummyRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(MultiblockDummyBlockEntity dummy, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = dummy.getLevel();
        if (level == null) {
            return;
        }
        BlockPos dummyPos = dummy.getBlockPos();
        MultiblockHelper.CoreLookup core = resolveColumnCore(level, dummy);
        if (core == null) {
            return;
        }
        BlockPos corePos = core.pos();
        if (dummyPos.getX() != corePos.getX() || dummyPos.getZ() != corePos.getZ()) {
            return;
        }
        int segment = dummyPos.getY() - corePos.getY();
        int heightAbove = RBMKColumnRenderer.columnHeightAbove();
        if (segment <= 0 || segment > heightAbove) {
            return;
        }
        BlockState coreState = core.state();
        if (!(coreState.getBlock() instanceof RBMKColumnBlock)) {
            return;
        }
        BlockEntity coreEntity = level.getBlockEntity(corePos);
        int modelLight = coreEntity instanceof RBMKColumnBlockEntity column
                ? LegacyRenderLighting.resolveMultiblockLight(column, packedLight)
                : packedLight;
        RBMKColumnRenderer.renderStaticSegment(blockRenderer, coreState, segment, heightAbove, poseStack, buffer,
                modelLight);
        if (coreEntity instanceof RBMKColumnBlockEntity column
                && MultiblockHelper.isOperationalCoreLayoutComplete(level, corePos)) {
            RBMKColumnRenderer.renderDynamicSegment(column, segment, partialTick, poseStack, buffer, modelLight,
                    packedOverlay);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(MultiblockDummyBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    private static MultiblockHelper.CoreLookup resolveColumnCore(Level level, MultiblockDummyBlockEntity dummy) {
        BlockPos corePos = dummy.getCorePos();
        if (corePos != null) {
            MultiblockHelper.CoreLookup core = MultiblockHelper.findCoreAt(level, corePos);
            if (core != null && core.state().getBlock() instanceof RBMKColumnBlock
                    && core.pos().getX() == dummy.getBlockPos().getX()
                    && core.pos().getZ() == dummy.getBlockPos().getZ()) {
                return core;
            }
        }
        return RBMKStructureDimensions.findVerticalColumnCore(level, dummy.getBlockPos());
    }
}
