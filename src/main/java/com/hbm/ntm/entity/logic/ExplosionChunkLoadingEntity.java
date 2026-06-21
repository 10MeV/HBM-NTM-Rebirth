package com.hbm.ntm.entity.logic;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.config.BombConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.network.NetworkHooks;

public abstract class ExplosionChunkLoadingEntity extends Entity {
    private static final String TAG_SAVED_MILLIS = "loaderSavedMillis";
    private long centerChunk = Long.MIN_VALUE;
    private long workChunk = Long.MIN_VALUE;

    protected ExplosionChunkLoadingEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    protected void forceCenterChunk() {
        if (!chunkLoadingEnabled()) {
            clearChunkLoader();
            return;
        }
        ChunkPos chunkPos = chunkPosition();
        long packed = chunkPos.toLong();
        if (centerChunk == packed) {
            return;
        }
        releaseForcedChunk(centerChunk);
        centerChunk = packed;
        forceChunk(chunkPos.x, chunkPos.z);
    }

    protected void loadChunk(int chunkX, int chunkZ) {
        if (!chunkLoadingEnabled()) {
            clearChunkLoader();
            return;
        }
        long packed = ChunkPos.asLong(chunkX, chunkZ);
        if (workChunk == packed) {
            return;
        }
        releaseForcedChunk(workChunk);
        workChunk = packed;
        forceChunk(chunkX, chunkZ);
    }

    protected void clearChunkLoader() {
        releaseForcedChunk(centerChunk);
        releaseForcedChunk(workChunk);
        centerChunk = Long.MIN_VALUE;
        workChunk = Long.MIN_VALUE;
    }

    protected void saveChunkLoader(CompoundTag tag) {
        tag.putLong("loaderCenterChunk", centerChunk);
        tag.putLong("loaderWorkChunk", workChunk);
        tag.putLong(TAG_SAVED_MILLIS, System.currentTimeMillis());
    }

    protected void readChunkLoader(CompoundTag tag) {
        // Saved chunk ids are stale after a world load; force them again on the next tick.
        centerChunk = Long.MIN_VALUE;
        workChunk = Long.MIN_VALUE;
    }

    protected boolean shouldExpireFromSave(CompoundTag tag) {
        int limitSeconds = BombConfig.explosionLifespanLimitSeconds();
        if (limitSeconds <= 0) {
            return false;
        }
        long savedMillis = tag.contains("milliTime") ? tag.getLong("milliTime") : tag.getLong(TAG_SAVED_MILLIS);
        return savedMillis > 0L && System.currentTimeMillis() - savedMillis > limitSeconds * 1000L;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private static boolean chunkLoadingEnabled() {
        return BombConfig.chunkLoadingEnabled();
    }

    private void forceChunk(int chunkX, int chunkZ) {
        if (level() instanceof ServerLevel serverLevel) {
            ForgeChunkManager.forceChunk(serverLevel, HbmNtm.MOD_ID, this, chunkX, chunkZ, true, true);
        }
    }

    private void releaseForcedChunk(long packed) {
        if (packed == Long.MIN_VALUE || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ChunkPos chunkPos = new ChunkPos(packed);
        ForgeChunkManager.forceChunk(serverLevel, HbmNtm.MOD_ID, this, chunkPos.x, chunkPos.z, false, true);
    }

    @Override
    public void remove(RemovalReason reason) {
        clearChunkLoader();
        super.remove(reason);
    }
}
