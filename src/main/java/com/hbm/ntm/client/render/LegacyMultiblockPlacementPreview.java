package com.hbm.ntm.client.render;

import com.hbm.ntm.client.obj.LegacyLineRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyMultiblockPlaceable;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Modern shared equivalent of {@code BlockDummyable#drawPlacementHighlight}.
 * It intentionally uses the same direct-placement contract as {@link BlockItem}.
 */
@OnlyIn(Dist.CLIENT)
public final class LegacyMultiblockPlacementPreview {
    private static final double EXPAND = 0.002D;
    private static final int ALPHA = 255;
    private static final float LINE_WIDTH = 2.0F;

    /**
     * @return true when a directly placeable legacy multiblock consumed the highlight event.
     */
    public static boolean render(RenderHighlightEvent.Block event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Level level = minecraft.level;
        if (player == null || level == null) {
            return false;
        }
        BlockHitResult hit = event.getTarget();

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof BlockItem item)) {
            return false;
        }
        Block block = item.getBlock();
        if (!(block instanceof LegacyMultiblockPlaceable placeable)
                || !(block instanceof MultiblockCoreBlock coreBlock)) {
            return false;
        }

        BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit);
        BlockState state = placeable.getDirectPlacementState(context);
        if (state == null) {
            return false;
        }
        BlockPos corePos = placeable.getDirectPlacementCore(context, state);
        LegacyMultiblockLayout layout = coreBlock.getMultiblockLayout(state, level, corePos);
        if (layout == null) {
            return false;
        }

        Set<BlockPos> positions = previewPositions(corePos, layout);
        boolean canPlace = block.isEnabled(level.enabledFeatures()) && context.canPlace()
                && placeable.canPlaceDirectMultiblock(level, corePos, context.getClickedPos(), state);
        draw(event, positions, canPlace ? successColor() : failureColor());
        event.setCanceled(true);
        return true;
    }

    private static Set<BlockPos> previewPositions(BlockPos corePos, LegacyMultiblockLayout layout) {
        Set<BlockPos> positions = new LinkedHashSet<>();
        positions.add(corePos.immutable());
        for (BlockPos offset : layout.checkOffsets()) {
            positions.add(corePos.offset(offset).immutable());
        }
        return positions;
    }

    private static void draw(RenderHighlightEvent.Block event, Set<BlockPos> positions, int color) {
        Vec3 camera = event.getCamera().getPosition();
        VertexConsumer consumer = LegacyLineRenderer.consumer(event.getMultiBufferSource(), LINE_WIDTH,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, ALPHA);
        PoseStack.Pose pose = event.getPoseStack().last();
        for (BlockPos pos : positions) {
            drawExposedEdges(consumer, pose, pos, positions, camera, color);
        }
    }

    /** Matches the old preview's omission of internal shared edges. */
    private static void drawExposedEdges(VertexConsumer consumer, PoseStack.Pose pose, BlockPos pos,
            Set<BlockPos> positions, Vec3 camera, int color) {
        boolean east = positions.contains(pos.east());
        boolean west = positions.contains(pos.west());
        boolean up = positions.contains(pos.above());
        boolean down = positions.contains(pos.below());
        boolean south = positions.contains(pos.south());
        boolean north = positions.contains(pos.north());

        double minX = pos.getX() - camera.x - EXPAND;
        double minY = pos.getY() - camera.y - EXPAND;
        double minZ = pos.getZ() - camera.z - EXPAND;
        double maxX = pos.getX() + 1.0D - camera.x + EXPAND;
        double maxY = pos.getY() + 1.0D - camera.y + EXPAND;
        double maxZ = pos.getZ() + 1.0D - camera.z + EXPAND;

        if (!up) {
            line(consumer, pose, minX, maxY, minZ, minX, maxY, maxZ, color, !west);
            line(consumer, pose, minX, maxY, maxZ, maxX, maxY, maxZ, color, !south);
            line(consumer, pose, maxX, maxY, maxZ, maxX, maxY, minZ, color, !east);
            line(consumer, pose, maxX, maxY, minZ, minX, maxY, minZ, color, !north);
        }
        if (!down) {
            line(consumer, pose, minX, minY, minZ, minX, minY, maxZ, color, !west);
            line(consumer, pose, minX, minY, maxZ, maxX, minY, maxZ, color, !south);
            line(consumer, pose, maxX, minY, maxZ, maxX, minY, minZ, color, !east);
            line(consumer, pose, maxX, minY, minZ, minX, minY, minZ, color, !north);
        }
        if (!north) {
            line(consumer, pose, minX, minY, minZ, minX, maxY, minZ, color, !west);
            line(consumer, pose, minX, maxY, minZ, maxX, maxY, minZ, color, !up);
            line(consumer, pose, maxX, maxY, minZ, maxX, minY, minZ, color, !east);
            line(consumer, pose, maxX, minY, minZ, minX, minY, minZ, color, !down);
        }
        if (!south) {
            line(consumer, pose, minX, minY, maxZ, minX, maxY, maxZ, color, !west);
            line(consumer, pose, minX, maxY, maxZ, maxX, maxY, maxZ, color, !up);
            line(consumer, pose, maxX, maxY, maxZ, maxX, minY, maxZ, color, !east);
            line(consumer, pose, maxX, minY, maxZ, minX, minY, maxZ, color, !down);
        }
        if (!west) {
            line(consumer, pose, minX, minY, minZ, minX, maxY, minZ, color, !north);
            line(consumer, pose, minX, maxY, minZ, minX, maxY, maxZ, color, !up);
            line(consumer, pose, minX, maxY, maxZ, minX, minY, maxZ, color, !south);
            line(consumer, pose, minX, minY, maxZ, minX, minY, minZ, color, !down);
        }
        if (!east) {
            line(consumer, pose, maxX, minY, minZ, maxX, maxY, minZ, color, !north);
            line(consumer, pose, maxX, maxY, minZ, maxX, maxY, maxZ, color, !up);
            line(consumer, pose, maxX, maxY, maxZ, maxX, minY, maxZ, color, !south);
            line(consumer, pose, maxX, minY, maxZ, maxX, minY, minZ, color, !down);
        }
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose,
            double x0, double y0, double z0, double x1, double y1, double z1, int color, boolean visible) {
        if (visible) {
            LegacyLineRenderer.line(consumer, pose, x0, y0, z0, x1, y1, z1, color, ALPHA);
        }
    }

    private static int successColor() {
        int intensity = pulseIntensity();
        return intensity << 8;
    }

    private static int failureColor() {
        return pulseIntensity() << 16;
    }

    private static int pulseIntensity() {
        double timer = (System.currentTimeMillis() % (1_000.0D * Math.PI)) / 250.0D;
        return (int) (255.0D * (Math.sin(timer) * 0.25D + 0.75D));
    }

    private LegacyMultiblockPlacementPreview() {
    }
}
