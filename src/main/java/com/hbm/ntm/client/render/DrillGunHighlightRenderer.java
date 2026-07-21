package com.hbm.ntm.client.render;

import com.hbm.ntm.client.obj.LegacyLineRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.item.DrillGunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;

/**
 * Source-backed replacement for the 1.7.10 drill selection overlay.  The old handler cancelled the vanilla
 * highlight whenever a drill was held, including when the extended-reach ray did not find a block.
 */
@OnlyIn(Dist.CLIENT)
public final class DrillGunHighlightRenderer {
    private static final double EXPAND = 0.002D;
    private static final int WHITE = 0xFFFFFF;
    private static final int DARK_RED = 0x800000;
    private static final int ALPHA = 102;
    private static final float LINE_WIDTH = 2.0F;

    /**
     * @return true when the legacy drill path owned this highlight event.
     */
    public static boolean render(RenderHighlightEvent.Block event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof DrillGunItem drill)) {
            return false;
        }

        HitResult target = player.pick(drill.highlightReach(stack), event.getPartialTick(), false);
        if (target instanceof BlockHitResult blockHit) {
            draw(event, blockHit.getBlockPos(), player.isShiftKeyDown() ? 0 : drill.highlightArea(stack));
        }
        event.setCanceled(true);
        return true;
    }

    private static void draw(RenderHighlightEvent.Block event, BlockPos origin, int area) {
        Vec3 cameraPos = event.getCamera().getPosition();
        double offsetX = origin.getX() - cameraPos.x;
        double offsetY = origin.getY() - cameraPos.y;
        double offsetZ = origin.getZ() - cameraPos.z;

        VertexConsumer consumer = LegacyLineRenderer.consumer(event.getMultiBufferSource(), LINE_WIDTH,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, ALPHA);
        PoseStack.Pose pose = event.getPoseStack().last();
        if (area <= 0) {
            box(consumer, pose, offsetX, offsetY, offsetZ, 0, DARK_RED);
            return;
        }

        box(consumer, pose, offsetX, offsetY, offsetZ, 0, WHITE);
        LegacyLineRenderer.box(consumer, pose,
                -area - EXPAND + offsetX, -area - EXPAND + offsetY, -area - EXPAND + offsetZ,
                1.0D + area + EXPAND + offsetX, 1.0D + area + EXPAND + offsetY, 1.0D + area + EXPAND + offsetZ,
                DARK_RED, ALPHA);
    }

    private static void box(VertexConsumer consumer, PoseStack.Pose pose,
            double offsetX, double offsetY, double offsetZ, int area, int color) {
        LegacyLineRenderer.box(consumer, pose,
                -area - EXPAND + offsetX, -area - EXPAND + offsetY, -area - EXPAND + offsetZ,
                1.0D + area + EXPAND + offsetX, 1.0D + area + EXPAND + offsetY, 1.0D + area + EXPAND + offsetZ,
                color, ALPHA);
    }

    private DrillGunHighlightRenderer() {
    }
}
