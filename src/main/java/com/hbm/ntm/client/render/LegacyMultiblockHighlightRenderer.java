package com.hbm.ntm.client.render;

import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;

@OnlyIn(Dist.CLIENT)
public final class LegacyMultiblockHighlightRenderer {
    private static final double EXPAND = 0.002D;
    private static final float RED = 0.0F;
    private static final float GREEN = 0.0F;
    private static final float BLUE = 0.0F;
    private static final float ALPHA = 0.4F;

    public static void render(RenderHighlightEvent.Block event) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) {
            return;
        }

        BlockHitResult hit = event.getTarget();
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, hit.getBlockPos());
        if (core == null) {
            return;
        }

        VoxelShape shape = highlightShape(level, core, hit.getBlockPos());
        if (shape == null || shape.isEmpty()) {
            return;
        }

        drawShape(event, core.pos(), shape);
        event.setCanceled(true);
    }

    private static VoxelShape highlightShape(Level level, MultiblockHelper.CoreLookup core, BlockPos hitPos) {
        Object block = core.state().getBlock();
        if (block instanceof MultiblockCoreBlock multiblock
                && !multiblock.usesMultiblockHighlightShape(core.state(), level, core.pos())) {
            return null;
        }
        if (block instanceof LegacyVisibleMultiblockMachineBlock machine
                && machine.definition().hasCollisionShapeFactory()) {
            return machine.definition().highlightShape(core.state());
        }
        if (!(block instanceof MultiblockCoreBlock coreBlock)) {
            return null;
        }
        VoxelShape shape = coreBlock.getMultiblockShape(core.state(), level, core.pos(), CollisionContext.empty());
        if (shape.isEmpty()) {
            return Shapes.empty();
        }
        if (!hitPos.equals(core.pos()) || isLargerThanSingleBlock(shape.bounds())) {
            return shape;
        }
        return null;
    }

    private static boolean isLargerThanSingleBlock(AABB box) {
        return box.minX < 0.0D || box.minY < 0.0D || box.minZ < 0.0D
                || box.maxX > 1.0D || box.maxY > 1.0D || box.maxZ > 1.0D;
    }

    private static void drawShape(RenderHighlightEvent.Block event, BlockPos corePos, VoxelShape shape) {
        if (shape.isEmpty()) {
            return;
        }

        Vec3 cameraPos = event.getCamera().getPosition();
        double offsetX = corePos.getX() - cameraPos.x;
        double offsetY = corePos.getY() - cameraPos.y;
        double offsetZ = corePos.getZ() - cameraPos.z;

        PoseStack poseStack = event.getPoseStack();
        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
        for (AABB box : shape.toAabbs()) {
            LevelRenderer.renderLineBox(poseStack, consumer, box.inflate(EXPAND).move(offsetX, offsetY, offsetZ),
                    RED, GREEN, BLUE, ALPHA);
        }
    }

    private LegacyMultiblockHighlightRenderer() {
    }
}
