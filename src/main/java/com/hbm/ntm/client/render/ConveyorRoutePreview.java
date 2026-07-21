package com.hbm.ntm.client.render;

import com.hbm.ntm.api.conveyor.ConveyorRoutePlanner;
import com.hbm.ntm.client.obj.ObjRenderUtils;
import com.hbm.ntm.item.ConveyorWandItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.List;

/** Client-side replacement for the legacy conveyor wand's WorldInAJar action preview. */
@OnlyIn(Dist.CLIENT)
public final class ConveyorRoutePreview {
    private static final int SUCCESS_COLOR = 0x00FFFF;
    private static final int FAILURE_COLOR = 0xFF0000;
    private static final PoseStack PREVIEW_POSE = new PoseStack();

    private static List<PreviewBlock> previewBlocks = List.of();
    private static Level previewLevel;
    private static boolean previewSuccess;
    private static BlockPos cachedHit;
    private static Direction cachedSide;
    private static float cachedYaw;
    private static int cachedStartTagHash;

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        Player player = minecraft.player;
        if (level == null || player == null) {
            clear();
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ConveyorWandItem) || !ConveyorWandItem.hasStart(stack)
                || !(minecraft.hitResult instanceof BlockHitResult hit)) {
            clear();
            return;
        }

        BlockPos hitPos = hit.getBlockPos();
        Direction hitSide = hit.getDirection();
        float yaw = player.getYRot();
        int startTagHash = stack.getTag().hashCode();
        if (level == previewLevel && hitPos.equals(cachedHit) && hitSide == cachedSide
                && startTagHash == cachedStartTagHash && Math.abs(yaw - cachedYaw) < 15.0F) {
            return;
        }

        ConveyorRoutePlanner.RouteResult route = ConveyorWandItem.planRoute(level, player, stack, hitPos, hitSide);
        List<BlockState> previewStates = ConveyorWandItem.blockStatesForPreview(level, route.placements());
        previewBlocks = java.util.stream.IntStream.range(0, route.placements().size())
                .mapToObj(index -> new PreviewBlock(route.placements().get(index).pos(), previewStates.get(index)))
                .toList();
        previewLevel = level;
        previewSuccess = route.status() == ConveyorRoutePlanner.Status.SUCCESS;
        cachedHit = hitPos.immutable();
        cachedSide = hitSide;
        cachedYaw = yaw;
        cachedStartTagHash = startTagHash;
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER || previewBlocks.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != previewLevel) {
            clear();
            return;
        }

        Camera camera = event.getCamera();
        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        PoseStack poseStack = PREVIEW_POSE;
        poseStack.setIdentity();
        int color = previewSuccess ? SUCCESS_COLOR : FAILURE_COLOR;

        for (PreviewBlock preview : previewBlocks) {
            BlockPos pos = preview.pos();
            poseStack.pushPose();
            poseStack.translate(pos.getX() - camera.getPosition().x, pos.getY() - camera.getPosition().y,
                    pos.getZ() - camera.getPosition().z);
            BlockState state = preview.state();
            ObjRenderUtils.renderBlockModel(dispatcher.getBlockModel(state), state, dispatcher.getModelRenderer(), poseStack,
                    buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, null, 1.0F, color, true, false);
            poseStack.popPose();
        }
        buffer.endBatch();
    }

    public static void clear() {
        previewBlocks = List.of();
        previewLevel = null;
        previewSuccess = false;
        cachedHit = null;
        cachedSide = null;
        cachedYaw = 0.0F;
        cachedStartTagHash = 0;
    }

    private record PreviewBlock(BlockPos pos, BlockState state) {
    }

    private ConveyorRoutePreview() {
    }
}
