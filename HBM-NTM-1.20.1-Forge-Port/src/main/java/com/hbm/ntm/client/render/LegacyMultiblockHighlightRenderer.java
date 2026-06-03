package com.hbm.ntm.client.render;

import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
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
        if (core == null || !(core.state().getBlock() instanceof LegacyVisibleMultiblockMachineBlock machine)) {
            return;
        }
        if (!machine.definition().hasCollisionShapeFactory()) {
            return;
        }

        VoxelShape shape = machine.definition().highlightShape(core.state());
        if (shape.isEmpty()) {
            return;
        }

        drawShape(event, core.pos(), shape);
        event.setCanceled(true);
    }

    private static void drawShape(RenderHighlightEvent.Block event, BlockPos corePos, VoxelShape shape) {
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
