package com.hbm.ntm.client.light;

import com.hbm.ntm.item.No9ArmorItem;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-only, short-lived block-light writes used by legacy effects that called
 * {@code World#setLightValue(EnumSkyBlock.Block, ...)}.  The backing vanilla
 * block-light nibbles are changed only in the client level and are restored by
 * the normal light engine after their legacy five-tick deadline.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientBlockLightOverrides {
    private static final int NO9_RANGE = 50;
    private static final int LEGACY_EXPIRY_TICKS = 5;

    private static final LongOpenHashSet BREADCRUMBS = new LongOpenHashSet();
    private static final Long2LongOpenHashMap EXPIRIES = new Long2LongOpenHashMap();
    private static Level activeLevel;

    static {
        EXPIRIES.defaultReturnValue(Long.MIN_VALUE);
    }

    private ClientBlockLightOverrides() {
    }

    /** Mirrors ArmorNo9#updateWorldHook: expire old writes through vanilla's block-light recalculation. */
    public static void tick(Level level) {
        if (level == null || !level.isClientSide) {
            return;
        }
        ensureLevel(level);

        long gameTime = level.getGameTime();
        if (EXPIRIES.isEmpty()) {
            return;
        }

        LayerLightEventListener listener = level.getLightEngine().getLayerListener(LightLayer.BLOCK);
        boolean recalculated = false;
        LongIterator iterator = EXPIRIES.keySet().iterator();
        while (iterator.hasNext()) {
            long packedPos = iterator.nextLong();
            if (gameTime > EXPIRIES.get(packedPos)) {
                listener.checkBlock(BlockPos.of(packedPos));
                iterator.remove();
                recalculated = true;
            }
        }
        if (recalculated) {
            level.getLightEngine().runLightUpdates();
        }
    }

    /**
     * Mirrors the client half of ArmorNo9#onArmorTick.  The source uses the
     * synced {@code isOn} armor tag, runs every other world tick, ray-traces 50
     * blocks, and writes a recursively attenuated temporary block-light field.
     */
    public static void tickNo9(Level level, Player player) {
        if (level == null || player == null || !level.isClientSide || level.getGameTime() % 2L != 0L) {
            return;
        }
        ensureLevel(level);

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof No9ArmorItem) || !helmet.hasTag() || !helmet.getTag().getBoolean("isOn")) {
            return;
        }

        Vec3 rayStart = player.getEyePosition(0.0F);
        Vec3 rayEnd = rayStart.add(player.getViewVector(0.0F).scale(NO9_RANGE));
        BlockHitResult hit = level.clip(new ClipContext(rayStart, rayEnd, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        Vec3 currentEye = player.getEyePosition();
        int light = Math.min(15, (int) (25.0D - currentEye.subtract(hit.getLocation()).length() * 25.0D / NO9_RANGE));
        BlockPos target = hit.getBlockPos().relative(hit.getDirection());
        LayerLightSectionStorage<?> storage = getBlockLightStorage(level.getLightEngine());
        if (storage == null) {
            return;
        }
        BREADCRUMBS.clear();
        try {
            writeRecursively(level, storage, target, light, level.getGameTime() + LEGACY_EXPIRY_TICKS);
            // The legacy recursion completed within one client tick. Commit the same complete field once,
            // so vanilla emits one coherent batch of section-light update notifications.
            storage.swapSectionMap();
        } finally {
            BREADCRUMBS.clear();
        }
    }

    /** Drops only client-side tracking when a level is discarded; legacy never persisted this data. */
    public static void clear() {
        BREADCRUMBS.clear();
        EXPIRIES.clear();
        activeLevel = null;
    }

    private static void writeRecursively(Level level, LayerLightSectionStorage<?> storage, BlockPos pos, int light,
            long expiry) {
        if (light <= 0 || pos.getY() < level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight()
                || !level.hasChunkAt(pos)) {
            return;
        }

        long packedPos = pos.asLong();
        if (!BREADCRUMBS.add(packedPos)) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        // 1.20.1 exposes the old 0..255 opacity contract as its 0..15 light-block scale.
        if (state.getLightBlock(level, pos) >= 15) {
            return;
        }

        LevelLightEngine lightEngine = level.getLightEngine();
        LayerLightEventListener listener = lightEngine.getLayerListener(LightLayer.BLOCK);
        int newLight = Math.min(15, Math.max(listener.getLightValue(pos), light));
        storage.setStoredLevel(packedPos, newLight);
        EXPIRIES.put(packedPos, expiry);

        for (Direction direction : Direction.values()) {
            writeRecursively(level, storage, pos.relative(direction), light - 1, expiry);
        }
    }

    @SuppressWarnings("unchecked")
    private static LayerLightSectionStorage<?> getBlockLightStorage(LevelLightEngine lightEngine) {
        LayerLightEventListener listener = lightEngine.getLayerListener(LightLayer.BLOCK);
        if (!(listener instanceof LightEngine<?, ?> engine)) {
            return null;
        }
        return engine.storage;
    }

    private static void ensureLevel(Level level) {
        if (activeLevel != level) {
            BREADCRUMBS.clear();
            EXPIRIES.clear();
            activeLevel = level;
        }
    }
}
