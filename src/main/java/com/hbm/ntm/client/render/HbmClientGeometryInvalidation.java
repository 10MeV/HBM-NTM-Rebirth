package com.hbm.ntm.client.render;

import com.hbm.ntm.client.render.culling.HbmRenderFrameCulling;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class HbmClientGeometryInvalidation {
    private static final ConcurrentLinkedQueue<BlockPos> PENDING_INVALIDATIONS = new ConcurrentLinkedQueue<>();
    private static final Set<BlockPos> PENDING_POSITIONS = ConcurrentHashMap.newKeySet();

    private HbmClientGeometryInvalidation() {
    }

    public static void schedule(BlockPos pos) {
        if (pos == null) {
            return;
        }
        BlockPos immutable = pos.immutable();
        if (PENDING_POSITIONS.add(immutable)) {
            PENDING_INVALIDATIONS.add(immutable);
        }
    }

    public static void scheduleWithNeighbors(BlockPos pos) {
        schedule(pos);
        if (pos == null) {
            return;
        }
        for (Direction direction : Direction.values()) {
            schedule(pos.relative(direction));
        }
    }

    public static void processPendingInvalidations() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.levelRenderer == null) {
            clearPending();
            return;
        }

        boolean processed = false;
        BlockPos pos;
        while ((pos = PENDING_INVALIDATIONS.poll()) != null) {
            PENDING_POSITIONS.remove(pos);
            BlockState state = minecraft.level.getBlockState(pos);
            minecraft.levelRenderer.blockChanged(minecraft.level, pos, state, state, Block.UPDATE_CLIENTS);
            processed = true;
        }

        if (processed) {
            HbmRenderFrameCulling.noteClientGeometryChanged();
        }
    }

    public static void noteWorldGeometryChanged() {
        HbmRenderFrameCulling.noteClientGeometryChanged();
    }

    public static void clearPending() {
        PENDING_INVALIDATIONS.clear();
        PENDING_POSITIONS.clear();
    }
}
